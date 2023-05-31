package com.xy.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.admin.dtos.AdUserDto;
import com.xy.model.admin.pojos.AdUser;
import com.xy.model.common.dtos.ResponseResult;

public interface AdUserService extends IService<AdUser> {

    /**
     * 登录
     * @param dto
     * @return
     */
    public ResponseResult login(AdUserDto dto);
}
