package com.xy.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.user.dtos.LoginDto;
import com.xy.model.user.pojos.ApUser;

/**
 * @author 杨路恒
 */
public interface ApUserService extends IService<ApUser> {
    /**
     * app端登录功能
     * @param loginDto
     * @return
     */
    public ResponseResult login(LoginDto loginDto);
}
