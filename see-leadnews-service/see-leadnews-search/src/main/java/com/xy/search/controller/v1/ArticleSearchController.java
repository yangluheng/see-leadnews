package com.xy.search.controller.v1;


import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.search.dtos.UserSearchDto;
import com.xy.search.service.ArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 杨路恒
 */
@RestController
@RequestMapping("/api/v1/article/search")
public class ArticleSearchController {
    @Autowired
    private ArticleSearchService articleSearchService;
    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto userSearchDto) throws Exception {
        return articleSearchService.search(userSearchDto);
    }
}
