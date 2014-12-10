/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.db.ebean.Model;
import enums.Right;

@MappedSuperclass
/**
 * @author mdpinar
*/
public abstract class AbstractStockTransRelation extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Integer relId;
	public Right relRight;
	public Integer relReceiptNo;

}
