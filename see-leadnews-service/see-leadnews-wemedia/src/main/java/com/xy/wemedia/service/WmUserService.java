package com.xy.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.dtos.WmLoginDto;
import com.xy.model.wemedia.pojos.WmUser;

/**
 * @author 杨路恒
 */
public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmLoginDto dto);

}