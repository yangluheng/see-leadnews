package com.xy.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xy.common.aliyun.GreenImageScan;
import com.xy.common.aliyun.GreenTextScan;
import com.xy.common.aliyun.ImageScan;
import com.xy.common.aliyun.TextScan;
import com.xy.common.tess4j.Tess4jClient;
import com.xy.file.service.FileStorageService;
import com.xy.model.article.dtos.ArticleDto;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.pojos.WmChannel;
import com.xy.model.wemedia.pojos.WmNews;
import com.xy.model.wemedia.pojos.WmSensitive;
import com.xy.model.wemedia.pojos.WmUser;
import com.xy.utils.common.SensitiveWordUtil;
import com.xy.wemedia.mapper.WmChannelMapper;
import com.xy.wemedia.mapper.WmNewsMapper;
import com.xy.wemedia.mapper.WmSensitiveMapper;
import com.xy.wemedia.mapper.WmUserMapper;
import com.xy.wemedia.service.WmNewsAutoScanService;
import com.xy.apis.article.IArticleClient;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 杨路恒
 */
@Service
//@Transactional
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    /**
     * 自媒体文章审核
     * @param id    自媒体文章id
     */
    @Override
    @Async  //标明当前方法是一个异步方法
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null){
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            //从内容中提取纯文本内容和图片
            Map<String,Object> textAndImages = handleTextAndImages(wmNews);
            //自管理的敏感词过滤
            boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
            if(!isSensitive){
                return;
            }
            //2.审核文本内容  阿里云接口
            boolean isTextScan = handleTextScan((String) textAndImages.get("content"),wmNews);
            if (!isTextScan){
                return;
            }
            //3.审核图片  阿里云接口
            boolean isImageScan =  handleImageScan((List<String>) textAndImages.get("images"),wmNews);
//            isImageScan = true;
            if (!isImageScan){
                return;
            }
            //4.审核成功，保存app端的相关的文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }
            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,(short) 9,"审核成功");
            saveAppArticle(wmNews);
        }

    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自管理的敏感词审核
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag =true;
        //获取所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);
        //查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if (map.size() > 0){
            updateWmNews(wmNews, (short) 2,"当前文章中存在违规内容" + map);
            flag = false;
        }
        return flag;
    }

    @Autowired
    private IArticleClient iArticleClient;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     * 修改文章内容
     * @param wmNews
     * @param i
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short i, String reason) {
        wmNews.setStatus(i);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 保存app端相关的文章数据
     * @param wmNews
     * @return
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto articleDto = new ArticleDto();
        //属性的拷贝
        BeanUtils.copyProperties(wmNews,articleDto);
        //文章的布局
        articleDto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null){
            articleDto.setChannelName(wmChannel.getName());
        }
        //作者
        articleDto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null){
            articleDto.setAuthorName(wmUser.getName());
        }
        //设置文章id
        if (wmNews.getArticleId() != null){
            articleDto.setId(wmNews.getArticleId());
        }
        articleDto.setCreatedTime(new Date());
        System.out.println(articleDto.getId());
        ResponseResult responseResult = iArticleClient.saveArticle(articleDto);
        return responseResult;
    }
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private Tess4jClient tess4jClient;
    @Autowired
    private ImageScan imageScan;
    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag = true;
        if (images == null || images.size() == 0){
            return flag;
        }
        //下载图片 minIO
        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());
//        List<byte[]> imageList = new ArrayList<byte[]>();
//        try {
//            for (String image : images) {
//                byte[] bytes = fileStorageService.downLoadFile(image);
//                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
//                BufferedImage imageFile = ImageIO.read(in);
//                String result = tess4jClient.doOCR(imageFile);
//                boolean isSensitive = handleSensitiveScan(result, wmNews);
//                if (!isSensitive){
//                    return isSensitive;
//                }
//                imageList.add(bytes);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        List<String> imageList = images;
        //审核图片
        try {
//            Map map = greenImageScan.imageScan(imageList);
            Map map = imageScan.imageScan(imageList);
            if (map != null){
                //审核失败
                if(map.get("suggestion").equals("block")){
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                }
                //不确定信息  需要人工审核
                else if (map.get("suggestion").equals("review")){
                    flag = false;
                    updateWmNews(wmNews, (short)3,"当前文章中存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private TextScan textScan;

    /**
     * 审核纯文本内容
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        boolean flag = true;
        if ((wmNews.getTitle() + "-" + content).length() == 0){
            return flag;
        }
        try {
            Map map = textScan.greenTextScan(wmNews.getTitle() + "-" + content);
            if (map != null){
                //审核失败
                if(map.get("suggestion").equals("block")){
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                }
                //不确定信息  需要人工审核
                else if (map.get("suggestion").equals("review")){
                    flag = false;
                    updateWmNews(wmNews, (short)3,"当前文章中存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }

        return flag;
    }

    /**
     *  1.从自媒体文章的内容中提取文本和图片
     *  2.提取文章的封面图片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        //存储纯文本内容
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();
        //1.从自媒体文章的内容中提取文本和图片
        if (StringUtils.isNoneBlank(wmNews.getContent())){
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")){
                    stringBuilder.append(map.get("value"));
                }
                if (map.get("type").equals("images")){
                    images.add((String) map.get("value"));
                }
            }
        }
        //2.提取文章的封面图片
        if (StringUtils.isNoneBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("content",stringBuilder.toString());
        resultMap.put("images",images);
        return resultMap;
    }

}
