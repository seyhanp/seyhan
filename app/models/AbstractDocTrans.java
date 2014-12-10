/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.MappedSuperclass;

import play.data.validation.Constraints.Required;
import enums.Right;

@MappedSuperclass
/**
 * @author mdpinar
*/
public abstract class AbstractDocTrans extends AbstractBaseTrans {

	private static final long serialVersionUID = 1L;

	@Required
	public Double amount = 0d;
	public Double debt = 0d;
	public Double credit = 0d;

	public AbstractDocTrans(Right right) {
		super(right);
	}

}
