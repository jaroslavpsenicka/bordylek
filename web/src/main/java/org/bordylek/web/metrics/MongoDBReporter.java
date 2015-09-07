
package org.bordylek.web.metrics;

import com.codahale.metrics.*;
import org.bordylek.service.model.Counter;
import org.bordylek.service.model.Gauge;
import org.bordylek.service.model.Histogram;
import org.bordylek.service.model.Meter;
import org.bordylek.service.model.Timer;
import org.bordylek.service.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class MongoDBReporter extends ScheduledReporter implements InitializingBean {

//    private GaugeRepository gaugeRepository;
//    private CounterRepository counterRepository;
//    private HistogramRepository histogramRepository;
//    private MeterRepository meterRepository;
//    private TimerRepository timerRepository;

    @Autowired
    private MetricsRepository metricsRepository;

    private Long period;
    private TimeUnit timeUnit;

    private static Logger LOG = LoggerFactory.getLogger(MongoDBReporter.class);

    private MongoDBReporter(MetricRegistry registry, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
        super(registry, "mongodb-reporter", filter, rateUnit, durationUnit);
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

        LOG.debug("Saving metrics");

        for (Map.Entry<String, com.codahale.metrics.Gauge> entry : gauges.entrySet()) try {
            metricsRepository.save(new Gauge(entry.getKey(), entry.getValue()));
        } catch (Exception ex) {
            LOG.error("Error writing gauge " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Counter> entry : counters.entrySet()) try {
            metricsRepository.save(new Counter(entry.getKey(), entry.getValue()));
        } catch (Exception ex) {
            LOG.error("Error writing counter " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Histogram> entry : histograms.entrySet()) try {
            metricsRepository.save(new Histogram(entry.getKey(), entry.getValue().getSnapshot()));
        } catch (Exception ex) {
            LOG.error("Error writing histogram " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Meter> entry : meters.entrySet()) try {
            metricsRepository.save(new Meter(entry.getKey(), entry.getValue()));
        } catch (Exception ex) {
            LOG.error("Error writing meter " + entry, ex);
        }

        for (Map.Entry<String, com.codahale.metrics.Timer> entry : timers.entrySet()) try {
            metricsRepository.save(new Timer(entry.getKey(), entry.getValue()));
        } catch (Exception ex) {
            LOG.error("Error writing timer " + entry, ex);
        }
    }

}
