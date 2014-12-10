/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.ModelHelper;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class BankExpense extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.BANK_MASRAF_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public Boolean isActive = Boolean.TRUE;

	public BankExpense(String name) {
		super();
		this.name = name;
	}

	public static Map<String, String> options() {
		return ModelHelper.options(RIGHT);
	}

	public static List<BankExpense> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static BankExpense findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id);
	}

	@Override
	public Right getAuditRight() {
		return RIGHT;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.name") + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
