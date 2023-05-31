package com.xy.schedule.test;

import com.alibaba.fastjson.JSON;
import com.xy.common.redis.CacheService;
import com.xy.model.schedule.dtos.Task;
import com.xy.schedule.ScheduleApplication;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {
    @Autowired
    private CacheService cacheService;

    @Test
    public void testList(){
        //在list的左边添加元素
        cacheService.lLeftPush("list_01","hello,redis");
        //在list的右边添加元素
        cacheService.lRightPush("list_02","hello,redis");
        //在list的右边获取元素，并删除
        String list_02 = cacheService.lRightPop("list_02");
        System.out.println(list_02);
    }

    @Test
    public void testZset(){
        //添加数据到zset中 分值
        cacheService.zAdd("zset_key_01","hello zset 01",1000);
        cacheService.zAdd("zset_key_01","hello zset 02",8000);
        cacheService.zAdd("zset_key_01","hello zset 03",7000);
        cacheService.zAdd("zset_key_01","hello zset 04",99999);
        //按照分值获取数据
        Set<String> zset_key_01 = cacheService.zRangeByScore("zset_key_01", 0, 8888);
        System.out.println(zset_key_01);
    }

    @Test
    public void testKeys(){
        Set<String> keys = cacheService.keys("future_*");
        System.out.println(keys);
        Set<String> scan = cacheService.scan("future_*");
        System.out.println(scan);
    }

    //耗时7278
    @Test
    public  void testPiple1(){
        long start =System.currentTimeMillis();
        for (int i = 0; i <10000 ; i++) {
            Task task = new Task();
            task.setTaskType(1001);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime());
            cacheService.lLeftPush("1001_1", JSON.toJSONString(task));
        }
        System.out.println("耗时"+(System.currentTimeMillis()- start));
    }


    @Test
    public void testPiple2(){
        long start  = System.currentTimeMillis();
        //使用管道技术
        List<Object> objectList = cacheService.getstringRedisTemplate().executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                for (int i = 0; i <10000 ; i++) {
                    Task task = new Task();
                    task.setTaskType(1001);
                    task.setPriority(1);
                    task.setExecuteTime(new Date().getTime());
                    redisConnection.lPush("1001_1".getBytes(), JSON.toJSONString(task).getBytes());
                }
                return null;
            }
        });
        System.out.println("使用管道技术执行10000次自增操作共耗时:"+(System.currentTimeMillis()-start)+"毫秒");
    }

}
