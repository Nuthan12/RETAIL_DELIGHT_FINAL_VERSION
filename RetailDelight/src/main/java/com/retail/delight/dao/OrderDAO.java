package com.retail.delight.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.retail.delight.entity.Order;
import com.retail.delight.entity.OrderDetail;
import com.retail.delight.entity.Product;
import com.retail.delight.model.CartInfo;
import com.retail.delight.model.CartLineInfo;
import com.retail.delight.model.CustomerInfo;
import com.retail.delight.model.OrderDetailInfo;
import com.retail.delight.model.OrderInfo;
import com.retail.delight.pagination.PaginationResult;

@Transactional
@Repository
public class OrderDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ProductDAO productDAO;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	private int getMaxOrderNum() throws Exception {

		return jdbcTemplate.query("select max(order_num) FROM orders", new ResultSetExtractor<Integer>() {

			@Override
			public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
				int maxNum = 0;
				while (rs.next()) {
					maxNum = rs.getInt(1);
				}
				return maxNum;
			}


		});

	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrder(CartInfo cartInfo) throws Exception {
		Session session = this.sessionFactory.getCurrentSession();

		int orderNum = this.getMaxOrderNum() + 1;
		Order order = new Order();

		order.setId(UUID.randomUUID().toString());
		order.setOrderNum(orderNum);
		order.setOrderDate(new Date());
		order.setAmount(cartInfo.getAmountTotal());

		CustomerInfo customerInfo = cartInfo.getCustomerInfo();
		order.setCustomerName(customerInfo.getName());
		order.setCustomerEmail(customerInfo.getEmail());
		order.setCustomerPhone(customerInfo.getPhone());
		order.setCustomerAddress(customerInfo.getAddress());

		session.persist(order);

		List<CartLineInfo> lines = cartInfo.getCartLines();

		for (CartLineInfo line : lines) {
			OrderDetail detail = new OrderDetail();
			detail.setId(UUID.randomUUID().toString());
			detail.setOrder(order);
			detail.setAmount(line.getAmount());
			detail.setPrice(line.getProductInfo().getPrice());
			detail.setQuanity(line.getQuantity());

			String code = line.getProductInfo().getCode();
			Product product = this.productDAO.findProduct(code);
			detail.setProduct(product);

			session.persist(detail);
		}

		// Order Number!
		cartInfo.setOrderNum(orderNum);
		// Flush
		session.flush();
	}

	// @page = 1, 2, ...
	public PaginationResult<OrderInfo> listOrderInfo(int page, int maxResult, int maxNavigationPage) {
		String sql = "Select new " + OrderInfo.class.getName()//
				+ "(ord.id, ord.orderDate, ord.orderNum, ord.amount, "
				+ " ord.customerName, ord.customerAddress, ord.customerEmail, ord.customerPhone) " + " from "
				+ Order.class.getName() + " ord "//
				+ " order by ord.orderNum desc";

		Session session = this.sessionFactory.getCurrentSession();
		Query<OrderInfo> query = session.createQuery(sql, OrderInfo.class);
		return new PaginationResult<OrderInfo>(query, page, maxResult, maxNavigationPage);
	}

	public Order findOrder(String orderId) {
		Session session = this.sessionFactory.getCurrentSession();
		return session.find(Order.class, orderId);
	}
	
	public OrderDetail findOrderDetailsById(String id) {
        Session session = this.sessionFactory.getCurrentSession();
        return session.find(OrderDetail.class, id);
    }

	public OrderInfo getOrderInfo(String orderId) {
		Order order = this.findOrder(orderId);
		if (order == null) {
			return null;
		}
		return new OrderInfo(order.getId(), order.getOrderDate(), //
				order.getOrderNum(), order.getAmount(), order.getCustomerName(), //
				order.getCustomerAddress(), order.getCustomerEmail(), order.getCustomerPhone());
	}

	public List<OrderDetailInfo> listOrderDetailInfos(String orderId) {
		String sql = "Select new " + OrderDetailInfo.class.getName() //
				+ "(d.id, d.product.code, d.product.name , d.quanity,d.price,d.amount) "//
				+ " from " + OrderDetail.class.getName() + " d "//
				+ " where d.order.id = :orderId ";

		Session session = this.sessionFactory.getCurrentSession();
		Query<OrderDetailInfo> query = session.createQuery(sql, OrderDetailInfo.class);
		query.setParameter("orderId", orderId);

		return query.getResultList();
	}
}
