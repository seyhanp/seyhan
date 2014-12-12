/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
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
