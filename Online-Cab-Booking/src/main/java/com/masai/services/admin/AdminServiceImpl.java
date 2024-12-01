package com.masai.services.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.masai.entities.Admin;
import com.masai.entities.Cab;
import com.masai.entities.Customer;
import com.masai.entities.TripDetails;
import com.masai.exceptions.UserDoesNotExist;
import com.masai.exceptions.UserNameAlreadyExist;
import com.masai.repository.AdminRepository;
import com.masai.repository.CabRepository;
import com.masai.repository.CustomerRepository;
import com.masai.repository.TripDetailsRepository;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private AdminRepository adminDao;

	@Autowired
	private CustomerRepository customerDao;

	@Autowired
	private CabRepository cabDao;

	@Autowired
	private TripDetailsRepository tripDetailsDao;

	// Singleton instance
	private static AdminServiceImpl instance;

	// Private constructor
	private AdminServiceImpl() {
	}

	// Public method to get the singleton instance
	public static synchronized AdminServiceImpl getInstance() {
		if (instance == null) {
			instance = new AdminServiceImpl();
		}
		return instance;
	}

	// Helper method for admin authentication
	private Admin authenticateAdmin(String username, String password) {
		Admin admin = adminDao.findByUsernameAndPassword(username, password);
		if (admin == null) {
			throw new UserDoesNotExist("Invalid username or password");
		}
		return admin;
	}

	// Helper method for retrieving a Cab by ID
	private Cab findCabById(Integer cabId) {
		return cabDao.findById(cabId)
				.orElseThrow(() -> new UserDoesNotExist("Cab with ID " + cabId + " does not exist"));
	}

	// Helper method for retrieving a Customer by username
	private Customer findCustomerByUsername(String username) {
		Customer customer = customerDao.findByUsername(username);
		if (customer == null) {
			throw new UserDoesNotExist("Customer does not exist");
		}
		return customer;
	}

	@Override
	public ResponseEntity<Admin> insertAdmin(Admin admin) {
		if (adminDao.findByUsername(admin.getUsername()) != null) {
			throw new UserNameAlreadyExist("Username already exists");
		}
		adminDao.save(admin);
		return new ResponseEntity<>(admin, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Admin> updateAdmin(Admin admin, String user, String pass) {
		Admin authenticatedAdmin = authenticateAdmin(user, pass);
		if (admin.getUsername() != null)
			authenticatedAdmin.setUsername(admin.getUsername());
		if (admin.getPassword() != null)
			authenticatedAdmin.setPassword(admin.getPassword());
		adminDao.save(authenticatedAdmin);
		return new ResponseEntity<>(authenticatedAdmin, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deleteAdmin(Admin admin) {
		Admin authenticatedAdmin = authenticateAdmin(admin.getUsername(), admin.getPassword());
		adminDao.delete(authenticatedAdmin);
		return new ResponseEntity<>("Admin deleted successfully", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<TripDetails>> getAllTrips(Admin admin) {
		authenticateAdmin(admin.getUsername(), admin.getPassword());
		List<TripDetails> allTrips = tripDetailsDao.findAll();
		return new ResponseEntity<>(allTrips, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<TripDetails>> getAllTripsByCustomer(Admin admin, String username) {
		authenticateAdmin(admin.getUsername(), admin.getPassword());
		Customer customer = findCustomerByUsername(username);
		return new ResponseEntity<>(customer.getTripDetailsList(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<TripDetails>> getAllTripsByCab(Admin admin, Integer cabId) {
		authenticateAdmin(admin.getUsername(), admin.getPassword());
		Cab cab = findCabById(cabId);
		List<TripDetails> tripsByCab = cab.getCabDriver().getTripDetailsList();
		return new ResponseEntity<>(tripsByCab, HttpStatus.OK);
	}
}
