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
public class PayrollSourcesForCheque extends Controller {

	public static Result index() {
		return PayrollSources.index(ChqbllSort.Cheque);
	}

	public static Result options(String rightName) {
		return PayrollSources.options(ChqbllSort.Cheque, rightName);
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		return PayrollSources.list(ChqbllSort.Cheque);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		return PayrollSources.save(ChqbllSort.Cheque);
	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		return PayrollSources.create(ChqbllSort.Cheque);
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		return PayrollSources.edit(id, ChqbllSort.Cheque);
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		return PayrollSources.remove(id, ChqbllSort.Cheque);
	}

}
