package com.masai.validation;

import com.masai.entities.Cab;
import com.masai.entities.CabDriver;
import com.masai.entities.CabDriverCabDTO;
import com.masai.exceptions.UserDoesNotExist;
import com.masai.exceptions.UserNameAlreadyExist;
import com.masai.repository.CabDriverRepository;
import com.masai.repository.CabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CabDriverValidator {

    @Autowired
    private CabDriverRepository cabDriverDao;

    @Autowired
    private CabRepository cabDao;

    // Validate if a new CabDriver's credentials are unique
    public void validateNewCabDriver(CabDriverCabDTO cabdto) {
        if (cabDriverDao.findByUsername(cabdto.getUsername()) != null) {
            throw new UserNameAlreadyExist("Username Already Exists");
        }
        if (cabDriverDao.findByLicenseNumber(cabdto.getLicenseNumber()) != null) {
            throw new UserNameAlreadyExist("License Number Already Registered");
        }
        if (cabDao.findByNumberPlate(cabdto.getNumberPlate()) != null) {
            throw new UserNameAlreadyExist("Number Plate Already Registered");
        }
    }

    // Validate credentials of an existing CabDriver
    public CabDriver validateCredentials(String username, String password) {
        CabDriver cabDriver = cabDriverDao.findByUsernameAndPassword(username, password);
        if (cabDriver == null) {
            throw new UserDoesNotExist("Invalid username or password");
        }
        return cabDriver;
    }

    // Validate if a CabDriver is free (no ongoing trips)
    public boolean isTripInProgress(CabDriver cabDriver) {
        return cabDriver.getTripDetailsList()
                .stream()
                .anyMatch(trip -> !trip.getStatus()); // If any trip status is false (in progress)
    }
}

//package com.masai.validation;
//
//import com.masai.entities.CabDriver;
//import com.masai.entities.CabDriverCabDTO;
//import com.masai.exceptions.UserDoesNotExist;
//import com.masai.exceptions.UserNameAlreadyExist;
//import com.masai.repository.CabDriverRepository;
//import com.masai.repository.CabRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CabDriverValidator {
//
//    @Autowired
//    private CabDriverRepository cabDriverDao;
//
//    @Autowired
//    private CabRepository cabDao;
//
//    // Validate if a new CabDriver's credentials are unique
//    public void validateNewCabDriver(CabDriverCabDTO cabdto) {
//        validateUsername(cabdto.getUsername());
//        validateLicenseNumber(cabdto.getLicenseNumber());
//        validateNumberPlate(cabdto.getNumberPlate());
//    }
//
//    // Validate credentials of an existing CabDriver
//    public CabDriver validateCredentials(String username, String password) {
//        return findCabDriverByUsernameAndPassword(username, password);
//    }
//
//    // Validate if a CabDriver is free (no ongoing trips)
//    public boolean isTripInProgress(CabDriver cabDriver) {
//        return cabDriver.getTripDetailsList()
//                .stream()
//                .anyMatch(trip -> !trip.getStatus());
//    }
//
//    // Utility method to validate username uniqueness
//    private void validateUsername(String username) {
//        if (cabDriverDao.findByUsername(username) != null) {
//            throw new UserNameAlreadyExist("Username Already Exists");
//        }
//    }
//
//    // Utility method to validate license number uniqueness
//    private void validateLicenseNumber(String licenseNumber) {
//        if (cabDriverDao.findByLicenseNumber(licenseNumber) != null) {
//            throw new UserNameAlreadyExist("License Number Already Registered");
//        }
//    }
//
//    // Utility method to validate number plate uniqueness
//    private void validateNumberPlate(String numberPlate) {
//        if (cabDao.findByNumberPlate(numberPlate) != null) {
//            throw new UserNameAlreadyExist("Number Plate Already Registered");
//        }
//    }
//
//    // Utility method to find a CabDriver by username and password
//    private CabDriver findCabDriverByUsernameAndPassword(String username, String password) {
//        CabDriver cabDriver = cabDriverDao.findByUsernameAndPassword(username, password);
//        if (cabDriver == null) {
//            throw new UserDoesNotExist("Invalid username or password");
//        }
//        return cabDriver;
//    }
//}
//
