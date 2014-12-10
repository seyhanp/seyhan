/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.chqbll;

import meta.RightBind;
import play.mvc.Controller;
import play.mvc.Result;
import enums.ChqbllSort;

/**
 * @author mdpinar
*/
public class PartialsForBill extends Controller {

	public static Result list(RightBind rightBind) {
		return Partials.list(ChqbllSort.Bill, rightBind);
	}

	public static Result save(RightBind rightBind) {
		return Partials.save(ChqbllSort.Bill, rightBind);
	}

	public static Result edit(Integer id, RightBind rightBind) {
		return Partials.edit(id, ChqbllSort.Bill, rightBind);
	}

	public static Result remove(Integer id, RightBind rightBind) {
		return Partials.remove(id, ChqbllSort.Bill, rightBind);
	}

}
