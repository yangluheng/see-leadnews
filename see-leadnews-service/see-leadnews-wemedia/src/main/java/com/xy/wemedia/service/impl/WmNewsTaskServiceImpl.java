package com.xy.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.common.enums.TaskTypeEnum;
import com.xy.model.schedule.dtos.Task;
import com.xy.model.wemedia.pojos.WmNews;
import com.xy.utils.common.ProtostuffUtil;
import com.xy.wemedia.service.WmNewsAutoScanService;
import com.xy.wemedia.service.WmNewsTaskService;
import com.xy.apis.schedule.IScheduleClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author 杨路恒
 */
@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private IScheduleClient iScheduleClient;
    /**
     * 添加任务到延迟队列中
     * @param id    文章id
     * @param publishTime   发布时间，可以作为任务的执行时间
     */
    @Async
    @Override
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延迟服务中---begin");
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));
        iScheduleClient.addTask(task);
        log.info("添加任务到延迟服务中---end");
    }
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    /**
     * 消费任务，审核文章
     */
    @Scheduled(fixedRate = 1000)
    @Override
//    @SneakyThrows
    public void scanNewsByTask() {
        log.info("开始消费任务，审核文章");
        ResponseResult responseResult = iScheduleClient.pull(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if (responseResult.getCode() == 200 && responseResult.getData() != null){
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
            System.out.println(wmNews.getId());
        }
        log.info("结束消费任务，审核文章");
    }
}
