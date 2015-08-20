package org.bordylek.service.event;

import org.bordylek.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;

@Component
public class UserNotifier {

	@Autowired
	private NotificationService service;
	
	@Value("${mail.template.newUser:newUser}")
	private String newUserEmailTemplate;
	
	private static final Logger LOG = LoggerFactory.getLogger(UserNotifier.class);
	
	public void notify(final NewUserEvent newUserEvent) {
        String to = newUserEvent.getUser().getEmail();
        if (StringUtils.isEmpty(to)) {
            throw new IllegalArgumentException("Parameter 'email' not defined, cannot send registration email");
        }

        String localeText = newUserEvent.getUser().getLocale();
        Locale locale = !StringUtils.isEmpty(localeText) ? new Locale(localeText) : Locale.ENGLISH;
        LOG.info("Sending registration email to "+to+" locale "+locale);
        service.sendMessage(to, newUserEmailTemplate, locale, new HashMap<String, Object>() {{
            put("user", newUserEvent.getUser());
        }});
    }

}
