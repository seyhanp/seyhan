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
public class InvoiceTransSource extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.FATR_FATURA_KAYNAKLARI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public Right suitableRight;
	public Boolean hasCostEffect = Boolean.TRUE;
	public Boolean hasStockEffect = Boolean.TRUE;

	public Boolean isActive = Boolean.TRUE;

	public static Map<String, String> options(Right suitableRight) {
		return ModelHelper.options(RIGHT, suitableRight);
	}

	public static List<InvoiceTransSource> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static InvoiceTransSource findById(Integer id) {
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
