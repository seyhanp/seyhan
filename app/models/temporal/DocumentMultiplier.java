/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import enums.Module;
import enums.Right;
import play.data.validation.Constraints;

/**
 * @author mdpinar
*/
public class DocumentMultiplier {

	@Constraints.Required
	public Integer id;

	@Constraints.Required
	public Module module;

	public String header;

	@Constraints.Required
	public Right right;

	@Constraints.Required
	@Constraints.MaxLength(30)
	public String name;

	@Constraints.MaxLength(30)
	public String description;

}
