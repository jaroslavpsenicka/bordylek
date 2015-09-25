package org.bordylek.mon;

import org.bordylek.mon.model.Alert;
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

    @Value("${mail.recipient.address}")
    private String recipientAddress;

    @Value("${mail.template.alert:alert}")
	private String alertTemplate;
	
	private static final Logger LOG = LoggerFactory.getLogger(UserNotifier.class);
	
	public void notify(final Alert alert) {
        if ( ! StringUtils.isEmpty(recipientAddress)) {
            LOG.info("Sending alert " + alert.getFqName() + " to " + recipientAddress);
            for (String address : recipientAddress.split(",")) {
                service.sendMessage(address, alertTemplate, Locale.ENGLISH, new HashMap<String, Object>() {{
                    put("alert", alert);
                }});
            }
        } else throw new IllegalArgumentException("Recipient address not defined, notification cannot be delivered.");
    }

}
