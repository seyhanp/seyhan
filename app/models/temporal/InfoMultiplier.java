/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import play.data.validation.Constraints;

/**
 * @author mdpinar
*/
public class InfoMultiplier {

	@Constraints.Required
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String code;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(100)
	public String name;

	@Constraints.MaxLength(30)
	public String description;

}
