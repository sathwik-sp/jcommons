package com.adtsw.jcommons.metrics;

import com.adtsw.jcommons.utils.EncryptionUtil;
import org.junit.Assert;
import org.junit.Test;

public class MetricsRegistryTest {

    @Test
    public void testEncryption() throws EncryptionUtil.CryptoException, InterruptedException {

        MetricsRegistry metricsRegistry = new MetricsRegistry();
        metricsRegistry.incrementCounter("c1");
        metricsRegistry.incrementCounter("c1", 2L);
        metricsRegistry.incrementCounter("c1", 3L);

        metricsRegistry.startTimer("t1");
        Thread.sleep(2 * 1000);
        metricsRegistry.stopTimer("t1");
        metricsRegistry.startTimer("t1");
        Thread.sleep(1 * 1000);
        metricsRegistry.stopTimer("t1");
        metricsRegistry.startTimer("t1");
        Thread.sleep(3 * 1000);
        metricsRegistry.stopTimer("t1");
        
        metricsRegistry.setStat("s1", 22L);
        metricsRegistry.setStat("s1", 2L);
        metricsRegistry.setStat("s1", 100L);

        System.out.println(metricsRegistry);
        
        //Assert.assertEquals(plaintext, decryptedString);
    }
}
