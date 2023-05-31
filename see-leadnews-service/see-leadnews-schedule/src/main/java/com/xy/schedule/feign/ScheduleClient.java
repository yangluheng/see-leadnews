package com.xy.schedule.feign;

import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.schedule.dtos.Task;
import com.xy.schedule.service.TaskService;
import com.xy.apis.schedule.IScheduleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 杨路恒
 */
@RestController
public class ScheduleClient implements IScheduleClient {
    @Autowired
    private TaskService taskService;
    /**
     * 添加任务
     * @param task   任务对象
     * @return
     */
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task) {
        return ResponseResult.okResult(taskService.addTask(task));
    }

    /**
     * 取消任务
     * @param taskId        任务id
     * @return
     */
    @GetMapping("/api/v1/task/cancel/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId) {
        return ResponseResult.okResult(taskService.cancelTask(taskId));
    }

    /**
     * 按照类型和优先级来拉取任务
     * @param type
     * @param priority
     * @return
     */
    @GetMapping("/api/v1/task/pull/{type}/{priority}")
    public ResponseResult pull(@PathVariable("type") int type,@PathVariable("priority")  int priority) {
        return ResponseResult.okResult(taskService.pull(type,priority));
    }
}
