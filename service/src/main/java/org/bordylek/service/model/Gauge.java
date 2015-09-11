
package org.bordylek.service.model;

import java.util.Date;

public class Gauge extends Metrics {

    private Object value;

    public Gauge() {
    }

    public Gauge(String name, com.codahale.metrics.Gauge gauge, Date saveDate) {
        super(name, saveDate);
        this.value = gauge.getValue();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }


}
