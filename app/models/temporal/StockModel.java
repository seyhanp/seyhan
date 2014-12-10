/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

@Entity
@Sql
/**
 * @author mdpinar
*/
public class StockModel {

	public Integer id;
	public String code;
	public String name;
	public Integer number = 1;
	public String excCode;
	public Double buyTax;
	public Double sellTax;
	public Double taxRate2;
	public Double taxRate3;
	public Double buyPrice;
	public Double sellPrice;
	public Double discountRate1;
	public Double discountRate2;
	public Double discountRate3;
	public String unit1;
	public String unit2;
	public String unit3;
	public Double unit2ratio;
	public Double unit3ratio;

	public String prefix;
	public String suffix;
	public String unitNo;

}
