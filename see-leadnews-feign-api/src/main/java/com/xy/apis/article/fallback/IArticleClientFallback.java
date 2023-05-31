package com.xy.apis.article.fallback;

import com.xy.model.article.dtos.ArticleDto;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.common.enums.AppHttpCodeEnum;
import com.xy.apis.article.IArticleClient;
import org.springframework.stereotype.Component;

/**
 * feign失败配置
 * @author 杨路恒
 */
@Component
public class IArticleClientFallback implements IArticleClient {
    @Override
    public ResponseResult saveArticle(ArticleDto articleDto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取数据失败");
    }
}
