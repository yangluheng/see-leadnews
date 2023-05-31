package com.xy.xxljob.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class HelloJob {


    @XxlJob("demo-JobHandler")
    public void helloJob(){
        System.out.println("简单任务执行了。。。。");
    }
}