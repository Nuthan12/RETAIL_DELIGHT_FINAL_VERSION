package com.retail.delight.utils;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.retail.delight.model.CartInfo;

public class Utils {
	
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	// Products in the cart, stored in Session.
	public static CartInfo getCartInSession(HttpServletRequest request) {

		CartInfo cartInfo = (CartInfo) request.getSession().getAttribute("myCart");
		logger.info("Fetching the cart info from the cart session");
		if (cartInfo == null) {
			cartInfo = new CartInfo();

			request.getSession().setAttribute("myCart", cartInfo);
		}

		return cartInfo;
	}

	public static void removeCartInSession(HttpServletRequest request) {
		logger.info("The checked out cart has been cleared from the cart session");
		request.getSession().removeAttribute("myCart");
	}

	public static void storeLastOrderedCartInSession(HttpServletRequest request, CartInfo cartInfo) {
		logger.info("The cart information has been succesfully stored in the database after checkout");
		request.getSession().setAttribute("lastOrderedCart", cartInfo);
	}

	public static CartInfo getLastOrderedCartInSession(HttpServletRequest request) {
		logger.info("Fetching the cart info from the database");
		return (CartInfo) request.getSession().getAttribute("lastOrderedCart");
	}

}
