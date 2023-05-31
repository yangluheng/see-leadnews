package com.xy.behavior.service;

import com.xy.model.behavior.dtos.ReadBehaviorDto;
import com.xy.model.common.dtos.ResponseResult;

public interface ApReadBehaviorService {

    /**
     * 保存阅读行为
     * @param dto
     * @return
     */
    public ResponseResult readBehavior(ReadBehaviorDto dto);
}
