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
public class StockCostFactorModel {

	public Integer id;
	public String name;
	public String factorType;
	public String calcType;
	public String effectType;
	public Double effect;
	public String factorTypeOri;
	public String calcTypeOri;
	public String effectTypeOri;

}
