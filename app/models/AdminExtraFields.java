/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;

import com.avaje.ebean.ExpressionList;

import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class AdminExtraFields extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	public Integer idno;
	
	@Constraints.Required
	@Constraints.MinLength(2)
	@Constraints.MaxLength(15)
	public String distinction;
	
	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(12)
	public String name;

	public Boolean isRequired = Boolean.FALSE;
	public Boolean isActive = Boolean.TRUE;

	private static Model.Finder<Integer, AdminExtraFields> find = new Model.Finder<Integer, AdminExtraFields>(Integer.class, AdminExtraFields.class);

	public static List<AdminExtraFields> page(String distinction) {
		return find.where()
					.eq("distinction", distinction)
					.orderBy("idno")
				.findList();
	}
	
	public static List<AdminExtraFields> listAll(String distinction) {
		return find.where()
					.eq("distinction", distinction)
					.eq("isActive", Boolean.TRUE)
					.orderBy("idno")
				.findList();
	}

	public static AdminExtraFields findById(String distinction, Integer id) {
		return find.where().eq("distinction", distinction).eq("idno", id).findUnique();
	}

	public static int getRowCount(String distinction) {
		int result = find.where().eq("distinction", distinction).findRowCount();
		if (result > 0) 
			return result - 1;
		else
			return result;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id, String distinction) {
		ExpressionList<AdminExtraFields> el = find.where().eq("distinction", distinction).eq(field, value);
		if (id != null)
			el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(AdminExtraFields.class, Right.EKSTRA_CARI_ALANLARI);
		CacheUtils.cleanAll(AdminExtraFields.class, Right.EKSTRA_STOK_ALANLARI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(AdminExtraFields.class, Right.EKSTRA_CARI_ALANLARI);
		CacheUtils.cleanAll(AdminExtraFields.class, Right.EKSTRA_STOK_ALANLARI);
		super.update();
	}
	
	@Override
	public void delete() {
		CacheUtils.cleanAll(AdminExtraFields.class, Right.EKSTRA_CARI_ALANLARI);
		CacheUtils.cleanAll(AdminExtraFields.class, Right.EKSTRA_STOK_ALANLARI);
		ContactExtraFields.deleteAll(this);
		super.delete();
	}

}
