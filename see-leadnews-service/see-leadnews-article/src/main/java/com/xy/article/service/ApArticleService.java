package com.xy.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.article.dtos.ArticleDto;
import com.xy.model.article.dtos.ArticleHomeDto;
import com.xy.model.article.pojos.ApArticle;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.mess.ArticleVisitStreamMess;
import org.springframework.stereotype.Service;

/**
 * @author 杨路恒
 */
public interface ApArticleService extends IService<ApArticle> {
    /**
     * 根据参数加载文章列表
     * @param loadType  1 加载更多 2 加载最新
     * @param articleHomeDto
     * @return
     */
    public ResponseResult load(Short loadType, ArticleHomeDto articleHomeDto);

    /**
     * 根据参数加载文章列表
     * @param loadType
     * @param articleHomeDto
     * @param firstPage
     * @return
     */
    public ResponseResult load2(Short loadType, ArticleHomeDto articleHomeDto, boolean firstPage);

    /**
     * 保存app端相关文章
     * @param articleDto
     * @return
     */
    public ResponseResult saveArticle(ArticleDto articleDto);

    /**
     * 更新文章的分值  同时更新缓存中的热点文章数据
     * @param mess
     */
    public void updateScore(ArticleVisitStreamMess mess);
}
