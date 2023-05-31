package com.xy.search.service;

import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.search.dtos.UserSearchDto;

/**
 * @author 杨路恒
 */
public interface ApAssociateWordsService {
    /**
     *  联想词
     * @param userSearchDto
     * @return
     */
    public ResponseResult search(UserSearchDto userSearchDto);
}
