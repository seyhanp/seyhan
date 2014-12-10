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
public class ContactModel {

	public Integer id;
	public String code;
	public String name;
	public String taxOffice;
	public String taxNumber;
	public String address1;
	public String address2;

}
