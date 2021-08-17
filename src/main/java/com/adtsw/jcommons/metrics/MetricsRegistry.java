package com.adtsw.jcommons.metrics;

import java.util.concurrent.ConcurrentHashMap;

public class MetricsRegistry {

    private final ConcurrentHashMap<String, Long> startTimings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TimerValue> timings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CounterValue> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StatValue> stats = new ConcurrentHashMap<>();

    private static class RegistryHolder {
        public static MetricsRegistry instance = new MetricsRegistry();
    }

    public static MetricsRegistry getInstance() {
        return RegistryHolder.instance;
    }

    public void setStat(String statName, long statValue) {
        stats.put(statName, new StatValue(statValue));
    }

    public void clearStat(String statName) {
        stats.remove(statName);
    }
    
    public void clearStats() {
        stats.clear();
    }

    public void incrementCounter(String counterName) {
        incrementCounter(counterName, 1L);
    }

    public void incrementCounter(String counterName, Long incrementValue) {
        counters.compute(counterName, (key, currentValue) -> currentValue == null ?
            new CounterValue(incrementValue) :
            new CounterValue(currentValue.getVal() + incrementValue));
    }

    public void clearCounter(String counterName) {
        counters.remove(counterName);
    }

    public void clearCounters() {
        counters.clear();
    }

    public TimerValue getTimer(String timerName) {
        return timings.getOrDefault(timerName, new TimerValue(0L, 0L, 0L));
    }

    public void startTimer(String timerName) {
        startTimings.put(Thread.currentThread().getId() + "_" + timerName, System.currentTimeMillis());
    }
    
    public void stopTimer(String timerName) {
        String threadTimerName = Thread.currentThread().getId() + "_" + timerName;
        Long startTime = startTimings.get(threadTimerName);
        if(startTime != null) {
            long timeTaken = System.currentTimeMillis() - startTime;
            startTimings.remove(threadTimerName);
            timings.compute(timerName, (key, currentValue) -> {
                return currentValue == null ? 
                    new TimerValue(timeTaken, timeTaken, timeTaken) :
                    new TimerValue(
                        Math.max(currentValue.getMax(), timeTaken),
                        Math.min(currentValue.getMin(), timeTaken),
                        (long) ((currentValue.getAvg() + timeTaken)/(2.0))
                    );
            });
        }
    }

    public void clearTimer(String timerName) {
        startTimings.remove(timerName);
        timings.remove(timerName);
    }
    
    public void clearTimers() {
        startTimings.clear();
        timings.clear();
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        counters.forEach((counterName, counterValue) -> {
            appendMetricValue(sb, counterName + " [val] ", counterValue.getVal());
        });
        stats.forEach((statName, statValue) -> {
            appendMetricValue(sb, statName + " [val] ", statValue.getVal());
        });
        timings.forEach((timerName, timeTaken) -> {
            appendMetricValue(sb, timerName + " [max] ", timeTaken.getMax());
            appendMetricValue(sb, timerName + " [min] ", timeTaken.getMin());
            appendMetricValue(sb, timerName + " [avg] ", timeTaken.getAvg());
        });
        return sb.toString();
    }

    private void appendMetricValue(StringBuilder sb, String metric, Long metricValue) {
        sb.append(metric);
        sb.append(":");
        sb.append(metricValue);
        sb.append("\n");
    }
}
