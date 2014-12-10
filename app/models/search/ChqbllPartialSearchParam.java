/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.ManyToOne;

import models.ChqbllType;
import play.data.validation.Constraints;
import play.i18n.Messages;
import enums.ChqbllSort;
import enums.ChqbllStep;

/**
 * @author mdpinar
*/
public class ChqbllPartialSearchParam extends AbstractSearchParam {

	public ChqbllStep step;

	@ManyToOne
	public ChqbllType cbtype;

	@Constraints.Required
	public ChqbllSort sort = ChqbllSort.Cheque;

	public Integer portfolioNo;
	public String serialNo;

	public Boolean balanceOpts;

	public static Map<String, String> balanceOptions() {
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		options.put("true", Messages.get("balance.open"));
		options.put("false", Messages.get("balance.completed"));

		return options;
	}

}
