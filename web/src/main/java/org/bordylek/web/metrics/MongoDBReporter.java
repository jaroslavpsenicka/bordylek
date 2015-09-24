
package org.bordylek.web.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import org.bordylek.service.model.*;
import org.bordylek.service.repository.LogRepository;
import org.bordylek.service.repository.MetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class MongoDBReporter extends ScheduledReporter implements InitializingBean {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogHistoryAppender logHistoryAppender;

    private Long period;
    private TimeUnit timeUnit;
    private Map<String, Long> lastValues;

    private static Logger LOG = LoggerFactory.getLogger(MongoDBReporter.class);

    private MongoDBReporter(MetricRegistry registry, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
        super(registry, "mongodb-reporter", filter, rateUnit, durationUnit);
        lastValues = new HashMap<>();
    }

    @Required
    public void setPeriod(Long period) {
        this.period = period;
    }

    @Required
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.start(period, timeUnit);
    }

    @Override
    public void report(SortedMap<String, com.codahale.metrics.Gauge> gauges, SortedMap<String, com.codahale.metrics.Counter> counters, SortedMap<String, com.codahale.metrics.Histogram> histograms,
        SortedMap<String, com.codahale.metrics.Meter> meters, SortedMap<String, com.codahale.metrics.Timer> timers) {

        Date saveDate = new Date();
        Log log = new Log(logHistoryAppender.getLog(), saveDate);

        LOG.debug("Saving metrics: " + gauges.size() + " gauges, " + counters.size() + " counters, " +
            histograms.size() + " histograms, " + meters.size() + " meters, " + timers.size() + " timers, " +
            log.getMessage().length() + " bytes of log.");

        try {
            logRepository.save(log);
        } catch (Exception ex) {
            LOG.error("Error writing log", ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Gauge> entry : gauges.entrySet()) try {
            Gauge entity = new Gauge(entry.getKey(), entry.getValue(), saveDate);
            entity.setLogId(log.getId());
            metricsRepository.save(entity);
        } catch (Exception ex) {
            LOG.error("Error writing gauge " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Counter> entry : counters.entrySet()) try {
            Counter entity = new Counter(entry.getKey(), entry.getValue(), saveDate);
            entity.setLogId(log.getId());
            metricsRepository.save(entity);
        } catch (Exception ex) {
            LOG.error("Error writing counter " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Histogram> entry : histograms.entrySet()) try {
            Histogram entity = new Histogram(entry.getKey(), entry.getValue().getSnapshot(), saveDate);
            entity.setLogId(log.getId());
            metricsRepository.save(entity);
        } catch (Exception ex) {
            LOG.error("Error writing histogram " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Meter> entry : meters.entrySet()) try {
            Meter entity = new Meter(entry.getKey(), entry.getValue(), saveDate);
            entity.setDiff(calculateDiff("meter." + entry.getKey(), entity.getCount()));
            entity.setLogId(log.getId());
            metricsRepository.save(entity);
        } catch (Exception ex) {
            LOG.error("Error writing meter " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Timer> entry : timers.entrySet()) try {
            Timer entity = new Timer(entry.getKey(), entry.getValue(), saveDate);
            entity.setDiff(calculateDiff("timer." + entry.getKey(), entity.getCount()));
            entity.setLogId(log.getId());
            metricsRepository.save(entity);
        } catch (Exception ex) {
            LOG.error("Error writing timer " + entry, ex);
        }
    }

    private Long calculateDiff(String key, Long count) {
        Long lastValue = lastValues.get(key);
        lastValues.put(key, count);
        return count - (lastValue != null ? lastValue : 0);
    }

}
