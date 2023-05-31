package com.xy.apis.article;

import com.xy.model.article.dtos.ArticleDto;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.apis.article.fallback.IArticleClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 杨路恒
 */
@FeignClient(value = "leadnews-article",fallback = IArticleClientFallback.class)
public interface IArticleClient {
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto articleDto);;
}
