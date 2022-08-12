package com.masai.services.cabdriver;

import org.springframework.http.ResponseEntity;

import com.masai.entities.CabDriver;
import com.masai.entities.CabDriverCabDTO;

public interface CabDriverService {
	
	public ResponseEntity<CabDriver> insertCabDriver(CabDriverCabDTO cabdto);

}