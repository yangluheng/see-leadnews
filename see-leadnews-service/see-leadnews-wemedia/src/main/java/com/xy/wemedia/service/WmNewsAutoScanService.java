package com.xy.wemedia.service;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 杨路恒
 */
public interface WmNewsAutoScanService {
    /**
     * 自媒体文章审核
     * @param id    自媒体文章id
     */
    public void autoScanWmNews(Integer id);
}
