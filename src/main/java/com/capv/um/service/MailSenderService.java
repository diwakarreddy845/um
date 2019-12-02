package com.capv.um.service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;



@Service
public class MailSenderService {

	@Autowired 
	Environment environment;
	
	public JavaMailSender javaMailSender(Long clientId) {
		
	    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
	    javaMailSender.setHost(CapvClientUserUtil.getClientConfigProperty(clientId,CapvClientUserConstants.CAPV_CLIENT_MAIL_HOST));
	    javaMailSender.setPort(Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(clientId,CapvClientUserConstants.CAPV_CLIENT_MAIL_PORT)));
	    javaMailSender.setUsername(CapvClientUserUtil.getClientConfigProperty(clientId,CapvClientUserConstants.CAPV_CLIENT_MAIL_USERNAME));
	    javaMailSender.setPassword(environment.getRequiredProperty(CapvClientUserUtil.getClientConfigProperty(clientId,CapvClientUserConstants.CAPV_CLIENT_MAIL_PASSWORD)));
	    javaMailSender.setJavaMailProperties(getMailProperties(clientId));
	    return javaMailSender;
	}
	
	private Properties getMailProperties(Long clientId) {
	    Properties properties = new Properties();
	    properties.setProperty("mail.smtp.auth", CapvClientUserUtil.getClientConfigProperty(clientId,CapvClientUserConstants.CAPV_CLIENT_MAIL_SMTP_AUTH));
	    properties.setProperty("mail.smtp.ssl.enable",CapvClientUserUtil.getClientConfigProperty(clientId,CapvClientUserConstants.CAPV_CLIENT_MAIL_SSL_ENABLE));
	    return properties;
	}


}
