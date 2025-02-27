package com.adtsw.jcommons.utils;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class ScheduledTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(ScheduledTask.class);
    private final String taskName;
    private final Task actor;
    private final int variationInSeconds;
    private final int timeoutInSeconds;

    @Override
    public void run() {
        if(variationInSeconds > 1) {
            try {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                Thread.sleep(random.nextInt(1, variationInSeconds + 1) * 1000L);
            } catch (InterruptedException e) {
                logger.info("Interruption while sleeping", e);
            }
        }
        logger.info("Running scheduled task " + taskName);

        try {
            Thread taskThread = new Thread(actor::execute);
            taskThread.start();
            long startTs = System.currentTimeMillis();
            taskThread.join(timeoutInSeconds * 1000L);
            if(taskThread.isAlive()) {
                logger.warn("Terminating task " + taskName + " due to timeout");
                taskThread.interrupt();
                actor.onTimeout();
            };
            long endTs = System.currentTimeMillis();
            logger.info("Task " + taskName + " took " + (endTs - startTs) + " ms");
        } catch (InterruptedException e) {
            logger.warn("Terminating task " + taskName + " due to interruption");
        }
    }
}
