package org.bordylek.mon;

import org.drools.definition.rule.Rule;

public interface Alerter {
    void info(Rule rule, String message);
    void warning(Rule rule, String message);
    void error(Rule rule, String message);
}
