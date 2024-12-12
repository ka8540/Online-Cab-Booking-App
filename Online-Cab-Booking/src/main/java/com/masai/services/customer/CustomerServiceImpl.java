package com.masai.services.customer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.masai.entities.CabDriver;
import com.masai.entities.Customer;
import com.masai.entities.TripDetails;
import com.masai.exceptions.UserDoesNotExist;
import com.masai.exceptions.UserNameAlreadyExist;
import com.masai.repository.CabDriverRepository;
import com.masai.repository.CustomerRepository;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerService decoratedService;

    @Autowired
    private CustomerRepository customerDao;
    
    @Autowired
    private CabDriverRepository cabDriverDao;

    // Constructor Injection to accept the decorated service (Proxy/Decorator)
    @Autowired
    public CustomerServiceImpl(CustomerService decoratedService) {
        this.decoratedService = decoratedService;
    }

    // Structural Helper Method: Check if username already exists
    private void validateUsernameExists(String username) {
        Customer existingCustomer = customerDao.findByUsername(username);
        if (existingCustomer != null) {
            throw new UserNameAlreadyExist("Username already exists");
        }
    }

    // Structural Helper Method: Authenticate and get Customer by username and password
    private Customer authenticateCustomer(String username, String password) {
        Customer customer = customerDao.findByUsernameAndPassword(username, password);
        if (customer == null) {
            throw new UserDoesNotExist("Username or Password is wrong");
        }
        return customer;
    }

    // Structural Helper Method: Update Customer fields based on Customer object
    private void updateCustomerFields(Customer existingCustomer, Customer newCustomerData) {
        if (newCustomerData.getUsername() != null) {
            validateUsernameExists(newCustomerData.getUsername());
            existingCustomer.setUsername(newCustomerData.getUsername());
        }
        if (newCustomerData.getPassword() != null) existingCustomer.setPassword(newCustomerData.getPassword());
        if (newCustomerData.getEmail() != null) existingCustomer.setEmail(newCustomerData.getEmail());
        if (newCustomerData.getAddress() != null) existingCustomer.setAddress(newCustomerData.getAddress());
        if (newCustomerData.getMobile() != null) existingCustomer.setMobile(newCustomerData.getMobile());
    }

    // Proxy Pattern: Authentication before performing the actual operations
    private boolean checkAuthentication(String username, String password) {
        // Here, you can add a real authentication mechanism if necessary
        return "admin".equals(username);  // Mock check: Only "admin" can perform actions
    }

    @Override
    public ResponseEntity<Customer> insertCustomer(Customer customer) {
        if (!checkAuthentication(customer.getUsername(), customer.getPassword())) {
            throw new UserDoesNotExist("Unauthorized access to insert customer");
        }

        validateUsernameExists(customer.getUsername());  // Validate username uniqueness
        customerDao.save(customer);  // Save new customer
        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Customer> updateCustomer(Customer customer, String user, String pass) {
        if (!checkAuthentication(user, pass)) {
            throw new UserDoesNotExist("Unauthorized access to update customer");
        }

        Customer existingCustomer = authenticateCustomer(user, pass);  // Authenticate user
        
        // Update fields of the existing customer
        updateCustomerFields(existingCustomer, customer);
        
        customerDao.save(existingCustomer);  // Save updated customer
        return new ResponseEntity<>(existingCustomer, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteCustomer(Customer customer) {
        if (!checkAuthentication(customer.getUsername(), customer.getPassword())) {
            throw new UserDoesNotExist("Unauthorized access to delete customer");
        }

        Customer existingCustomer = authenticateCustomer(customer.getUsername(), customer.getPassword());  // Authenticate user
        
        // Handle cancellation of trip if there are active trips
        List<TripDetails> tripDetailsList = existingCustomer.getTripDetailsList();
        if (tripDetailsList.size() > 0) {
            TripDetails lastTripDetails = tripDetailsList.get(tripDetailsList.size() - 1);
            if (!lastTripDetails.getStatus()) {
                CabDriver cabDriver = lastTripDetails.getCabDriver();
                cabDriver.setAvailablity(true);  // Make the cab available again
                cabDriverDao.save(cabDriver);  // Save updated cab driver
                tripDetailsList.remove(tripDetailsList.size() - 1);  // Remove the last trip
                customerDao.save(existingCustomer);  // Save updated customer
            }
        }

        // Delete the customer
        customerDao.delete(existingCustomer);
        return new ResponseEntity<>("Customer with username: " + customer.getUsername() + " deleted", HttpStatus.OK);
    }
}