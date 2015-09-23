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
import java.util.Set;

@Component
public class AlertProcessor implements Alerter {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private StatelessKnowledgeSession droolsSession;

    @Autowired
    private ConfigurationService configurationService;

    private static final Logger LOG = LoggerFactory.getLogger(AlertProcessor.class);

    public void process() {
        Metrics latestMetric = metricsRepository.findTopByOrderByTimestampDesc();
        if (latestMetric != null) {
            droolsSession.setGlobal("alerter", this);
            droolsSession.execute(metricsRepository.findByTimestamp(latestMetric.getTimestamp()));
        }
    }

    @Override
    public void info(Rule rule, Metrics metrics, String message) {
        createAlert(rule, metrics, Severity.INFO, message);
    }

    @Override
    public void warning(Rule rule, Metrics metrics, String message) {
        createAlert(rule, metrics, Severity.WARNING, message);
    }

    @Override
    public void error(Rule rule, Metrics metrics, String message) {
        createAlert(rule, metrics, Severity.ERROR, message);
    }

    private void createAlert(Rule rule, Metrics metrics, Severity severity, String message) {
        String fqName = rule.getPackageName() + "." + rule.getName();
        Set<String> disabledRules = configurationService.getDisabledRules();
        if (!disabledRules.contains(fqName)) {
            Date date = metrics.getTimestamp();
            if (alertRepository.findByFqNameAndTimestampAndSeverity(fqName, date, severity).size() == 0) {
                Alert entity = new Alert(fqName, severity, message);
                entity.setTimestamp(date);
                entity.setLogId(metrics.getLogId());
                alertRepository.save(entity);
            } else LOG.warn("Duplicite alert " + fqName + " severity " + severity + ": " + message);
        } else LOG.warn("Disabled alert " + fqName + " severity " + severity + ": " + message);
    }

}
