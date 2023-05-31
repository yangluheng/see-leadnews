package com.xy.wemedia.service;


import java.util.Date;

/**
 * @author 杨路恒
 */
public interface WmNewsTaskService {
    /**
     * 添加任务到延迟队列中
     * @param id    文章id
     * @param publishTime   发布时间，可以作为任务的执行时间
     */
    public void addNewsToTask(Integer id, Date publishTime);

    /**
     * 消费任务，审核文章
     */
    public void scanNewsByTask();
}
