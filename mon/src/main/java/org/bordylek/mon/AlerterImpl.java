package org.bordylek.mon;

import org.bordylek.mon.model.Alert;
import org.bordylek.mon.model.Severity;
import org.bordylek.mon.repository.AlertRepository;
import org.drools.definition.rule.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlerterImpl implements Alerter {

    @Autowired
    private AlertRepository alertRepository;

    @Override
    public void info(Rule rule, String message) {
        alertRepository.save(new Alert(rule.getPackageName(), rule.getName(), Severity.INFO, message));
    }

    @Override
    public void warning(Rule rule, String message) {
        alertRepository.save(new Alert(rule.getPackageName(), rule.getName(), Severity.WARNING, message));
    }

    @Override
    public void error(Rule rule, String message) {
        alertRepository.save(new Alert(rule.getPackageName(), rule.getName(), Severity.ERROR, message));
    }
}
