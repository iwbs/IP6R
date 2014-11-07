package com.joe.entity;

import java.util.HashMap;
import java.util.Map;

public class Shop {
	String storeName;
	String storeNumber;
	String enabled;
	String formattedDate;
	Map<String, String> timeslots;
	Map<String, Boolean> inventories = new HashMap<>();

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreNumber() {
		return storeNumber;
	}

	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	public Map<String, String> getTimeslots() {
		return timeslots;
	}

	public void setTimeslots(Map<String, String> timeslots) {
		this.timeslots = timeslots;
	}

	public Map<String, Boolean> getInventories() {
		return inventories;
	}

	public void setInventories(Map<String, Boolean> inventories) {
		this.inventories = inventories;
	}

}
