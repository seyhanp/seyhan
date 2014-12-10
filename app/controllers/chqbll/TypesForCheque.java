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
public class TypesForCheque extends Controller {

	public static Result index() {
		return Types.index(ChqbllSort.Cheque);
	}

	public static Result options() {
		return Types.options(ChqbllSort.Cheque);
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		return Types.list(ChqbllSort.Cheque);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		return Types.save(ChqbllSort.Cheque);
	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		return Types.create(ChqbllSort.Cheque);
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		return Types.edit(id, ChqbllSort.Cheque);
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		return Types.remove(id, ChqbllSort.Cheque);
	}

}
