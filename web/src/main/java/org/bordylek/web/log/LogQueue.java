package org.bordylek.web.log;

import org.apache.commons.collections.Buffer;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

@Component
public class LogQueue {

    private Buffer buffer;

    @Required
    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    public void append(LoggingEvent event) {
        buffer.add(event);
    }
}
