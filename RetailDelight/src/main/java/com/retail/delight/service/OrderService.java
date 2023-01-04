package com.retail.delight.service;

import org.springframework.stereotype.Component;

import com.retail.delight.model.CartInfo;
import com.retail.delight.model.OrderInfo;
import com.retail.delight.pagination.PaginationResult;
@Component
public interface OrderService {
	
	OrderInfo orderinfo( String orderId);
	
	PaginationResult<OrderInfo> orderListPagination(String pageStr);
	
	void save(CartInfo cartInfo) throws Exception;
	
	PaginationResult<OrderInfo> billListPagination(String pageStr);

}