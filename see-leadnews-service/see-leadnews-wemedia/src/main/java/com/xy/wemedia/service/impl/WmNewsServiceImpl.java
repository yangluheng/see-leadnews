package com.xy.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.constants.WemediaConstants;
import com.xy.common.constants.WmNewsMessageConstants;
import com.xy.common.exception.CustomException;
import com.xy.model.common.dtos.PageResponseResult;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.common.enums.AppHttpCodeEnum;
import com.xy.model.wemedia.dtos.WmNewsDto;
import com.xy.model.wemedia.dtos.WmNewsPageReqDto;
import com.xy.model.wemedia.pojos.WmMaterial;
import com.xy.model.wemedia.pojos.WmNews;
import com.xy.model.wemedia.pojos.WmNewsMaterial;
import com.xy.model.wemedia.pojos.WmUser;
import com.xy.utils.thread.WmThreadLocalUtil;
import com.xy.wemedia.mapper.WmMaterialMapper;
import com.xy.wemedia.mapper.WmNewsMapper;
import com.xy.wemedia.mapper.WmNewsMaterialMapper;
import com.xy.wemedia.service.WmNewsAutoScanService;
import com.xy.wemedia.service.WmNewsService;
import com.xy.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 杨路恒
 */
@Service
@Transactional
@Slf4j
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    /**
     * 查询文章
     * @param wmNewsPageReqDto
     * @return
     */
    @Override
    public ResponseResult findAll(WmNewsPageReqDto wmNewsPageReqDto) {
        //1.检查参数
        if (wmNewsPageReqDto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页参数查询
        wmNewsPageReqDto.checkParam();
        //获取当前登录人的信息
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //2.分页条件查询
        IPage page = new Page(wmNewsPageReqDto.getPage(), wmNewsPageReqDto.getSize());
        LambdaQueryWrapper<WmNews> wmNewsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //状态精确查询
        if (wmNewsPageReqDto.getStatus() != null){
            wmNewsLambdaQueryWrapper.eq(WmNews::getStatus,wmNewsPageReqDto.getStatus());
        }
        //频道精确查询
        if (wmNewsPageReqDto.getChannelId() != null){
            wmNewsLambdaQueryWrapper.eq(WmNews::getChannelId,wmNewsPageReqDto.getChannelId());
        }
        //时间范围查询
        if (wmNewsPageReqDto.getBeginPubDate() != null && wmNewsPageReqDto.getEndPubDate() != null){
            wmNewsLambdaQueryWrapper.between(WmNews::getPublishTime,wmNewsPageReqDto.getBeginPubDate(),wmNewsPageReqDto.getEndPubDate());
        }
        //关键字模糊查询
        if (StringUtils.isNoneBlank(wmNewsPageReqDto.getKeyword())){
            wmNewsLambdaQueryWrapper.like(WmNews::getTitle,wmNewsPageReqDto.getKeyword());
        }
        //查询当前登录用户的文章
        wmNewsLambdaQueryWrapper.eq(WmNews::getUserId,user.getId());
        //发布时间倒序查询
        wmNewsLambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);
        page = page(page,wmNewsLambdaQueryWrapper);
        //3.结果返回
        PageResponseResult pageResponseResult = new PageResponseResult(wmNewsPageReqDto.getPage(), wmNewsPageReqDto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private WmNewsTaskService wmNewsTaskService;
    /**
     * 发布修改文章或保存为草稿
     * @param wmNewsDto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto wmNewsDto) {
        //1.检查参数
        if (wmNewsDto == null || wmNewsDto.getContent() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.保存或修改文章
        LambdaQueryWrapper<WmNewsMaterial> wmNewsMaterialLambdaQueryWrapper = new LambdaQueryWrapper<>();
        WmNews wmNews = new WmNews();
        //属性拷贝 属性名称和类型相同才能拷贝
        BeanUtils.copyProperties(wmNewsDto,wmNews);
        ///封面图片 list ==>String
        if (wmNewsDto.getImages() != null && wmNewsDto.getImages().size() > 0){
            //[a.jpg,b.jpg]-->   a.jpg,b.jpg
            String imageStr = StringUtils.join(wmNewsDto.getImages(), ",");
            wmNews.setImages(imageStr);
        }
        //如果当前封面类型为自动 -1
        if (wmNewsDto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);
        //3.判断是否为草稿 如果为草稿结束当前方法
        if (wmNews.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        //4.不是草稿，保存文章内容图片与素材的关系
        //获取到文章内容中的图片信息
        List<String> wmNewsMaterials = extractUrlInfo(wmNewsDto.getContent());
        saveRelativeInfoForContent(wmNewsMaterials,wmNews.getId());
        //5.不是草稿，保存文章封面图片与素材的关系，如果当前布局是自动，需要匹配封面图片
        saveRelativeInfoForCover(wmNewsDto,wmNews,wmNewsMaterials);
        System.out.println(wmNewsMaterials);
        //审核文章
//        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        wmNewsTaskService.addNewsToTask(wmNews.getId(),wmNews.getPublishTime());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    /**
     * 文章的上下架
     * @param wmNewsDto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto wmNewsDto) {
        //1.检查参数
        if (wmNewsDto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.查询文章
        WmNews wmNews = getById(wmNewsDto.getId());
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //3.判断文章是否已发布
        if (!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"当前文章不是发布状态，不能上下架");
        }
        //4.修改文章enable
        if (wmNewsDto != null && wmNewsDto.getEnable() > -1 && wmNewsDto.getEnable() < 2){
            update(Wrappers.<WmNews>lambdaUpdate().set(WmNews::getEnable,wmNewsDto.getEnable()).eq(WmNews::getId,wmNews.getId()));
            //发送消息，通知article端修改文章配置
            if (wmNews.getArticleId() != null){
                Map<String,Object> map = new HashMap<>();
                map.put("articleId",wmNews.getArticleId());
                map.put("enable",wmNews.getEnable());
                kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC,JSON.toJSONString(map));
            }
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除发布文章或草稿
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteNews(Integer id) {
        //1.检查参数
        //2.查询文章
        WmNews wmNews = getById(id);
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //3.删除文章
        wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
        removeById(wmNews.getId());
        System.out.println("删除");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 编辑发布文章或草稿
     * @param id
     * @return
     */
    @Override
    public ResponseResult editNews(Integer id) {
        //1.检查参数
        //2.查询文章
        WmNews wmNews = getById(id);
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        WmNewsDto wmNewsDto = new WmNewsDto();
        BeanUtils.copyProperties(wmNews,wmNewsDto);
        //2.保存或修改文章
        return submitNews(wmNewsDto);
    }

    /**
     * 处理文章内容图片与素材的关系
     * @param wmNewsMaterials
     * @param id
     */
    private void saveRelativeInfoForContent(List<String> wmNewsMaterials, Integer id) {
        saveRelativeInfo(wmNewsMaterials,id,WemediaConstants.WM_CONTENT_REFERENCE);
    }

    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 保存文章图片与素材的关系到数据库中
     * @param wmNewsMaterials
     * @param id
     * @param wmContentReference
     */
    private void saveRelativeInfo(List<String> wmNewsMaterials, Integer id, Short wmContentReference) {
        if (wmNewsMaterials != null && !wmNewsMaterials.isEmpty()) {
            //通过图片的url查询素材的id
            List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, wmNewsMaterials));
            //判断素材是否有效
            if(wmMaterials == null || wmMaterials.size() == 0){
                //手动抛出异常   第一个功能：能够提示调用者素材失效了，第二个功能，进行数据的回滚
                throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }
            if (wmNewsMaterials.size() != wmMaterials.size()){
                throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }
            List<Integer> idList = wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
            //批量保存
            wmNewsMaterialMapper.saveRelations(idList,id,wmContentReference);
        }
    }

    /**
     *第一个功能：如果当前封面类型为自动，则设置封面类型的数据
     *      匹配规则：
     *       1，如果内容图片大于等于1，小于3  单图  type 1
     *       2，如果内容图片大于等于3  多图  type 3
     *       3，如果内容没有图片，无图  type 0
     *
     * 第二个功能：保存封面图片与素材的关系
     * @param wmNewsDto
     * @param wmNews
     * @param wmNewsMaterials
     */
    private void saveRelativeInfoForCover(WmNewsDto wmNewsDto, WmNews wmNews, List<String> wmNewsMaterials) {
        List<String> images = wmNewsDto.getImages();
        //如果当前封面类型为自动，则设置封面类型的数据
        if (wmNewsDto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            //多图
            if (wmNewsMaterials.size() >= 3){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = wmNewsMaterials.stream().limit(3).collect(Collectors.toList());
            } else if (wmNewsMaterials.size() >= 1 && wmNewsMaterials.size() < 3) {
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = wmNewsMaterials.stream().limit(1).collect(Collectors.toList());
            }
            else {
                //无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
        }
        //修改文章
        if (images != null && images.size() > 0) {
            wmNews.setImages(StringUtils.join(images, ","));
        }
        updateById(wmNews);
        if (images != null && images.size() > 0){
            saveRelativeInfo(images,wmNews.getId(),WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    /**
     * 获取文章内容中的图片信息
     * @param content
     * @return
     */
    private List<String> extractUrlInfo(String content) {
        List<String> materials = new ArrayList<String>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if (map.get("type").equals("image")){
                String imageUrl = (String) map.get("value");
                materials.add(imageUrl);
            }
        }
        return materials;
    }


    /**
     * 保存或修改文章
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        //补全属性
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setPublishTime(new Date());
        wmNews.setEnable((short)1); //默认上架
        if (wmNews.getId() == null){
            //保存
            save(wmNews);
            System.out.println("保存");
        }
        else{
            //修改
            //删除文章图片与素材的关系
            System.out.println(wmNews.getId());
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
            System.out.println("更新");
        }
    }
}
