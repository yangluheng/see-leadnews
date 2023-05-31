package com.xy.search.service;

import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.search.dtos.UserSearchDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 杨路恒
 */
public interface ArticleSearchService {
    /**
     * ES文章分页搜索
     * @param userSearchDto
     * @return
     * @throws Exception
     */
    public ResponseResult search(UserSearchDto userSearchDto) throws Exception;
}
