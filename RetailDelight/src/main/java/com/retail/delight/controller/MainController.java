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
      logger.info("Loading the list of all products");
      PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
            maxResult, maxNavigationPage, likeName);

      model.addAttribute("paginationProducts", result);
      logger.debug("Loaded list of all products");
      return "productList";
   }
   //productlistmanager
   @RequestMapping({ "/productListManager" })
   public String listProduct(Model model, //
         @RequestParam(value = "name", defaultValue = "") String likeName,
         @RequestParam(value = "page", defaultValue = "1") int page) {
      final int maxResult = 25;
      final int maxNavigationPage = 10;

      PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
            maxResult, maxNavigationPage, likeName);

      model.addAttribute("paginationProducts", result);
      return "productListManager";
   }
   //to buy product
   @RequestMapping({ "/buyProduct" })
	public String listProductHandler(HttpServletRequest request, Model model, //
			@RequestParam(value = "code", defaultValue = "") String code) {
		
		logger.info("Adding the product to the cart");
		Product product = null;
		logger.info("Validating the length of the product code");
		if (code != null && code.length() > 0) {
			product = productDAO.findProduct(code);
			logger.debug("The length of the product code is {}",code.length());
			logger.debug("The product fetched to add to the cart is {}", product);
		}
		logger.info("Validating the product availablity");
		if (product != null) {

			logger.info("Fetching the cart info from the cart session");
			CartInfo cartInfo = Utils.getCartInSession(request);
			logger.info("Fetching the cart info from the cart session");
			ProductInfo productInfo = new ProductInfo(product);

			cartInfo.addProduct(productInfo, 1);
			logger.debug("The product added to the cart is {}", product);
			logger.debug("The product info of the product added to the cart is {}", productInfo);
		}
		logger.info("The product has been succesfully added to the cart");
		return "redirect:/productList";
	}
   //to remove product from cart 
   @RequestMapping({ "/shoppingCartRemoveProduct" })
	public String removeProductHandler(HttpServletRequest request, Model model, //
			@RequestParam(value = "code", defaultValue = "") String code) {
		logger.info("Removing the product from the cart");
		Product product = null;
		logger.info("Validating the length of the product code");
		if (code != null && code.length() > 0) {
			product = productDAO.findProduct(code);
			logger.debug("The length of the product code is {}", code.length());
			logger.debug("The product fetched to remove the cart is {}", product);
		}
		if (product != null) {
			logger.info("Fetching the cart info from the cart session");
			CartInfo cartInfo = Utils.getCartInSession(request);
			logger.info("Fetching the cart info from the cart session");
			ProductInfo productInfo = new ProductInfo(product);

			cartInfo.removeProduct(productInfo);
			logger.debug("The product removed from the cart is {}", product);
			logger.debug("The product info of the product added to the cart is {}", productInfo);
		}
		logger.info("The product has been succesfully removed from the cart");
		return "redirect:/shoppingCart";
	}

