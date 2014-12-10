/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import javax.persistence.Transient;

import play.data.validation.Constraints;

/**
 * @author mdpinar
*/
public class UserData {

	@Constraints.MaxLength(30)
	public String title;

	@Constraints.Email
	@Constraints.MaxLength(100)
	public String email;

	@Constraints.Required
	@Constraints.MinLength(4)
	@Constraints.MaxLength(30)
	public String password;

	@Transient
	@Constraints.Required
	@Constraints.MinLength(4)
	@Constraints.MaxLength(30)
	public String oldPassword;

	@Transient
	@Constraints.Required
	@Constraints.MinLength(4)
	@Constraints.MaxLength(30)
	public String repeatPassword;

	@Transient
	public String passwordHash;

}
