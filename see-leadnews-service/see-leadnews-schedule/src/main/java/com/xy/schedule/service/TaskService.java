package com.xy.schedule.service;

import com.xy.model.schedule.dtos.Task;

/**
 * @author 杨路恒
 */
public interface TaskService {
    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    public long addTask(Task task);

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    public boolean cancelTask(long taskId);

    /**
     * 按照类型和优先级拉取任务
     * @param type
     * @param priority
     * @return
     */
    public Task pull(int type,int priority);

    /**
     * 未来数据定时刷新
     */
    public void refresh();

    /**
     * 数据库定时任务同步到redis
     */
    public void reloadData();
}
