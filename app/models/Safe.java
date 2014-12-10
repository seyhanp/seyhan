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
public class Safe extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.KASA_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(50)
	public String name;

	@Constraints.MaxLength(3)
	public String excCode;

	@Constraints.MaxLength(30)
	public String responsible;

	public Boolean isActive = Boolean.TRUE;

	public static Map<String, String> options() {
		return ModelHelper.options(RIGHT);
	}

	public static List<Safe> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static Safe findById(Integer id) {
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
