package org.bordylek.mon;

import org.bordylek.service.model.metrics.Metrics;
import org.drools.definition.rule.Rule;

public interface Alerter {
    void info(Rule rule, Metrics metrics, String message);
    void warning(Rule rule, Metrics metrics, String message);
    void error(Rule rule, Metrics metrics, String message);
}
