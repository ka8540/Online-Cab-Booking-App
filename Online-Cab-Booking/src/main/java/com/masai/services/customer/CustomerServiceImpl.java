package com.masai.services.customer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.masai.entities.Customer;
import com.masai.entities.TripDetails;
import com.masai.exceptions.UserDoesNotExist;
import com.masai.exceptions.UserNameAlreadyExist;
import com.masai.repository.CustomerRepository;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerDao;

	// Singleton instance
	private static CustomerServiceImpl instance;

	// Private constructor
	private CustomerServiceImpl() {
	}

	// Public method to get the singleton instance
	public static synchronized CustomerServiceImpl getInstance() {
		if (instance == null) {
			instance = new CustomerServiceImpl();
		}
		return instance;
	}

	@Override
	public ResponseEntity<Customer> insertCustomer(Customer customer) {
		Customer cust = customerDao.findByUsername(customer.getUsername());
		if (cust != null)
			throw new UserNameAlreadyExist("Username already exists");
		customerDao.save(customer);
		return new ResponseEntity<>(customer, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Customer> updateCustomer(Customer customer, String user, String pass) {
		Customer cust = customerDao.findByUsernameAndPassword(user, pass);
		if (cust == null)
			throw new UserDoesNotExist("Invalid username or password");
		if (customer.getUsername() != null)
			cust.setUsername(customer.getUsername());
		if (customer.getPassword() != null)
			cust.setPassword(customer.getPassword());
		customerDao.save(cust);
		return new ResponseEntity<>(cust, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deleteCustomer(Customer customer) {
		Customer cust = customerDao.findByUsernameAndPassword(customer.getUsername(), customer.getPassword());
		if (cust == null)
			throw new UserDoesNotExist("Invalid username or password");
		customerDao.delete(cust);
		return new ResponseEntity<>("Customer deleted successfully", HttpStatus.OK);
	}
}
