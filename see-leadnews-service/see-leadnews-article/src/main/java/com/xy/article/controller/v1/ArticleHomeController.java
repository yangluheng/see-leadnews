package com.xy.article.controller.v1;

import com.xy.common.constants.ArticleConstants;
import com.xy.model.article.dtos.ArticleHomeDto;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.article.service.ApArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 杨路恒
 */
@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {

    @Autowired
    private ApArticleService apArticleService;
    /**
     * 加载首页
     * @param articleHomeDto
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto articleHomeDto){
//        return apArticleService.load(ArticleConstants.LOADTYPE_LOAD_MORE,articleHomeDto);
        return apArticleService.load2(ArticleConstants.LOADTYPE_LOAD_MORE,articleHomeDto,true);
    }

    /**
     * 加载更多
     * @param articleHomeDto
     * @return
     */
    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto articleHomeDto){
        return apArticleService.load(ArticleConstants.LOADTYPE_LOAD_MORE,articleHomeDto);
    }

    /**
     * 加载最新
     * @param articleHomeDto
     * @return
     */
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto articleHomeDto){
        return apArticleService.load(ArticleConstants.LOADTYPE_LOAD_NEW,articleHomeDto);
    }
}
