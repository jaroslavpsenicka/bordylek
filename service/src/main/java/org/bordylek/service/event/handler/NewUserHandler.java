package org.bordylek.service.event.handler;

import org.bordylek.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

@Component
public class NewUserHandler {

	@Autowired
	private NotificationService service;
	
	@Value("${mail.template.newUser:newUser.ftl}")
	private String templateName;
	
	private static final Logger LOG = LoggerFactory.getLogger(NewUserHandler.class);
	
	public void handleEvent(String eventDomain, String eventName, Map<String, Object> parameters) {
		String to = (String)parameters.get("email");
        String locale = (String)parameters.get("locale");
        String lang = locale.substring(0, 2);
        String country = locale.substring(3);

        if (StringUtils.isEmpty(to)) {
            throw new IllegalArgumentException("Parameter 'email' not defined, cannot send registration email");
        }

        LOG.info("Sending registration email to "+parameters.get("email")+" locale "+locale);
        service.sendMessage(to, templateName, new Locale(lang, country), parameters);
    }

}
