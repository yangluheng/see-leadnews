package com.xy.schedule.service.impl;

import com.xy.model.schedule.dtos.Task;
import com.xy.schedule.ScheduleApplication;
import com.xy.schedule.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)

class TaskServiceImplTest {
    @Autowired
    private TaskService taskService;
    @Test
    public void addTask() {
//        Task task = new Task();
//        task.setTaskType(66);
//        task.setPriority(6666);
//        task.setParameters("task test".getBytes());
//        task.setExecuteTime(new Date().getTime() + 500);
//
//        long taskId = taskService.addTask(task);
//        System.out.println(taskId);

        for (int i = 0; i < 5; i++) {
            Task task = new Task();
            task.setTaskType(6 + i);
            task.setPriority(666);
            task.setParameters("task test".getBytes());
            task.setExecuteTime(new Date().getTime() + 600 * i);

            long taskId = taskService.addTask(task);
        }
    }
    @Test
    public void cancelTask(){
        boolean b = taskService.cancelTask(1613139097084854274L);
        System.out.println(b);
    }

    @Test
    public void pullTask(){
        Task task = taskService.pull(6, 666);
        System.out.println(task);
    }
}