package org.bordylek.mon;

import org.bordylek.mon.model.Alert;
import org.bordylek.mon.model.Severity;
import org.bordylek.mon.repository.AlertRepository;
import org.bordylek.service.model.Metrics;
import org.bordylek.service.repository.MetricsRepository;
import org.drools.definition.rule.Rule;
import org.drools.runtime.StatelessKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AlertProcessor implements Alerter {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private StatelessKnowledgeSession droolsSession;

    private static final Logger LOG = LoggerFactory.getLogger(AlertProcessor.class);

    public void process() {
        Metrics latestMetric = metricsRepository.findTopByOrderByTimestampDesc();
        if (latestMetric != null) {
            droolsSession.setGlobal("alerter", this);
            droolsSession.execute(metricsRepository.findByTimestamp(latestMetric.getTimestamp()));
        }
    }

    @Override
    public void info(Rule rule, Date date, String message) {
        createAlert(rule, date, Severity.INFO, message);
    }

    @Override
    public void warning(Rule rule, Date date, String message) {
        createAlert(rule, date, Severity.WARNING, message);
    }

    @Override
    public void error(Rule rule, Date date, String message) {
        createAlert(rule, date, Severity.ERROR, message);
    }

    private void createAlert(Rule rule, Date date, Severity severity, String message) {
        String fqName = rule.getPackageName() + "." + rule.getName();
        if (alertRepository.findByFqNameAndTimestampAndSeverity(fqName, date, severity).size() == 0) {
            alertRepository.save(new Alert(fqName, date, severity, message));
        } else LOG.warn("Duplicite alert " + fqName + " severity " + severity + ": " + message);
    }

}
