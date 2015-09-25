package org.bordylek.mon;

import org.bordylek.service.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LogCleaner {

    @Autowired
    private LogRepository logRepository;

    @Value("${log.timeToLive:172800000}")
    private Long timeToLive;

    private static final Logger LOG = LoggerFactory.getLogger(LogCleaner.class);

    public Long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void clean() {
        Date since = new Date(System.currentTimeMillis() - timeToLive);
        LOG.info("Cleaning logs older than " + since);
        logRepository.delete(logRepository.findByTimestampLessThan(since));
    }

}
