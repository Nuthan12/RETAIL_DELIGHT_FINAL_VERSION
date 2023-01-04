package com.retail.delight.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retail.delight.dao.ProductDAO;
import com.retail.delight.entity.Product;
import com.retail.delight.form.ProductForm;
import com.retail.delight.model.ProductInfo;
import com.retail.delight.pagination.PaginationResult;

@Service
public class ProductServiceImpl implements ProductService  {
	
	@Autowired
	private ProductDAO productDAO;
	@Override
	public Product getProduct(String code){
		Product product = productDAO.findProduct(code);
		return product;
	}
	@Override
	public void saveProduct(ProductForm productForm){
		productDAO.save(productForm);
		
	}
	@Override
	public PaginationResult<ProductInfo> productList(String likeName,int page){
		final int maxResult = 25;
	      final int maxNavigationPage = 10;
	      PaginationResult<ProductInfo> pageResult = productDAO.queryProducts(page, maxResult, maxNavigationPage, likeName);
	      return pageResult;
	}
	

}
