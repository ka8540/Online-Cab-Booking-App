package com.masai.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Admin extends User implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer adminId;

	private String username;
	private String password;
	private String email;

	// Getters and Setters
	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// Constructor
	public Admin(Integer adminId, String username, String password, String email) {
		super();
		this.adminId = adminId;
		this.username = username;
		this.password = password;
		this.email = email;
	}

	public Admin() {
		// Default constructor
	}

	// Clone method for Prototype Design Pattern
	@Override
	public Admin clone() {
		try {
			return (Admin) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Cloning failed for Admin object", e);
		}
	}
}
