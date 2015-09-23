package org.bordylek.web.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class LogHistoryAppender extends AppenderSkeleton implements ApplicationContextAware {

    @Autowired
    private LogQueue logQueue;

    @Override
    protected void append(LoggingEvent event) {
        if (logQueue != null) {
            logQueue.append(event);
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    @Override
    public synchronized void close() {
        this.closed = true;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }
}