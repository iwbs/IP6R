package com.joe.entity;

import java.util.List;

public class Account {

	private String id;
	private String password;
	private String firstName;
	private String lastName;
	private String govtId;
	private String govtIdType;
	private String phoneNumber;
	private List<String> targetProductList;
	private String targetProductListStr;
	private List<String> targetShopList;
	private String timeslot;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public List<String> getTargetProductList() {
		return targetProductList;
	}

	public void setTargetProductList(List<String> targetProductList) {
		this.targetProductList = targetProductList;
	}

	public String getTargetProductListStr() {
		return targetProductListStr;
	}

	public void setTargetProductListStr(String targetProductListStr) {
		this.targetProductListStr = targetProductListStr;
	}

	public List<String> getTargetShopList() {
		return targetShopList;
	}

	public void setTargetShopList(List<String> targetShopList) {
		this.targetShopList = targetShopList;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGovtId() {
		return govtId;
	}

	public void setGovtId(String govtId) {
		this.govtId = govtId;
	}

	public String getGovtIdType() {
		return govtIdType;
	}

	public void setGovtIdType(String govtIdType) {
		this.govtIdType = govtIdType;
	}

	public String getTimeslot() {
		return timeslot;
	}

	public void setTimeslot(String timeslot) {
		this.timeslot = timeslot;
	}

}
