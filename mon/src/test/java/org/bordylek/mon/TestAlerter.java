package org.bordylek.mon;

import org.drools.definition.rule.Rule;

public class TestAlerter implements Alerter {

    private String info;
    private String warning;
    private String error;

    @Override
    public void info(Rule rule, String message) {
        this.info = rule.getName() + " - " + message;
    }

    @Override
    public void warning(Rule rule, String message) {
        this.warning = message;
    }

    @Override
    public void error(Rule rule, String message) {
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
