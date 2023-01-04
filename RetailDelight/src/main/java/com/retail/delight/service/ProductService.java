package com.retail.delight.service;

import org.springframework.stereotype.Component;

import com.retail.delight.entity.Product;
import com.retail.delight.form.ProductForm;
import com.retail.delight.model.ProductInfo;
import com.retail.delight.pagination.PaginationResult;
@Component
public interface ProductService {
	
	Product getProduct(String code);
	
	void saveProduct(ProductForm productForm);
	
	 PaginationResult<ProductInfo> productList(String likeName,int page);
	 

}