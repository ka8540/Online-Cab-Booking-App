package com.masai.services.cabdriver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.masai.entities.Cab;
import com.masai.entities.CabDriver;
import com.masai.entities.CabDriverCabDTO;
import com.masai.repository.CabDriverRepository;
import com.masai.repository.CabRepository;
import com.masai.validation.CabDriverValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CabDriverServiceImpl implements CabDriverService {

	@Autowired
	private CabDriverRepository cabDriverDao;

	@Autowired
	private CabRepository cabDao;

	@Autowired
	private CabDriverValidator validator; // Validation class

	@Override
	public ResponseEntity<CabDriver> insertCabDriver(CabDriverCabDTO cabdto) {
		validator.validateNewCabDriver(cabdto); // Validation

		// Create entities
		Cab cab = createCabFromDTO(cabdto);
		CabDriver cabDriver = createCabDriverFromDTO(cabdto, cab);

		// Save entities
		cab.setCabDriver(cabDriver);
		cabDriverDao.save(cabDriver);

		return new ResponseEntity<>(cabDriver, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<CabDriver> updateCabDriver(CabDriverCabDTO cabdtupdate, String user, String pass) {
		CabDriver existingDriver = validator.validateCredentials(user, pass); // Validate user

		// Update CabDriver and Cab details
		updateCabDriverDetails(existingDriver, cabdtupdate);
		cabDriverDao.save(existingDriver);

		return new ResponseEntity<>(existingDriver, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deleteCabDriver(CabDriver cabDriver) {
		CabDriver existingDriver = validator.validateCredentials(cabDriver.getUsername(), cabDriver.getPassword()); // Validate user
		cabDriverDao.delete(existingDriver);

		return new ResponseEntity<>("Driver with username: " + cabDriver.getUsername() + " deleted", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> updateStatus(CabDriver cabDriver) {
		CabDriver existingDriver = validator.validateCredentials(cabDriver.getUsername(), cabDriver.getPassword()); // Validate user

		if (validator.isTripInProgress(existingDriver)) {
			throw new IllegalStateException("Trip is already in progress");
		}

		existingDriver.setAvailablity(!existingDriver.getAvailablity()); // Toggle availability
		cabDriverDao.save(existingDriver);

		return new ResponseEntity<>("Status Updated Successfully", HttpStatus.OK);
	}

	// Helper methods
	private Cab createCabFromDTO(CabDriverCabDTO cabdto) {
		Cab cab = new Cab();
		cab.setCarType(cabdto.getCarType());
		cab.setNumberPlate(cabdto.getNumberPlate());
		cab.setRatePerKms(cabdto.getRatePerKms());
		return cab;
	}

	private CabDriver createCabDriverFromDTO(CabDriverCabDTO cabdto, Cab cab) {
		CabDriver cabDriver = new CabDriver();
		cabDriver.setAddress(cabdto.getAddress());
		cabDriver.setUsername(cabdto.getUsername());
		cabDriver.setPassword(cabdto.getPassword());
		cabDriver.setMobile(cabdto.getMobile());
		cabDriver.setEmail(cabdto.getEmail());
		cabDriver.setCab(cab);
		cabDriver.setLicenseNumber(cabdto.getLicenseNumber());
		return cabDriver;
	}

	private void updateCabDriverDetails(CabDriver cabDriver, CabDriverCabDTO cabdto) {
		if (cabdto.getUsername() != null) cabDriver.setUsername(cabdto.getUsername());
		if (cabdto.getPassword() != null) cabDriver.setPassword(cabdto.getPassword());
		if (cabdto.getMobile() != null) cabDriver.setMobile(cabdto.getMobile());
		if (cabdto.getAddress() != null) cabDriver.setAddress(cabdto.getAddress());
		if (cabdto.getEmail() != null) cabDriver.setEmail(cabdto.getEmail());

		Cab cab = cabDriver.getCab();
		if (cabdto.getCarType() != null) cab.setCarType(cabdto.getCarType());
		if (cabdto.getNumberPlate() != null) cab.setNumberPlate(cabdto.getNumberPlate());
		if (cabdto.getRatePerKms() != null) cab.setRatePerKms(cabdto.getRatePerKms());
	}
}
