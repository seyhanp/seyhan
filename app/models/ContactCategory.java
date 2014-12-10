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
import enums.TransType;

@Entity
/**
 * @author mdpinar
*/
public class ContactCategory extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.CARI_KATEGORI_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public TransType workingDir;
	public Double debtLimit = 0d;
	public Double creditLimit = 0d;

	public Boolean isActive = Boolean.TRUE;

	public ContactCategory() {
		super();
	}

	public ContactCategory(String name) {
		this();
		this.name = name;
	}

	public static Map<String, String> options() {
		return ModelHelper.options(RIGHT);
	}

	public static List<ContactCategory> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static ContactCategory findById(Integer id) {
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
