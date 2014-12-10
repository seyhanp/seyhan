/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.chqbll;

import play.mvc.Controller;
import play.mvc.Result;
import enums.ChqbllSort;

/**
 * @author mdpinar
*/
public class PayrollSourcesForBill extends Controller {

	public static Result index() {
		return PayrollSources.index(ChqbllSort.Bill);
	}

	public static Result options(String rightName) {
		return PayrollSources.options(ChqbllSort.Bill, rightName);
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		return PayrollSources.list(ChqbllSort.Bill);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		return PayrollSources.save(ChqbllSort.Bill);
	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		return PayrollSources.create(ChqbllSort.Bill);
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		return PayrollSources.edit(id, ChqbllSort.Bill);
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		return PayrollSources.remove(id, ChqbllSort.Bill);
	}

}
