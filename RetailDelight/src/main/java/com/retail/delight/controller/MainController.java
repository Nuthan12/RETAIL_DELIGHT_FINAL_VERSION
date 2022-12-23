package com.retail.delight.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.retail.delight.dao.OrderDAO;
import com.retail.delight.dao.ProductDAO;
import com.retail.delight.entity.Product;
import com.retail.delight.form.CustomerForm;
import com.retail.delight.model.CartInfo;
import com.retail.delight.model.CustomerInfo;
import com.retail.delight.model.OrderDetailInfo;
import com.retail.delight.model.OrderInfo;
import com.retail.delight.model.ProductInfo;
import com.retail.delight.pagination.PaginationResult;
import com.retail.delight.utils.Utils;
import com.retail.delight.validator.CustomerFormValidator;

@Controller
@Transactional
public class MainController {

   @Autowired
   private OrderDAO orderDAO;

   @Autowired
   private ProductDAO productDAO;

   @Autowired
   private CustomerFormValidator customerFormValidator;
   private static final Logger logger = LoggerFactory.getLogger(MainController.class);

   @InitBinder
   public void myInitBinder(WebDataBinder dataBinder) {
      Object target = dataBinder.getTarget();
      if (target == null) {
         return;
      }
      System.out.println("Target=" + target);

      // Case update quantity in cart
      // (@ModelAttribute("cartForm") @Validated CartInfo cartForm)
      if (target.getClass() == CartInfo.class) {

      }

      // Case save customer information.
      // (@ModelAttribute @Validated CustomerInfo customerForm)
      else if (target.getClass() == CustomerForm.class) {
         dataBinder.setValidator(customerFormValidator);
      }

   }
   @RequestMapping("/about")
   public String about() {
	   logger.info("The user is allowed access to the about us page");
      return "/about";
   }

   @RequestMapping("/403")
	public String accessDenied() {
		logger.warn("The Access for the user has been denied");
		return "/403";
	}

	@RequestMapping("/")
	public String home() {
		logger.info("The user is allowed access to the home page");
		return "index";
	}

   // Product List
   @RequestMapping({ "/productList" })
   public String listProductHandler(Model model, //
         @RequestParam(value = "name", defaultValue = "") String likeName,
         @RequestParam(value = "page", defaultValue = "1") int page) {
      final int maxResult = 25;
      final int maxNavigationPage = 10;
      logger.info("Fetching the list of all products for the customer from the database");
      PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
            maxResult, maxNavigationPage, likeName);
      logger.debug("Loaded a list of {} products fetched from the database",result.getTotalRecords());
      model.addAttribute("paginationProducts", result);
      return "productList";
   }
   //productlistmanager
   @RequestMapping({ "/productListManager" })
   public String listProduct(Model model, //
         @RequestParam(value = "name", defaultValue = "") String likeName,
         @RequestParam(value = "page", defaultValue = "1") int page) {
      final int maxResult = 25;
      final int maxNavigationPage = 10;
      logger.info("Fetching the list of all products for the admin from the database");
      PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
            maxResult, maxNavigationPage, likeName);
      logger.debug("Loaded a list of {} products fetched from the database",result.getTotalRecords());
      model.addAttribute("paginationProducts", result);
      return "productListManager";
   }
   //to buy product
   @RequestMapping({ "/buyProduct" })
	public String listProductHandler(HttpServletRequest request, Model model, //
			@RequestParam(value = "code", defaultValue = "") String code) {
		
		Product product = null;
		logger.info("The addition of the product with the code {} to the cart",code);
		logger.info("Validating the length of the product code");
		if (code != null && code.length() > 0) {
			product = productDAO.findProduct(code);
			logger.debug("The product {} with code {} to be added to the cart is currently been fetched from the product database",product.getName(), product.getCode());
		}
		logger.info("Validating the product availablity");
		if (product != null) {
			CartInfo cartInfo = Utils.getCartInSession(request);
			ProductInfo productInfo = new ProductInfo(product);
			logger.debug("The product {} with code {} to be added to the cart has been succesfully fetched from the product database",productInfo.getName(), productInfo.getCode());
			cartInfo.addProduct(productInfo, 1);
			logger.debug("The product {} has been succesfully added to the cart", product.getName());
		}
		return "redirect:/productList";
	}
   //to remove product from cart 
   @RequestMapping({ "/shoppingCartRemoveProduct" })
	public String removeProductHandler(HttpServletRequest request, Model model, //
			@RequestParam(value = "code", defaultValue = "") String code) {
		Product product = null;
		logger.info("The removal of the product with the code {} from the cart",code);
		logger.info("Validating the length of the product code");
		if (code != null && code.length() > 0) {
			product = productDAO.findProduct(code);
			logger.debug("The product {} with code {} to be removed from the cart is currently been fetched from the product database",product.getName(), product.getCode());
		}
		if (product != null) {
			CartInfo cartInfo = Utils.getCartInSession(request);
			ProductInfo productInfo = new ProductInfo(product);
			logger.debug("The product {} with code {} to be removed from the cart has been succesfully fetched from the product database",productInfo.getName(), productInfo.getCode());
			cartInfo.removeProduct(productInfo);
			logger.debug("The product {} has been succesfully removed from the cart", product.getName());
		}
		return "redirect:/shoppingCart";
	}

