package com.xy.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.constants.ArticleConstants;
import com.xy.common.redis.CacheService;
import com.xy.model.article.dtos.ArticleDto;
import com.xy.model.article.dtos.ArticleHomeDto;
import com.xy.model.article.pojos.ApArticle;
import com.xy.model.article.pojos.ApArticleConfig;
import com.xy.model.article.pojos.ApArticleContent;
import com.xy.model.article.vos.HotArticleVo;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.common.enums.AppHttpCodeEnum;
import com.xy.model.mess.ArticleVisitStreamMess;
import com.xy.article.mapper.ApArticleConfigMapper;
import com.xy.article.mapper.ApArticleContentMapper;
import com.xy.article.mapper.ApArticleMapper;
import com.xy.article.service.ApArticleService;
import com.xy.article.service.ArticleFreemarkerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author 杨路恒
 */
@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    //单页最大加载的数字
    private final static short MAX_PAGE_SIZE = 50;
    @Autowired
    ApArticleMapper apArticleMapper;
    /**
     * 根据参数加载文章列表
     * @param loadType  1 加载更多 2 加载最新
     * @param articleHomeDto
     * @return
     */
    @Override
    public ResponseResult load(Short loadType, ArticleHomeDto articleHomeDto) {
        //1.校验参数
        Integer size = articleHomeDto.getSize();
        if (size == null || size == 0){
            size = 10;
        }
        size = Math.min(size,MAX_PAGE_SIZE);
        articleHomeDto.setSize(size);
        //类型校验
        if (!loadType.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !loadType.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            articleHomeDto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        //文章频道校验
        if (StringUtils.isEmpty(articleHomeDto.getTag())){
            articleHomeDto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        //时间校验
        if (articleHomeDto.getMaxBehotTime() == null){
            articleHomeDto.setMaxBehotTime(new Date());
        }
        if (articleHomeDto.getMinBehotTime() == null){
            articleHomeDto.setMinBehotTime(new Date());
        }
        //2.查询数据
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(articleHomeDto, loadType);
        //3.结果封装
        ResponseResult responseResult = ResponseResult.okResult(apArticles);
        return responseResult;
    }
    @Autowired
    private CacheService cacheService;
    /**
     * 根据参数加载文章列表
     * @param loadType
     * @param articleHomeDto
     * @param firstPage
     * @return
     */
    @Override
    public ResponseResult load2(Short loadType, ArticleHomeDto articleHomeDto, boolean firstPage) {
        if (firstPage){
            String jsonStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + articleHomeDto.getTag());
            if (StringUtils.isNoneBlank(jsonStr)){
                List<HotArticleVo> hotArticleVoList = JSON.parseArray(jsonStr, HotArticleVo.class);
                ResponseResult responseResult = ResponseResult.okResult(hotArticleVoList);
                return responseResult;
            }
        }
        return load(loadType,articleHomeDto);
    }

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;
    /**
     * 保存app端相关文章
     * @param articleDto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto articleDto) {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        //1.检查参数
        if (articleDto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(articleDto,apArticle);
        //2.判断是否存在id
        if (articleDto.getId() == null){
            //2.1   不存在id 保存 文章 文章配置 文章内容
            //保存 文章
            save(apArticle);
            //保存  文章配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);
            //保存 文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(articleDto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        }
        else {
            //2.2   存在id 修改 文章 文章内容
            //修改 文章
            updateById(apArticle);
            //修改 文章内容
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId,articleDto.getId()));
            apArticleContent.setContent(articleDto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
            //修改  文章配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(articleDto.getId());
            apArticleConfigMapper.updateById(apArticleConfig);
        }
        //异步调用 生成静态文件上传到minio中
//        articleFreemarkerService.buildArticleToMinIO(apArticle,articleDto.getContent());
        //异步调用 生成静态文件上传到OSS中
        articleFreemarkerService.buildArticleToOSS(apArticle,articleDto.getContent());
        //3.结果返回 文章的id
        return ResponseResult.okResult(apArticle.getId());
    }

    /**
     * 更新文章的分值  同时更新缓存中的热点文章数据
     * @param mess
     */
    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        //1.更新文章的阅读、点赞、收藏、评论的数量
        ApArticle apArticle = updateArticle(mess);
        //2.计算文章的分值
        Integer score = computeScore(apArticle);
        score = score * 3;
        //3.替换当前文章对应频道的热点数据
        replaceDataToRedis(apArticle,score,ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId());
        //4.替换推荐对应的热点数据
        replaceDataToRedis(apArticle,score,ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);

    }

    /**
     * 替换数据并且存入到redis
     * @param apArticle
     * @param score
     * @param s
     */
    private void replaceDataToRedis(ApArticle apArticle, Integer score, String s) {
        String articleListStr = cacheService.get(s);
        if (StringUtils.isNoneBlank(articleListStr)){
            List<HotArticleVo> hotArticleVoList = JSON.parseArray(articleListStr, HotArticleVo.class);
            boolean flag = true;
            //如果缓存中存在该文章，只更新分值
            for (HotArticleVo hotArticleVo : hotArticleVoList) {
                if (hotArticleVo.getId().equals(apArticle.getId())){
                    hotArticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }
            //如果缓存中不存在，查询缓存中分值最小的一条数据，进行分值的比较，如果当前文章的分值大于缓存中的数据，就替换
            if (flag){
                if (hotArticleVoList.size() >= 30){
                    hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
                    HotArticleVo lastHot = hotArticleVoList.get(hotArticleVoList.size() - 1);
                    if (lastHot.getScore() < score){
                        hotArticleVoList.remove(lastHot);
                        HotArticleVo hot = new HotArticleVo();
                        BeanUtils.copyProperties(apArticle,hot);
                        hot.setScore(score);
                        hotArticleVoList.add(hot);
                    }
                }
            }
            else {
                HotArticleVo hot = new HotArticleVo();
                BeanUtils.copyProperties(apArticle,hot);
                hot.setScore(score);
                hotArticleVoList.add(hot);
            }
            //缓存到redis
            hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
            cacheService.set(s,JSON.toJSONString(hotArticleVoList));
        }
    }

    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if (apArticle.getLikes() != null){
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null){
            score += apArticle.getViews();
        }
        if (apArticle.getComment() != null){
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null){
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;
    }

    /**
     * 更新文章行为数量
     * @param mess
     * @return
     */
    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        apArticle.setCollection(apArticle.getCollection()==null?0:apArticle.getCollection()+mess.getCollect());
        apArticle.setComment(apArticle.getComment()==null?0:apArticle.getComment()+mess.getComment());
        apArticle.setLikes(apArticle.getLikes()==null?0:apArticle.getLikes()+mess.getLike());
        apArticle.setViews(apArticle.getViews()==null?0:apArticle.getViews()+mess.getView());
        updateById(apArticle);
        return apArticle;
    }
}
