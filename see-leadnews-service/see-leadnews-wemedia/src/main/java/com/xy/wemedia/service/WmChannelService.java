package com.xy.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.pojos.WmChannel;
import org.springframework.stereotype.Service;

/**
 * @author 杨路恒
 */
public interface WmChannelService extends IService<WmChannel> {
    /**
     * 查询所有频道
     * @return
     */
    public ResponseResult findAll();
}
