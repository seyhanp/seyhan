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

import meta.RightBind;
import play.mvc.Controller;
import play.mvc.Result;
import enums.ChqbllSort;

/**
 * @author mdpinar
*/
public class PayrollsForCheque extends Controller {

	public static Result list(RightBind rightBind) {
		return Payrolls.list(ChqbllSort.Cheque, rightBind);
	}

	public static Result save(RightBind rightBind) {
		return Payrolls.save(ChqbllSort.Cheque, rightBind);
	}

	public static Result create(RightBind rightBind) {
		return Payrolls.create(ChqbllSort.Cheque, rightBind);
	}

	public static Result edit(Integer id, RightBind rightBind) {
		return Payrolls.edit(id, ChqbllSort.Cheque, rightBind);
	}

	public static Result remove(Integer id, RightBind rightBind) {
		return Payrolls.remove(id, ChqbllSort.Cheque, rightBind);
	}

}
