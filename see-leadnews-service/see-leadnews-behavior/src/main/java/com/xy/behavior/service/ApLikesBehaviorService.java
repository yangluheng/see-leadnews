package com.xy.behavior.service;

import com.xy.model.behavior.dtos.LikesBehaviorDto;
import com.xy.model.common.dtos.ResponseResult;

public interface ApLikesBehaviorService {

    /**
     * 存储喜欢数据
     * @param dto
     * @return
     */
    public ResponseResult like(LikesBehaviorDto dto);
}
