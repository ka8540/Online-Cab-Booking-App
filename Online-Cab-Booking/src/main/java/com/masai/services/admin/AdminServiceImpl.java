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
import com.masai.repository.CabDriverRepository;
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
    private CabDriverRepository cabDriverDao;

    @Autowired
    private TripDetailsRepository tripDetailsDao;

    @Autowired
    private CabRepository cabDao;

    // Prototype instance of Admin
    private final Admin adminPrototype = new Admin();

    // Helper method to authenticate an admin
    private Admin authenticateAdmin(String username, String password) {
        Admin admin = adminDao.findByUsernameAndPassword(username, password);
        if (admin == null) {
            throw new UserDoesNotExist("Invalid username or password");
        }
        return admin;
    }

    // Helper method to clone and customize an Admin object
    private Admin createAdminPrototype(Admin admin) {
        Admin clonedAdmin = adminPrototype.clone();
        clonedAdmin.setUsername(admin.getUsername());
        clonedAdmin.setPassword(admin.getPassword());
        clonedAdmin.setEmail(admin.getEmail());
        clonedAdmin.setAddress(admin.getAddress());
        clonedAdmin.setMobile(admin.getMobile());
        return clonedAdmin;
    }

    // Structural Helper Method: Validate if Admin exists by username
    private void validateAdminExistenceByUsername(String username) {
        if (adminDao.findByUsername(username) != null) {
            throw new UserNameAlreadyExist("Username already exists");
        }
    }

    // Structural Helper Method: Update Admin fields conditionally
    private void updateAdminFields(Admin existingAdmin, Admin admin) {
        if (admin.getUsername() != null) {
            validateAdminExistenceByUsername(admin.getUsername());
            existingAdmin.setUsername(admin.getUsername());
        }
        if (admin.getPassword() != null)
            existingAdmin.setPassword(admin.getPassword());
        if (admin.getEmail() != null)
            existingAdmin.setEmail(admin.getEmail());
        if (admin.getAddress() != null)
            existingAdmin.setAddress(admin.getAddress());
        if (admin.getMobile() != null)
            existingAdmin.setMobile(admin.getMobile());
    }

    @Override
    public ResponseEntity<Admin> insertAdmin(Admin admin) {
        validateAdminExistenceByUsername(admin.getUsername());
        Admin newAdmin = createAdminPrototype(admin);
        adminDao.save(newAdmin);
        return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Admin> updateAdmin(Admin admin, String user, String pass) {
        Admin existingAdmin = authenticateAdmin(user, pass);
        updateAdminFields(existingAdmin, admin); // Reusing helper method to update fields
        adminDao.save(existingAdmin);
        return new ResponseEntity<>(existingAdmin, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteAdmin(Admin admin) {
        Admin existingAdmin = authenticateAdmin(admin.getUsername(), admin.getPassword());
        adminDao.delete(existingAdmin);
        return new ResponseEntity<>("Admin with username: " + admin.getUsername() + " deleted successfully",
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TripDetails>> getAllTrips(Admin admin) {
        authenticateAdmin(admin.getUsername(), admin.getPassword());
        List<TripDetails> allTrips = tripDetailsDao.findAll();
        return new ResponseEntity<>(allTrips, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TripDetails>> getAllTripsByCab(Admin admin, Integer cabId) {
        authenticateAdmin(admin.getUsername(), admin.getPassword());
        Cab cab = cabDao.findById(cabId).orElseThrow(() -> new UserDoesNotExist("Cab does not exist"));
        return new ResponseEntity<>(cab.getCabDriver().getTripDetailsList(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TripDetails>> getAllTripsByCustomer(Admin admin, String username) {
        authenticateAdmin(admin.getUsername(), admin.getPassword());
        Customer customer = customerDao.findByUsername(username);
        if (customer == null) {
            throw new UserDoesNotExist("Customer does not exist");
        }
        return new ResponseEntity<>(customer.getTripDetailsList(), HttpStatus.OK);
    }
}
