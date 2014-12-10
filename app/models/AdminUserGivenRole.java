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

import play.db.ebean.Model;

@Entity
/**
 * @author mdpinar
*/
public class AdminUserGivenRole extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@ManyToOne
	public AdminUserGroup userGroup;

	@ManyToOne
	public AdminWorkspace workspace;

	@ManyToOne
	public AdminUserRole userRole;

	public AdminUserGivenRole(AdminWorkspace workspace) {
		this.workspace = workspace;
	}

}
