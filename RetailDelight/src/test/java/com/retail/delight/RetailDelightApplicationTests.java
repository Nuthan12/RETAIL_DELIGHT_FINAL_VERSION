package com.retail.delight;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.retail.delight.dao.OrderDAO;
import com.retail.delight.dao.ProductDAO;
import com.retail.delight.entity.Account;
import com.retail.delight.entity.Order;
import com.retail.delight.entity.OrderDetail;
import com.retail.delight.entity.Product;
import com.retail.delight.model.OrderInfo;

@SpringBootTest
@EnableAutoConfiguration(exclude=SecurityAutoConfiguration.class)
@AutoConfigureMockMvc
class RetailDelightApplicationTests {

	@Autowired
	private ProductDAO pDao;

	@Autowired
	private OrderDAO oDao;

	@Autowired
	private MockMvc mvc;



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
	public void testProductDao() {
		Product p = pDao.findProduct("RD005");
		assertEquals(p.getPrice(),(double)(20.00),0.000001);
		assertEquals(p.getName(),"DAIRY MILK");
	}

	@Test
	public void testOrderDao() {	
		Order o= oDao.findOrder("66c0a469-ce25-43eb-b65e-fb668e2a41ee");
		assertEquals(o.getOrderNum(),1);
		assertEquals(o.getCustomerName(),"RITESH K");
		assertEquals(o.getAmount(),(double)(601.00),0.000001);
		assertEquals(o.getCustomerPhone(),"7300422656");
		assertEquals(o.getCustomerEmail(),"ritesh@gmail.com");
	}

	@Test
	public void testOrderInfo() {
		OrderInfo oi = oDao.getOrderInfo("64ec4d5f-b0e4-4181-bcda-835c2cdc56d3");
		assertEquals(oi.getOrderNum(),7);
		assertEquals(oi.getCustomerName(),"SANJAY");
		assertEquals(oi.getAmount(),(double)(112.05),0.000001);
		assertEquals(oi.getCustomerPhone(),"5428975991");
		assertEquals(oi.getCustomerEmail(),"sanjay@gmail.com");
	}


	@Test
	public void testOrderDetails() {
		OrderDetail od1=oDao.findOrderDetailsById("127dfc20-0b1d-4572-a3a9-818680d17941");
		assertEquals(od1.getAmount(), (double) (1872),0.000001);
		assertEquals(od1.getQuanity(), 10);
		assertEquals(od1.getPrice(), (double) (187.2),0.000001);
		assertEquals(od1.getProduct().getCode(), "RD0016");
		assertEquals(od1.getOrder().getId(), "c2ae29ff-fb8c-46d1-bdb7-669f3bef8d15");

		OrderDetail od2=oDao.findOrderDetailsById("f671d95f-2567-4462-9d4c-8653caad0223");
		assertEquals(od2.getAmount(), (double) (48.1),0.000001);
		assertEquals(od2.getQuanity(), 1);
		assertEquals(od2.getPrice(), (double) (48.1),0.000001);
		assertEquals(od2.getProduct().getCode(), "RD0015");
		assertEquals(od2.getOrder().getId(), "c2ae29ff-fb8c-46d1-bdb7-669f3bef8d15");
	}

	@Test
	public void testLoginPage() throws Exception {
		mvc.perform(get("/admin/login")).andExpect(status().isOk());
	}


	@Test
	@WithMockUser(authorities="ROLE_EMPLOYEE")
	public void testAdminAccessEmployee() throws Exception {
		mvc.perform(get("/admin/accountInfo")).andExpect(status().isOk());
		mvc.perform(get("/admin/orderList")).andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser(authorities="ROLE_MANAGER")
	public void testAdminAccessForManager() throws Exception {
		mvc.perform(get("/admin/accountInfo")).andExpect(status().isOk());
		mvc.perform(get("/productListManager")).andExpect(status().isOk());
		mvc.perform(get("/admin/orderList")).andExpect(status().isOk());
		mvc.perform(get("/admin/product")).andExpect(status().isOk());		
	}
	
	@Test
	@WithMockUser(authorities="ROLE_CUSTOMER")
	public void testlCustomerAccesstoAdmin() throws Exception {
		mvc.perform(get("/admin/accountInfo")).andExpect(status().isForbidden());
		mvc.perform(get("/productListManager")).andExpect(status().isForbidden());
		mvc.perform(get("/admin/orderList")).andExpect(status().isForbidden());
		mvc.perform(get("/admin/product")).andExpect(status().isForbidden());		
	}
}
