package com.retail.delight.controller;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.retail.delight.dao.OrderDAO;
import com.retail.delight.dao.ProductDAO;
import com.retail.delight.entity.Product;
import com.retail.delight.form.ProductForm;
import com.retail.delight.model.OrderDetailInfo;
import com.retail.delight.model.OrderInfo;
import com.retail.delight.pagination.PaginationResult;
import com.retail.delight.validator.ProductFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class AdminController {

   @Autowired
   private OrderDAO orderDAO;

   @Autowired
   private ProductDAO productDAO;

   @Autowired
   private ProductFormValidator productFormValidator;
   
   private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
   
   //to trim spaces and pre-processing of any object
   @InitBinder
   public void myInitBinder(WebDataBinder dataBinder) {
      Object target = dataBinder.getTarget();
      if (target == null) {
         return;
      }
      System.out.println("Target=" + target);

      if (target.getClass() == ProductForm.class) {
         dataBinder.setValidator(productFormValidator);
      }
   }

   // GET: Show Login Page
   @RequestMapping(value = { "/admin/login" }, method = RequestMethod.GET)
   public String login(Model model) {
	   logger.info("The admin is allowed access to the home page");
      return "login";
   }

   @RequestMapping(value = { "/admin/accountInfo" }, method = RequestMethod.GET)
   public String accountInfo(Model model) {
	   logger.info("Fetching the user details for the admin");
      UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      System.out.println(userDetails.getPassword());
      System.out.println(userDetails.getUsername());
      System.out.println(userDetails.isEnabled());
      logger.info("The user details for the admin has been fetched");
      model.addAttribute("userDetails", userDetails);
      return "accountInfo";
   }

   @RequestMapping(value = { "/admin/orderList" }, method = RequestMethod.GET)
   public String orderList(Model model, //
         @RequestParam(value = "page", defaultValue = "1") String pageStr) {
      int page = 1;
      try {
         page = Integer.parseInt(pageStr);
      } catch (Exception e) {
      }
      final int MAX_RESULT = 20;
      final int MAX_NAVIGATION_PAGE = 10;
      logger.info("Fetching the order list");
      PaginationResult<OrderInfo> paginationResult //
            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);
      
      model.addAttribute("paginationResult", paginationResult);
      logger.info("The order list has been succesfully fetched");
      return "orderList";
   }

   // GET: Show product.
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.GET)
	public String product(Model model, @RequestParam(value = "code", defaultValue = "") String code) {

		ProductForm productForm = null;
		logger.info("Validating the length of the product code");
		if (code != null && code.length() > 0) {
			Product product = productDAO.findProduct(code);
			logger.debug("The length of the product code is {}",code.length());
			
			logger.info("Validating the product availablity");
			if (product != null) {
				logger.debug("The product fetched is {} available", product);
				productForm = new ProductForm(product);
			}
		}
		logger.info("Adding of new product to the product list");
		logger.debug("Before addition of new product to the product list, {}", productForm);
		if (productForm == null) {
			productForm = new ProductForm();
			productForm.setNewProduct(true);
		}
		logger.debug("After addition of new product to the product list, {}", productForm);
		
		model.addAttribute("productForm", productForm);
		logger.info("The added product has been succesfully fetched");
		return "product";
	}

   // POST: Save product
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.POST)
   public String productSave(Model model, //
         @ModelAttribute("productForm") @Validated ProductForm productForm, //
         BindingResult result, //
         final RedirectAttributes redirectAttributes) {

      if (result.hasErrors()) {
    	  logger.error("The product could not be added to the database{}",productForm);
         return "product";
      }
      try {
    	  logger.info("Adding of new product to the database");
         productDAO.save(productForm);
      } catch (Exception e) {
         Throwable rootCause = ExceptionUtils.getRootCause(e);
         String message = rootCause.getMessage();
         model.addAttribute("errorMessage", message);
         logger.error("The product could not be added to the database{}",productForm);
         // Show product form.
         return "product";
      }

      return "redirect:/productListManager";
   }
   //Get: to get order details
   @RequestMapping(value = { "/admin/order" }, method = RequestMethod.GET)
   public String orderView(Model model, @RequestParam("orderId") String orderId) {
      OrderInfo orderInfo = null;
      
      if (orderId != null) {
    	  logger.info("The order details is available, {}", orderInfo);
         orderInfo = this.orderDAO.getOrderInfo(orderId);
      }
      if (orderInfo == null) {
    	  logger.info("Fetching the order details from the database");
         return "redirect:/admin/orderList";
      }
      List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfos(orderId);
      orderInfo.setDetails(details);

      model.addAttribute("orderInfo", orderInfo);
      logger.info("The order details has been succesfully fetched from the database");
      return "order";
   }

}
