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
import javax.persistence.ManyToOne;

import play.data.validation.Constraints;
import utils.ModelHelper;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;

import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class ContactExtraFields extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.CARI_EKSTRA_ALANLAR;

	@ManyToOne
	public AdminExtraFields extraFields;

	@Constraints.Required
	@Constraints.MinLength(2)
	@Constraints.MaxLength(30)
	public String name;

	public Boolean isActive = Boolean.TRUE;

	public static int getRowCount() {
		return ModelHelper.getRowCount(RIGHT);
	}

	public static Map<String, String> options(Integer extraFieldsId) {
		return ModelHelper.expOptions(RIGHT, Expr.eq("extraFields.id", extraFieldsId));
	}

	public static List<ContactExtraFields> page(AdminExtraFields extraFields) {
		return ModelHelper.page(RIGHT, "name", Expr.eq("extraFields", extraFields));
	}

	public static ContactExtraFields findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id, AdminExtraFields extraFields) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id, Expr.eq("extraFields", extraFields));
	}

	public static void deleteAll(AdminExtraFields extraFields) {
		Ebean.createSqlUpdate("delete from contact_extra_fields where extra_fields_id = " + extraFields.id).execute();
	}

	@Override
	public Right getAuditRight() {
		return RIGHT;
	}

	@Override
	public String getAuditDescription() {
		return this.extraFields.name + " : " + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
