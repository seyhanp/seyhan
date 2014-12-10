/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package external.model;

import java.util.Date;

/**
 * @author mdpinar
*/
public class TCMBRate {

	 private Date date;
	 private String code;
	 private String name;
	 private Double excBuying;
	 private Double excSelling;
	 private Double effBuying;
	 private Double effSelling;
	 
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getExcBuying() {
		return excBuying;
	}

	public void setExcBuying(Double excBuying) {
		this.excBuying = excBuying;
	}

	public Double getExcSelling() {
		return excSelling;
	}

	public void setExcSelling(Double excSelling) {
		this.excSelling = excSelling;
	}

	public Double getEffBuying() {
		return effBuying;
	}

	public void setEffBuying(Double effBuying) {
		this.effBuying = effBuying;
	}

	public Double getEffSelling() {
		return effSelling;
	}

	public void setEffSelling(Double effSelling) {
		this.effSelling = effSelling;
	}

}
