package com.xy.article.listener;

import com.alibaba.fastjson.JSON;
import com.xy.common.constants.HotArticleConstants;
import com.xy.model.mess.ArticleVisitStreamMess;
import com.xy.article.service.ApArticleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author 杨路恒
 */
@Component
@Slf4j
public class ArticleIncrHandleListener {
    @Autowired
    private ApArticleService apArticleService;
    @KafkaListener(topics = HotArticleConstants.HOT_ARTICLE_INCR_HANDLE_TOPIC)
    public void onMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            ArticleVisitStreamMess articleVisitStreamMess = JSON.parseObject(message, ArticleVisitStreamMess.class);
            apArticleService.updateScore(articleVisitStreamMess);
        }
    }
}
