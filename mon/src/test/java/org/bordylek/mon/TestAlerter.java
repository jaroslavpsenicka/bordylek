package org.bordylek.mon;

import org.drools.definition.rule.Rule;

import java.util.Date;

public class TestAlerter implements Alerter {

    private String info;
    private String warning;
    private String error;

    @Override
    public void info(Rule rule, Date date, String message) {
        this.info = rule.getName() + " - " + message;
    }

    @Override
    public void warning(Rule rule, Date date, String message) {
        this.warning = message;
    }

    @Override
    public void error(Rule rule, Date date, String message) {
        this.error = message;
    }

    public String getInfo() {
        return info;
    }

    public String getWarning() {
        return warning;
    }

    public String getError() {
        return error;
    }
}
