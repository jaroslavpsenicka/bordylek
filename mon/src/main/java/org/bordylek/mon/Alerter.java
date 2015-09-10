package org.bordylek.mon;

import org.drools.definition.rule.Rule;

import java.util.Date;

public interface Alerter {
    void info(Rule rule, Date date, String message);
    void warning(Rule rule, Date date, String message);
    void error(Rule rule, Date date, String message);
}
