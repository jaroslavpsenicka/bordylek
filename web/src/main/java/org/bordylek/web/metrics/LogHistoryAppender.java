package org.bordylek.web.metrics;

import org.apache.commons.collections.Buffer;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
public class LogHistoryAppender extends AppenderSkeleton implements InitializingBean {

    private Buffer buffer;

    private static final Logger LOG = LoggerFactory.getLogger(LogHistoryAppender.class);

    public String retrieveLog() {
        StringBuilder builder = new StringBuilder();
        Iterator iterator = buffer.iterator();
        while (iterator.hasNext()) {
            Object line = iterator.next();
            builder.append(line).append("\n");
            iterator.remove();
        }

        return builder.toString();
    }

    @Required
    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.debug("Registering custom log appender " + this);
        LogManager.getRootLogger().addAppender(this);
    }

    @Override
    protected void append(LoggingEvent event) {
        if (buffer != null) {
            buffer.add(event.getRenderedMessage());
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public synchronized void close() {
        this.closed = true;
    }

}