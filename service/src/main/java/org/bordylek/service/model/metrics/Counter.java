package org.bordylek.service.model.metrics;

import java.util.Date;

public class Counter extends Metrics {

    private Object count;

    public Counter() {
    }

    public Counter(String name, com.codahale.metrics.Counter counter, Date saveDate) {
        super(name, saveDate);
        this.count = counter.getCount();
    }

    public Object getCount() {
        return count;
    }

    public void setCount(final Object count) {
        this.count = count;
    }

}
