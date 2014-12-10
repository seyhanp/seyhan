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
public class BankTransSource extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.BANK_ISLEM_KAYNAKLARI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public Right suitableRight;

	public Boolean isActive = Boolean.TRUE;

	public static Map<String, String> options(Right suitableRight) {
		return ModelHelper.options(RIGHT, suitableRight);
	}

	public static List<BankTransSource> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static BankTransSource findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id);
	}

	public static Map<String, String> crossOptions(String direction) {
		if (direction != null) {
			TransType tt = null;
			try {
				tt = TransType.valueOf(direction);
			} catch (Exception e) {
				;
			}
			if (tt != null) {
				if (tt.equals(TransType.Debt)) return options(Right.BANK_HESABA_PARA_GIRISI);
				if (tt.equals(TransType.Credit)) return options(Right.BANK_HESAPTAN_PARA_CIKISI);
			}
		}
		return options(null);
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
