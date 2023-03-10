package com.retail.delight.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import com.retail.delight.dao.AccountDAO;
import com.retail.delight.entity.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService  {
	@Autowired
    private AccountDAO accountDAO;
	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    
    /* (non-Javadoc)
	 * @see com.retail.delight.service.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
    @Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	logger.info("Fetching Account using username {}",username);
    	Account account = accountDAO.findAccount(username);
    	
        System.out.println("Account= " + account);

        if (account == null) {
        	logger.debug("The Account with username {} not found in the database",username);
            throw new UsernameNotFoundException("User " //
                    + username + " was not found in the database");
        }

        // EMPLOYEE,MANAGER,..
        String role = account.getUserRole();

        List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();

        // ROLE_EMPLOYEE, ROLE_MANAGER
        GrantedAuthority authority = new SimpleGrantedAuthority(role);

        grantList.add(authority);

        boolean enabled = account.isActive();
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        UserDetails userDetails = (UserDetails) new User(account.getUserName(), //
                account.getEncrytedPassword(), enabled, accountNonExpired, //
                credentialsNonExpired, accountNonLocked, grantList);
    	logger.info("The userdetails has been succesfully fetched");
        return userDetails;
    }

}
