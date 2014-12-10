/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;
import enums.Right;
import enums.RightLevel;

@Entity
/**
 * @author mdpinar
*/
public class AdminUserRight extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Boolean isCRUD;

	@Transient
	public Boolean isHeader;

	@Transient
	public Boolean isReport;

	@Transient
	public String title;

	@Transient
	public String key;

	@Transient
	public Right right;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(50)
	public String name;

	public RightLevel rightLevel = RightLevel.Disable;

	@ManyToOne
	public AdminUserRole userRole;

	public AdminUserRight(String right, String level) {
		this(Right.valueOf(right));
		this.rightLevel = RightLevel.valueOf(level);
	}

	public AdminUserRight(Right right) {
		this.right = right;
		this.name = right.name();
		this.key = right.key;
		this.isCRUD = right.isCRUD;
		this.isHeader = right.isHeader;
		this.isReport = right.isReport;
		this.rightLevel = RightLevel.Disable;
		this.title = Messages.get(right.key);
	}

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;

		if (!(other instanceof AdminUserRight)) return false;

		AdminUserRight otherObj = (AdminUserRight) other;

		return (otherObj.name != null && otherObj.name.equals(this.name));
	}

}
