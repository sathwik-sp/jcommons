package com.adtsw.jcommons.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskUtilTest {

    @Test
    public void testScheduleTask() {

        AtomicInteger interruptionCount = new AtomicInteger(0);
        AtomicInteger taskCompletionCount = new AtomicInteger(0);
    
        TaskUtil.scheduleTask(
            "task1", 0, 1, 0, 2,
            TimeUnit.SECONDS, new Task() {
                @Override
                public void execute() {
                    try {
                        Thread.sleep(4 * 1000);
                    } catch (InterruptedException e) {
                        interruptionCount.incrementAndGet();
                    }
                    taskCompletionCount.incrementAndGet();
                }
            });

        try {
            Thread.sleep(11 * 1000);
            TaskUtil.cancelScheduledTask("task1");
            Assert.assertEquals(5, interruptionCount.get());
            Assert.assertEquals(5, taskCompletionCount.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
