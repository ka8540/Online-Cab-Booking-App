package com.masai.services.cabdriver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.masai.entities.Cab;
import com.masai.entities.CabDriver;
import com.masai.entities.CabDriverCabDTO;
import com.masai.entities.TripDetails;
import com.masai.exceptions.TripInProgress;
import com.masai.exceptions.UserDoesNotExist;
import com.masai.exceptions.UserNameAlreadyExist;
import com.masai.repository.CabDriverRepository;
import com.masai.repository.CabRepository;

@Service
public class CabDriverServiceImpl implements CabDriverService {

    @Autowired
    private CabDriverRepository cabDriverDao;
    
    @Autowired
    private CabRepository cabDao;

    // Structural Helper Method: Check if username already exists
    private void validateUsernameExists(String username) {
        CabDriver existingDriver = cabDriverDao.findByUsername(username);
        if (existingDriver != null) {
            throw new UserNameAlreadyExist("Username already exists");
        }
    }

    // Structural Helper Method: Check if license number already exists
    private void validateLicenseNumberExists(String licenseNumber) {
        CabDriver existingDriver = cabDriverDao.findByLicenseNumber(licenseNumber);
        if (existingDriver != null) {
            throw new UserNameAlreadyExist("License number already registered");
        }
    }

    // Structural Helper Method: Check if number plate already exists
    private void validateNumberPlateExists(String numberPlate) {
        Cab existingCab = cabDao.findByNumberPlate(numberPlate);
        if (existingCab != null) {
            throw new UserNameAlreadyExist("Number Plate already registered");
        }
    }

    // Structural Helper Method: Update Cab Driver fields based on CabDriverCabDTO
    private void updateCabDriverFields(CabDriver cabDriver, CabDriverCabDTO cabdto) {
        if (cabdto.getUsername() != null) {
            validateUsernameExists(cabdto.getUsername());
            cabDriver.setUsername(cabdto.getUsername());
        }
        if (cabdto.getPassword() != null) cabDriver.setPassword(cabdto.getPassword());
        if (cabdto.getMobile() != null) cabDriver.setMobile(cabdto.getMobile());
        if (cabdto.getAddress() != null) cabDriver.setAddress(cabdto.getAddress());
        if (cabdto.getEmail() != null) cabDriver.setEmail(cabdto.getEmail());
    }

    // Structural Helper Method: Update Cab fields based on CabDriverCabDTO
    private void updateCabFields(Cab cab, CabDriverCabDTO cabdto) {
        if (cabdto.getCarType() != null) cab.setCarType(cabdto.getCarType());
        if (cabdto.getRatePerKms() != null) cab.setRatePerKms(cabdto.getRatePerKms());
        if (cabdto.getNumberPlate() != null) {
            validateNumberPlateExists(cabdto.getNumberPlate());
            cab.setNumberPlate(cabdto.getNumberPlate());
        }
    }

    // Structural Helper Method: Authenticate and get CabDriver by username and password
    private CabDriver authenticateCabDriver(String username, String password) {
        CabDriver cabDriver = cabDriverDao.findByUsernameAndPassword(username, password);
        if (cabDriver == null) {
            throw new UserDoesNotExist("Username or Password is wrong");
        }
        return cabDriver;
    }

    @Override
    public ResponseEntity<CabDriver> insertCabDriver(CabDriverCabDTO cabdto) {
        validateUsernameExists(cabdto.getUsername());
        validateLicenseNumberExists(cabdto.getLicenseNumber());
        validateNumberPlateExists(cabdto.getNumberPlate());

        // Create new Cab and CabDriver
        Cab cab = new Cab();
        cab.setCarType(cabdto.getCarType());
        cab.setNumberPlate(cabdto.getNumberPlate());
        cab.setRatePerKms(cabdto.getRatePerKms());

        CabDriver cabDriver = new CabDriver();
        cabDriver.setAddress(cabdto.getAddress());
        cabDriver.setUsername(cabdto.getUsername());
        cabDriver.setPassword(cabdto.getPassword());
        cabDriver.setMobile(cabdto.getMobile());
        cabDriver.setEmail(cabdto.getEmail());
        cabDriver.setCab(cab);
        cabDriver.setLicenseNumber(cabdto.getLicenseNumber());

        cab.setCabDriver(cabDriver); // Set back reference

        cabDriverDao.save(cabDriver);
        return new ResponseEntity<>(cabDriver, HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<CabDriver> updateCabDriver(CabDriverCabDTO cabdto, String user, String pass) {
        CabDriver cabDriver = authenticateCabDriver(user, pass);
        Cab cab = cabDriver.getCab(); // Get the associated Cab

        updateCabDriverFields(cabDriver, cabdto); // Update driver fields
        updateCabFields(cab, cabdto); // Update cab fields

        cabDriverDao.save(cabDriver);
        return new ResponseEntity<>(cabDriver, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteCabDriver(CabDriver cabDriver) {
        CabDriver existingDriver = authenticateCabDriver(cabDriver.getUsername(), cabDriver.getPassword());
        cabDriverDao.delete(existingDriver);
        return new ResponseEntity<>("Driver with username: " + cabDriver.getUsername() + " deleted", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> updateStatus(CabDriver cabDriver) {
        CabDriver existingDriver = authenticateCabDriver(cabDriver.getUsername(), cabDriver.getPassword());
        List<TripDetails> tripList = existingDriver.getTripDetailsList();

        // Check if there is an ongoing trip
        if (tripList.size() > 0) {
            TripDetails lastTripDetails = tripList.get(tripList.size() - 1);
            if (!lastTripDetails.getStatus()) {
                throw new TripInProgress("Trip is already in progress");
            }
        }

        existingDriver.setAvailablity(!existingDriver.getAvailablity()); // Toggle availability status
        cabDriverDao.save(existingDriver);
        return new ResponseEntity<>("Status Updated Successfully", HttpStatus.ACCEPTED);
    }
}
