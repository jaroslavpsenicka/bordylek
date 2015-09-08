package org.bordylek.mon;

public class TestAlerter implements Alerter {

    private String info;
    private String warning;
    private String error;

    @Override
    public void info(String message) {
        this.info = message;
    }

    @Override
    public void warning(String message) {
        this.warning = message;
    }

    @Override
    public void error(String message) {
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
