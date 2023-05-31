package com.xy.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.xy.common.constants.ArticleConstants;
import com.xy.common.redis.CacheService;
import com.xy.model.article.pojos.ApArticle;
import com.xy.model.article.vos.HotArticleVo;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.pojos.WmChannel;
import com.xy.apis.wemedia.IWemediaClient;
import com.xy.article.mapper.ApArticleMapper;
import com.xy.article.service.HotArticleService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
public class HotArticleServiceImpl implements HotArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;
    /**
     * 计算热点文章
     */
    @Override
    public void computeHotArticle() {
        //1.查询前5天的文章数据
        Date date = DateTime.now().minusDays(50).toDate();
        List<ApArticle> articleListByLast5days = apArticleMapper.findArticleListByLast5days(date);
        //2.计算文章的分值
        List<HotArticleVo> hotArticleVoList = computeHotArticle(articleListByLast5days);
        //3.为每个频道缓存30条分值较高的文章
        cacheTagToRedis(hotArticleVoList);

    }
    @Autowired
    private IWemediaClient iWemediaClient;
    @Autowired
    private CacheService cacheService;

    /**
     * 为每个频道缓存30条分值较高的文章
     * @param hotArticleVoList
     */
    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        //为每个频道缓存30条分值较高的文章
        ResponseResult responseResult = iWemediaClient.getChannels();
        if (responseResult.getCode().equals(200)){
            String jsonString = JSON.toJSONString(responseResult.getData());
            List<WmChannel> wmChannels = JSON.parseArray(jsonString, WmChannel.class);
            //检索出每个频道的文章
            if (wmChannels != null && wmChannels.size() > 0){
                for (WmChannel wmChannel : wmChannels) {
                    List<HotArticleVo> hotArticleVos = hotArticleVoList.stream().filter(x -> x.getChannelId().equals(wmChannel.getId())).collect(Collectors.toList());
                    //给文章进行排序，取30条分值较高的文章存入redis  key：频道id   value：30条分值较高的文章
                    sortAndCache(hotArticleVos, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + wmChannel.getId());
                }
            }
        }
        //设置推荐数据
        //给文章进行排序，取30条分值较高的文章存入redis  key：频道id   value：30条分值较高的文章
        sortAndCache(hotArticleVoList,ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);
    }

    /**
     * 排序并且缓存数据
     * @param hotArticleVos
     * @param s
     */
    private void sortAndCache(List<HotArticleVo> hotArticleVos, String s) {
        hotArticleVos = hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
        if (hotArticleVos.size() > 30) {
            hotArticleVos = hotArticleVos.subList(0,30);
        }
        cacheService.set(s,JSON.toJSONString(hotArticleVos));
    }

    /**
     * 计算文章分值
     * @param articleListByLast5days
     * @return
     */
    private List<HotArticleVo> computeHotArticle(List<ApArticle> articleListByLast5days) {
        List<HotArticleVo> hotArticleVoList = new ArrayList<>();
        if (articleListByLast5days != null && articleListByLast5days.size() > 0) {
            for (ApArticle apArticle : articleListByLast5days) {
                HotArticleVo hotArticleVo = new HotArticleVo();
                BeanUtils.copyProperties(apArticle,hotArticleVo);
                Integer score = computeScore(apArticle);
                hotArticleVo.setScore(score);
                hotArticleVoList.add(hotArticleVo);
            }
        }
        return hotArticleVoList;
    }

    /**
     * 计算文章的具体分值
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if (apArticle.getLikes() != null){
            score += apArticle.getLikes() + ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null){
            score += apArticle.getViews();
        }
        if (apArticle.getComment() != null){
            score += apArticle.getComment() + ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null){
            score += apArticle.getCollection() + ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;
    }
}
