package com.retail.delight.service;

public interface EmailService {

	void SendMail(String from, String to, String subject, String text);

}