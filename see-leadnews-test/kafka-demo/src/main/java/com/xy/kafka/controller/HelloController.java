package com.xy.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.xy.kafka.pojo.User;
import org.rocksdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author 杨路恒
 */
@RestController
public class HelloController {
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    @GetMapping("/hello")
    public String hello(){
        User user = new User();
        user.setUsername("杨大大");
        user.setAge(24);
        kafkaTemplate.send("user_topic", JSON.toJSONString(user));
//        kafkaTemplate.send("test_topic","看电视");
        return "ok";
    }
}
