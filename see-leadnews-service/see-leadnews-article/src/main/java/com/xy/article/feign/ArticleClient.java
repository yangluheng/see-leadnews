package com.xy.article.feign;

import com.xy.model.article.dtos.ArticleDto;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.apis.article.IArticleClient;
import com.xy.article.service.ApArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 杨路恒
 */
@RestController
public class ArticleClient implements IArticleClient {
    @Autowired
    private ApArticleService apArticleService;
    /**
     *
     * @param articleDto
     * @return
     */
    @PostMapping("/api/v1/article/save")
    @Override
    public ResponseResult saveArticle(@RequestBody ArticleDto articleDto) {

        return apArticleService.saveArticle(articleDto);
    }
}
