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
