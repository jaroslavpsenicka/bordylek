package org.bordylek.web;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

public class TestMailSender extends JavaMailSenderImpl {

    private List<MimeMessage> messages = new ArrayList<>();

    public void send(MimeMessage mimeMessage) throws MailException {
        messages.add(mimeMessage);
    }

    public List<MimeMessage> getMessages() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }

}
