package org.bordylek.mon;

public interface Alerter {
    void info(String message);
    void warning(String message);
    void error(String message);
}
