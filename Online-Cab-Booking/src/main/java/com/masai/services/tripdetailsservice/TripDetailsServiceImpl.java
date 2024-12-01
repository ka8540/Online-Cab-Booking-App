package com.masai.services.tripdetailsservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.masai.entities.BillDetails;
import com.masai.entities.CabDriver;
import com.masai.entities.Customer;
import com.masai.entities.TripDetails;
import com.masai.entities.TripDetailsDTO;
import com.masai.exceptions.CabDriverNotAvailableException;
import com.masai.exceptions.TripInProgress;
import com.masai.exceptions.UserDoesNotExist;
import com.masai.repository.CabDriverRepository;
import com.masai.repository.CustomerRepository;
import com.masai.repository.TripDetailsRepository;

@Service
public class TripDetailsServiceImpl implements TripDetailsService {

	@Autowired
	private CustomerRepository customerDao;

	@Autowired
	private CabDriverRepository cabDriverDao;

	@Autowired
	private TripDetailsRepository tripDetailsDao;

	// Prototype Instance for TripDetails
	private final TripDetails tripPrototype = new TripDetails();

	// Helper: Authenticate a customer
	private Customer authenticateCustomer(String username, String password) {
		Customer customer = customerDao.findByUsernameAndPassword(username, password);
		if (customer == null) {
			throw new UserDoesNotExist("Invalid username or password");
		}
		return customer;
	}

	// Helper: Find an available CabDriver
	private CabDriver findAvailableCabDriver() {
		return cabDriverDao.findAll().stream()
				.filter(CabDriver::getAvailablity)
				.findFirst()
				.orElseThrow(() -> new CabDriverNotAvailableException("No Driver Available at the moment"));
	}

	// Helper: Clone and customize TripDetails prototype
	private TripDetails createTripPrototype(Customer customer, CabDriver cabDriver, TripDetailsDTO tripDto,
			float distance) {
		TripDetails trip = tripPrototype.clone();
		trip.setCustomer(customer);
		trip.setCabDriver(cabDriver);
		trip.setFromLocation(tripDto.getFromLocation());
		trip.setToLocation(tripDto.getToLocation());
		trip.setDistance(distance);
		return trip;
	}

	// Helper: Calculate random distance
	private float calculateRandomDistance() {
		int min = 10, max = 100;
		return (float) Math.floor(Math.random() * (max - min + 1) + min);
	}

	@Override
	public ResponseEntity<TripDetails> insertTripDetails(TripDetailsDTO tripDto) {
		Customer customer = authenticateCustomer(tripDto.getUsername(), tripDto.getPassword());

		// Check for ongoing trips
		if (customer.getTripDetailsList().stream().anyMatch(trip -> !trip.getStatus())) {
			throw new TripInProgress("Cannot book another trip while one is in progress");
		}

		CabDriver cabDriver = findAvailableCabDriver();
		float distance = calculateRandomDistance();
		TripDetails tripDetails = createTripPrototype(customer, cabDriver, tripDto, distance);

		// Update associations
		cabDriver.setAvailablity(false);
		cabDriver.getTripDetailsList().add(tripDetails);
		customer.getTripDetailsList().add(tripDetails);

		tripDetailsDao.save(tripDetails);
		return new ResponseEntity<>(tripDetails, HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<String> deleteBookedTrip(TripDetailsDTO tripDto) {
		Customer customer = authenticateCustomer(tripDto.getUsername(), tripDto.getPassword());

		TripDetails ongoingTrip = customer.getTripDetailsList().stream()
				.filter(trip -> !trip.getStatus())
				.findFirst()
				.orElseThrow(() -> new TripInProgress("No active trip found"));

		// Update cab driver availability and remove trip
		CabDriver cabDriver = ongoingTrip.getCabDriver();
		cabDriver.setAvailablity(true);
		customer.getTripDetailsList().remove(ongoingTrip);

		cabDriverDao.save(cabDriver);
		customerDao.save(customer);

		return new ResponseEntity<>("Trip cancelled successfully", HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<List<TripDetails>> getAllTripsOfCustomer(TripDetailsDTO tripDto) {
		Customer customer = authenticateCustomer(tripDto.getUsername(), tripDto.getPassword());
		return new ResponseEntity<>(customer.getTripDetailsList(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> calculateBill(TripDetailsDTO tripDto) {
		CabDriver cabDriver = cabDriverDao.findByUsernameAndPassword(tripDto.getUsername(), tripDto.getPassword());
		if (cabDriver == null) {
			throw new UserDoesNotExist("Invalid username or password");
		}

		TripDetails ongoingTrip = cabDriver.getTripDetailsList().stream()
				.filter(trip -> !trip.getStatus())
				.findFirst()
				.orElseThrow(() -> new TripInProgress("No active trip found"));

		float totalFare = ongoingTrip.getDistance() * cabDriver.getCab().getRatePerKms();
		ongoingTrip.setTotalFare(totalFare);
		ongoingTrip.setStatus(true);
		cabDriver.setAvailablity(true);

		cabDriverDao.save(cabDriver);
		return new ResponseEntity<>("Bill is " + totalFare, HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<BillDetails> generateBill(TripDetailsDTO tripDto) {
		Customer customer = authenticateCustomer(tripDto.getUsername(), tripDto.getPassword());

		TripDetails tripDetails = tripDetailsDao.findById(tripDto.getTripId())
				.orElseThrow(() -> new TripInProgress("Trip with given ID does not exist"));

		if (!customer.getUsername().equals(tripDetails.getCustomer().getUsername())) {
			throw new UserDoesNotExist("User not verified");
		}

		if (!tripDetails.getStatus()) {
			throw new TripInProgress("Trip not completed yet");
		}

		BillDetails billDetails = new BillDetails();
		billDetails.setDistance(tripDetails.getDistance());
		billDetails.setRatePerKms(tripDetails.getCabDriver().getCab().getRatePerKms());
		billDetails.setTotalBill(tripDetails.getTotalFare());

		return new ResponseEntity<>(billDetails, HttpStatus.ACCEPTED);
	}
}