// POST: Update quantity for product in cart
	@RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.POST)
	public String shoppingCartUpdateQty(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("cartForm") CartInfo cartForm) {

		logger.info("Fetching the cart info from the cart session");
		CartInfo cartInfo = Utils.getCartInSession(request);

		logger.info("Updating the quantity of the product in the cart");
		cartInfo.updateQuantity(cartForm);
		logger.info("The quantity of the product has been succesfully upadted in the cart");
		logger.debug("The total updated quantity of all products is {}",cartInfo.getQuantityTotal());
		logger.info("The product has been succesfully removed from the cart");
		return "redirect:/shoppingCart";
	}

	// GET: Show cart.
		@RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.GET)
		public String shoppingCartHandler(HttpServletRequest request, Model model) {
			logger.info("Fetching the cart info from the cart session");
			CartInfo myCart = Utils.getCartInSession(request);

			model.addAttribute("cartForm", myCart);
			logger.debug("Loaded the items in the cart as {}",myCart);
			return "shoppingCart";
		}

		// GET: Enter customer information.
		@RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.GET)
		public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {
			logger.info("Fetching the cart info from the cart session");
			CartInfo cartInfo = Utils.getCartInSession(request);
			logger.info("Validating the cartInfo");
			if (cartInfo.isEmpty()) {
				logger.debug("The cart is empty, {}", cartInfo);
				return "redirect:/shoppingCart";
			}
			logger.info("Fetching the customer information");
			CustomerInfo customerInfo = cartInfo.getCustomerInfo();

			logger.debug("Before the customer information is fetched : {}",customerInfo);
			CustomerForm customerForm = new CustomerForm(customerInfo);
			logger.debug("After the customer information is fetched : {}",customerInfo);

			logger.debug("Before the customer form is fetched : {}",customerForm);
			model.addAttribute("customerForm", customerForm);
			logger.debug("After the customer form is fetched : {}",customerForm);
			return "shoppingCartCustomer";
		}

		// POST: Save customer information.
		@RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.POST)
		public String shoppingCartCustomerSave(HttpServletRequest request, //
				Model model, //
				@ModelAttribute("customerForm") @Validated CustomerForm customerForm, //
				BindingResult result, //
				final RedirectAttributes redirectAttributes) {
			logger.info("Saving the customer information");

			logger.info("Validating the customer information");
			if (result.hasErrors()) {
				logger.error("The customer form entry is invalid", customerForm);
				customerForm.setValid(false);
				// Forward to reenter customer info.
				logger.info("Redirected to enter the customer information");
				return "shoppingCartCustomer";
			}

			customerForm.setValid(true);
			logger.debug("The customer form entry is Valid : {}", customerForm);

			logger.info("Fetching the cart info from the cart session");
			CartInfo cartInfo = Utils.getCartInSession(request);

			CustomerInfo customerInfo = new CustomerInfo(customerForm);
			logger.debug("Before the customer information is saved : {}",customerInfo);
			cartInfo.setCustomerInfo(customerInfo);
			logger.debug("After the customer information is saved : {}",customerInfo);

			return "redirect:/shoppingCartConfirmation";
		}

		// GET: Show information to confirm.
		@RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.GET)
		public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {

			logger.info("Fetching the cart info from the cart session");
			CartInfo cartInfo = Utils.getCartInSession(request);

			logger.info("Validating the cartInfo");
			if (cartInfo == null || cartInfo.isEmpty()) {
				logger.debug("The cart is empty, {}", cartInfo);
				return "redirect:/shoppingCart";
			} else if (!cartInfo.isValidCustomer()) {
				logger.error("The customer is not invalid, {}",cartInfo);
				return "redirect:/shoppingCartCustomer";
			}
			model.addAttribute("myCart", cartInfo);
			logger.debug("The confirmed cart information is : {}",cartInfo);

			logger.info("The cart has been succesfully confirmed", cartInfo);
			return "shoppingCartConfirmation";
		}

		// POST: Submit Cart (Save)
		@RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.POST)

		public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {

			logger.info("Fetching the cart info from the cart session");
			CartInfo cartInfo = Utils.getCartInSession(request);

			logger.info("Validating the cartInfo");
			if (cartInfo.isEmpty()) {
				logger.debug("The cart is empty, {}", cartInfo);
				return "redirect:/shoppingCart";
			} else if (!cartInfo.isValidCustomer()) {
				logger.error("The customer is not invalid, {}",cartInfo);
				return "redirect:/shoppingCartCustomer";
			}
			try {
				orderDAO.saveOrder(cartInfo);
				logger.debug("The cart is {}", cartInfo);
			} catch (Exception e) {
				logger.error("The cart is {}", cartInfo);
				return "shoppingCartConfirmation";
			}

			
			// Remove Cart from Session.
			Utils.removeCartInSession(request);
			logger.info("The cart has been removed from the cart session");

			// Store last cart.
			Utils.storeLastOrderedCartInSession(request, cartInfo);
			logger.info("The cart has been succesfully stored", cartInfo);

			return "redirect:/shoppingCartFinalize";
		}
   //finalize the cart 
		@RequestMapping(value = { "/shoppingCartFinalize" }, method = RequestMethod.GET)
		public String shoppingCartFinalize(HttpServletRequest request, Model model) {
			
			logger.info("Fetching the cart info from the database");
			CartInfo lastOrderedCart = Utils.getLastOrderedCartInSession(request);

			logger.info("Validating the cartInfo fetched from the database");
			if (lastOrderedCart == null) {
				logger.debug("The cart is empty, {}", lastOrderedCart);
				return "redirect:/shoppingCart";
			}
			model.addAttribute("lastOrderedCart", lastOrderedCart);
			logger.info("The cart has been succesfully finalized", lastOrderedCart);
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
		logger.info("Fetching the bill");
      int page = 1;
      try {
         page = Integer.parseInt(pageStr);
      } catch (Exception e) {
      }
      final int MAX_RESULT = 1;
      final int MAX_NAVIGATION_PAGE = 0;
      logger.info("Fetching the order details from the database");
      PaginationResult<OrderInfo> paginationResult //
            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);

      model.addAttribute("paginationResult", paginationResult);
      logger.info("The order details has been succesfully fetched from the database");
      return "bill";
   }
   
   //to view bill
   @RequestMapping(value = { "/bill/view" }, method = RequestMethod.GET)
   public String orderView(Model model, @RequestParam("orderId") String orderId) {
      OrderInfo orderInfo = null;
      if (orderId != null) {
         orderInfo = this.orderDAO.getOrderInfo(orderId);
      }
      if (orderInfo == null) {
         return "redirect:/bill";
      }
      List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfos(orderId);
      orderInfo.setDetails(details);

      model.addAttribute("orderInfo", orderInfo);

      return "billview";
   }


}