
package org.bordylek.service.model;

public class Gauge extends AbstractMetric {

    private Object value;

    public Gauge() {
    }

    public Gauge(String name, com.codahale.metrics.Gauge gauge) {
        super(name);
        this.value = gauge.getValue();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }


}
