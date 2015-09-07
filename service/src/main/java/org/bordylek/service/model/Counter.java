package org.bordylek.service.model;

public class Counter extends AbstractMetric {

    private Object count;

    public Counter() {
    }

    public Counter(String name, com.codahale.metrics.Counter counter) {
        super(name);
        this.count = counter.getCount();
    }

    public Object getCount() {
        return count;
    }

    public void setCount(final Object count) {
        this.count = count;
    }

}
