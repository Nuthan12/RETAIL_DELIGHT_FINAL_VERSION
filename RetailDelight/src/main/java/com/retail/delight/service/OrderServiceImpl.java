package com.retail.delight.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retail.delight.dao.OrderDAO;
import com.retail.delight.model.CartInfo;
import com.retail.delight.model.OrderDetailInfo;
import com.retail.delight.model.OrderInfo;
import com.retail.delight.pagination.PaginationResult;
@Service
public class OrderServiceImpl implements OrderService {
	
	
	@Autowired
	private OrderDAO orderDAO; 
	
	@Override
	public OrderInfo orderinfo(String orderId){
		OrderInfo orderInfo=null;

		orderInfo = this.orderDAO.getOrderInfo(orderId);
		
		List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfos(orderId);
		orderInfo.setDetails(details);
		
		return orderInfo;
	}
	@Override
	public PaginationResult<OrderInfo> orderListPagination(String pageStr){
		int page = 1;
		try {
			page = Integer.parseInt(pageStr);
		} catch (Exception e) {
		}
		final int MAX_RESULT = 20;
		final int MAX_NAVIGATION_PAGE = 10;
		
		PaginationResult<OrderInfo> paginationResult=orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);
		return paginationResult;
		
	}
	@Override
	public void save(CartInfo cartInfo) throws Exception{
		orderDAO.saveOrder(cartInfo);
	}
	@Override
	public PaginationResult<OrderInfo> billListPagination(String pageStr){
		int page = 1;
		try {
			page = Integer.parseInt(pageStr);
		} catch (Exception e) {
		}
		final int MAX_RESULT = 1;
		final int MAX_NAVIGATION_PAGE = 1;
		
		PaginationResult<OrderInfo> paginationResult=orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);
		return paginationResult;
		
	}
	
	

}

