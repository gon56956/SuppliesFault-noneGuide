package com.zzmetro.suppliesfault.model;

public class SparepartList {

	//物资简介
	private String sparepartInfo;
	//总量
	private String number;

	public SparepartList(String sparepartInfo, String number) {
		this.sparepartInfo = sparepartInfo;
		this.number = number;
	}

	public String getSparepartInfo() {
		return sparepartInfo;
	}

	public String getNumber() {
		return number;
	}

}
