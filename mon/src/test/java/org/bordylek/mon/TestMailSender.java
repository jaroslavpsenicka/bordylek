package org.bordylek.mon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

public class TestMailSender extends JavaMailSenderImpl {

    private List<MimeMessage> messages = new ArrayList<>();

    private static final Logger LOG = LoggerFactory.getLogger(TestMailSender.class);

    public void send(MimeMessage mimeMessage) throws MailException {
        LOG.info("Receiving message " + mimeMessage);
        messages.add(mimeMessage);
    }

    public List<MimeMessage> getMessages() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }

}
