package org.bordylek.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

@Component
public class NotificationService {

	@Value("${mail.defaultSenderEmail:info@bordylek.org}")
	private String defaultSenderEmail;
	@Value("${mail.defaultSenderName:Bordylek}")
	private String defaultSenderName;

    @Autowired
    private JavaMailSender mailSender;

    private DocumentBuilder db;
	private Transformer serializer;
	private XPathExpression xpe;
	private Configuration configuration;

	private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);
	private static final String SUBJ_XPATH = "/html/head/title";

	public NotificationService() throws ParserConfigurationException, TransformerException, XPathException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
		xpe = factory.newXPath().compile(SUBJ_XPATH);
		serializer = TransformerFactory.newInstance().newTransformer();
	    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	}
	
	@Required
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	@Required
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void sendMessage(String to, String templateName, Locale locale, Map<String, Object> context) {
		try {
            InternetAddress from = new InternetAddress(defaultSenderEmail, defaultSenderName);
            sendMessage(from, to, templateName, locale, context);
		} catch (Exception ex) {
			LOG.error("error sending message", ex);
		}
	}
	
	public void sendMessage(InternetAddress from, String to, String templateName, Locale locale, 
		Map<String, Object> context) {
		try {
			String body = buildTemplate(templateName, locale, context);
			Document doc = db.parse(new ByteArrayInputStream(body.getBytes("UTF-8")));
			sendMessage(from, to, xpe.evaluate(doc), body);
		} catch (Exception ex) {
			LOG.error("error sending message", ex);
		}
	}

	public void sendMessage(InternetAddress from, String to, String subject, String body) {
		LOG.info("Sending email to " + to + ", subject: " + subject);
		try {
			MimeMessage msg = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg);
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);
			helper.setReplyTo(from);
			msg.setSentDate(new Date(System.currentTimeMillis()));
			mailSender.send(msg);
		} catch (Exception ex) {
			LOG.error("error sending message", ex);
		}
	}

	protected String buildTemplate(String templateName, Locale locale, 
		Map<String, Object> context) throws IOException, TemplateException {
		Template template = configuration.getTemplate(templateName+".ftl", locale, "UTF-8");
		StringWriter textWriter = new StringWriter();
		template.process(context, textWriter);
		return textWriter.toString();
	}

}
