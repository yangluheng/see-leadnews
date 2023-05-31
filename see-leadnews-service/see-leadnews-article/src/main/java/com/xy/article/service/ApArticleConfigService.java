package com.xy.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.article.pojos.ApArticleConfig;

import java.util.Map;

/**
 * @author 杨路恒
 */
public interface ApArticleConfigService extends IService<ApArticleConfig> {
    /**
     * 修改文章配置
     * @param map
     */
    public void updateByMap(Map map);
}
