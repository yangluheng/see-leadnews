package com.xy.article.service;

import com.xy.model.article.pojos.ApArticle;

/**
 * @author 杨路恒
 */
public interface ArticleFreemarkerService {
    /**
     * 生成静态文件上传到minIO中
     * @param apArticle
     * @param content
     */
    public void buildArticleToMinIO(ApArticle apArticle, String content);

    /**
     * 生成静态文件上传到OSS中
     * @param apArticle
     * @param content
     */
    public void buildArticleToOSS(ApArticle apArticle, String content);
}
