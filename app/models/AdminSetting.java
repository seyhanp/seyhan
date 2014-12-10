/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
/**
 * @author mdpinar
*/
public class AdminSetting extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(10)
	public String code;

	@Constraints.MaxLength(30)
	public String description;

	@Lob
	public String jsonData;

	@Version
	public Integer version;

	private static Model.Finder<Integer, AdminSetting> find = new Model.Finder<Integer, AdminSetting>(Integer.class, AdminSetting.class);

	public static AdminSetting findByCode(String code) {
		return find.where().eq("code", code).findUnique();
	}

	@Override
	public String toString() {
		return code;
	}

}
