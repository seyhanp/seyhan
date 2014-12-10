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
public class OrderActionModel {

	public Integer id;
	public String transDate;
	public String delvieryDate;
	public String contactName;

	/**
	 * For receipt action
	 */
	public String description;

	/**
	 * For row action
	 */
	public String stockName;
	public Double quantity;
	public String unit;
	public Double price;

	/**
	 * For both actions
	 */
	public Double total;

}
