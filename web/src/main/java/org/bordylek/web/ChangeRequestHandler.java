package org.bordylek.web;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import org.bordylek.service.model.process.ChangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestHandler {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private static final Logger LOG = LoggerFactory.getLogger(VotingController.class);

    @Metered
    @ExceptionMetered
    public void handle(ChangeRequest request) {
        beanFactory.autowireBean(request);
        request.execute();
    }
}
