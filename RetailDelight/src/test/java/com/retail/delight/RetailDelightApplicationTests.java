package com.retail.delight;

import static org.junit.Assert.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.retail.delight.dao.ProductDAO;
import com.retail.delight.entity.Account;
import com.retail.delight.entity.Product;

@SpringBootTest
class RetailDelightApplicationTests {

	@Autowired
	private ProductDAO pDao;
	private Account account = new Account("manager","123",true,"ROLE_MANAGER");

	@Test
	public void testFindAccountByUserName(){
		String username;
		username = "manager";
		assertEquals(username, account.getUserName());
		username = "employee";
		assertNotEquals(username, account.getUserName());
	}
	
	@Test
	public void testFindAccountByRole(){
		String role; 
		role = "ROLE_MANAGER";
		assertEquals(role, account.getUserRole());
		role = "ROLE_EMPLOYEE";
		assertNotEquals(role, account.getUserRole());
	}
	
	@Test
	void testProductDAO() {
		Product p = pDao.findProduct("RD005");
		assertEquals(p.getPrice(),(double)(20.00),0.000001);
		assertEquals(p.getName(),"DAIRY MILK");
	}
}
