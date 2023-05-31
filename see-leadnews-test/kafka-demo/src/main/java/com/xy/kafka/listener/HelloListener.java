package com.xy.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.xy.kafka.pojo.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author 杨路恒
 */
@Component
public class HelloListener {
//    @KafkaListener(topics = "test_topic")
    @KafkaListener(topics = "user_topic")
    public void onMessage(String message){
        if (!StringUtils.isEmpty(message)){
            User user = JSON.parseObject(message, User.class);
//            System.out.println(message);
            System.out.println(user);
        }
    }
}