// POST: Update quantity for product in cart
	@RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.POST)
	public String shoppingCartUpdateQty(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("cartForm") CartInfo cartForm) {

		CartInfo cartInfo = Utils.getCartInSession(request);
		logger.info("Updating the quantity of the product {} in the cart",cartInfo.getCartLines().toString());
		logger.debug("The total quantity of all products in cart before updation is {}",cartInfo.getQuantityTotal());
		cartInfo.updateQuantity(cartForm);
		logger.info("The quantity of selected product for updation has been succesfully updated in the cart");
		logger.debug("The total quantity of all products in cart after updation is {}",cartInfo.getQuantityTotal());
		return "redirect:/shoppingCart";
	}

	// GET: Show cart.
		@RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.GET)
		public String shoppingCartHandler(HttpServletRequest request, Model model) {
			CartInfo myCart = Utils.getCartInSession(request);

			model.addAttribute("cartForm", myCart);
			logger.debug("Loaded a list of items in the cart as {}",myCart.toString());
			return "shoppingCart";
		}

		// GET: Enter customer information.
		@RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.GET)
		public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {
			CartInfo cartInfo = Utils.getCartInSession(request);
			logger.info("Validating the cartInfo");
			if (cartInfo.isEmpty()) {
				logger.error("The cart is empty, {}", cartInfo.toString());
				return "redirect:/shoppingCart";
			}
			CustomerInfo customerInfo = cartInfo.getCustomerInfo();
			CustomerForm customerForm = new CustomerForm(customerInfo);
			model.addAttribute("customerForm", customerForm);
			logger.info("Fetching the customer information from customer through form");
			return "shoppingCartCustomer";
		}

		// POST: Save customer information.
		@RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.POST)
		public String shoppingCartCustomerSave(HttpServletRequest request, //
				Model model, //
				@ModelAttribute("customerForm") @Validated CustomerForm customerForm, //
				BindingResult result, //
				final RedirectAttributes redirectAttributes) {
			logger.debug("The customer information is fetched from the customer as {}",customerForm.toString());

			logger.info("Validating the customer information");
			if (result.hasErrors()) {
				logger.error("The customer form entry is invalid", customerForm);
				customerForm.setValid(false);
				// Forward to reenter customer info.
				logger.info("Redirected back to customer form to enter the customer information");
				return "shoppingCartCustomer";
			}
			customerForm.setValid(true);
			CartInfo cartInfo = Utils.getCartInSession(request);
			CustomerInfo customerInfo = new CustomerInfo(customerForm);
			cartInfo.setCustomerInfo(customerInfo);
			logger.debug("The customer information entered by the customer is saved as {}",customerInfo.toString());

			return "redirect:/shoppingCartConfirmation";
		}

		// GET: Show information to confirm.
		@RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.GET)
		public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {

			CartInfo cartInfo = Utils.getCartInSession(request);

			logger.info("Validating the cartInfo");
			if (cartInfo == null || cartInfo.isEmpty()) {
				logger.error("The cart is empty {}", cartInfo.toString());
				return "redirect:/shoppingCart";
			} else if (!cartInfo.isValidCustomer()) {
				logger.error("The customer informtation is not invalid");
				return "redirect:/shoppingCartCustomer";
			}
			model.addAttribute("myCart", cartInfo);
			logger.debug("The cart {} has been succesfully confirmed by the customer",cartInfo.toString());
			return "shoppingCartConfirmation";
		}

		// POST: Submit Cart (Save)
		@RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.POST)

		public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {

			CartInfo cartInfo = Utils.getCartInSession(request);
			
			if (cartInfo.isEmpty()) {
				logger.error("The cart is empty {}", cartInfo.toString());
				return "redirect:/shoppingCart";
			} else if (!cartInfo.isValidCustomer()) {
				logger.error("The customer informtation is not invalid");
				return "redirect:/shoppingCartCustomer";
			}
			try {
				orderDAO.saveOrder(cartInfo);
				logger.debug("The confirmed cart is been stored as {}", cartInfo.toString());
			} catch (Exception e) {
				logger.error("The confirmed cart {} could not stored", cartInfo.toString());
				return "shoppingCartConfirmation";
			}

			// Remove Cart from Session.
			Utils.removeCartInSession(request);

			// Store last cart.
			Utils.storeLastOrderedCartInSession(request, cartInfo);

			return "redirect:/shoppingCartFinalize";
		}
   //finalize the cart 
		@RequestMapping(value = { "/shoppingCartFinalize" }, method = RequestMethod.GET)
		public String shoppingCartFinalize(HttpServletRequest request, Model model) {
			
			CartInfo lastOrderedCart = Utils.getLastOrderedCartInSession(request);

			logger.info("Validating the cartInfo fetched from the database");
			if (lastOrderedCart == null) {
				logger.debug("The cart is empty {}", lastOrderedCart);
				return "redirect:/shoppingCart";
			}
			model.addAttribute("lastOrderedCart", lastOrderedCart);
			logger.info("The cart {} has been succesfully checked out", lastOrderedCart.toString());
			return "shoppingCartFinalize";
		}
   //to get the product image 
   @RequestMapping(value = { "/productImage" }, method = RequestMethod.GET)
   public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
         @RequestParam("code") String code) throws IOException {
      Product product = null;
      if (code != null) {
         product = this.productDAO.findProduct(code);
      }
      if (product != null && product.getImage() != null) {
         response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
         response.getOutputStream().write(product.getImage());
      }
      response.getOutputStream().close();
   }
   
   //To get the bill
   @RequestMapping(value = { "/bill" }, method = RequestMethod.GET)
   public String orderList(Model model, //
         @RequestParam(value = "page", defaultValue = "1") String pageStr) {
      int page = 1;
      try {
         page = Integer.parseInt(pageStr);
      } catch (Exception e) {
      }
      final int MAX_RESULT = 1;
      final int MAX_NAVIGATION_PAGE = 0;

      PaginationResult<OrderInfo> paginationResult //
            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);
      model.addAttribute("paginationResult", paginationResult);
      logger.info("Fetching the order details of order from the database for the bill");
      return "bill";
   }
   
   //to view bill
   @RequestMapping(value = { "/bill/view" }, method = RequestMethod.GET)
   public String orderView(Model model, @RequestParam("orderId") String orderId) {
      OrderInfo orderInfo = null;
      
      logger.info("Validating the orderInfo for the order {} fetched from the database",orderId);
      if (orderId != null) {
    	  logger.debug("The order details for the order id {} is available",orderId);
         orderInfo = this.orderDAO.getOrderInfo(orderId);
      }
      if (orderInfo == null) {
    	  logger.error("Fetching the order details for the bill from the database for the order id {} not possible",orderId);
         return "redirect:/bill";
      }
      List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfos(orderId);
      orderInfo.setDetails(details);
      model.addAttribute("orderInfo", orderInfo);
      logger.debug("The order details has been fetched from the database for the order id {}",orderId);
      logger.info("The bill has been succesfully displayed for the order id {}",orderId);
      return "billview";
   }


}