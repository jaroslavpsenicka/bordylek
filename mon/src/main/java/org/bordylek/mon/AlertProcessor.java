package org.bordylek.mon;

import org.bordylek.service.model.Metrics;
import org.bordylek.service.repository.MetricsRepository;
import org.drools.runtime.StatelessKnowledgeSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertProcessor {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private StatelessKnowledgeSession droolsSession;

    @Autowired
    private Alerter alerter;

    public void process() {
        Metrics latestMetric = metricsRepository.findTopByOrderByTimestampDesc();
        if (latestMetric != null) {
            droolsSession.setGlobal("alerter", alerter);
            droolsSession.execute(metricsRepository.findByTimestamp(latestMetric.getTimestamp()));
        }
    }

}
