package com.retail.delight.service;

public interface SendMail {

	

	void sendForgotPasswordToManager(String email, String role, String name);

	void sendForgotPasswordToEmployee(String email, String role, String name);

}