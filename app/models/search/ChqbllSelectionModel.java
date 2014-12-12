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

import java.util.Date;

import models.Contact;
import play.data.format.Formats.DateTime;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Right;

/**
 * @author mdpinar
*/
public class ChqbllSelectionModel {

	public Right selRight;
	public ChqbllSort selSort;
	public ChqbllStep step;
	public Contact refContact;
	public Integer selPortfolioNo;
	public String selSerialNo;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date startDate;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date endDate;

	public String alreadySelected;

	public ChqbllSelectionModel() {
		;
	}

	public ChqbllSelectionModel(ChqbllSort sort, Right right, ChqbllStep step, String alreadySelected) {
		super();
		this.selRight = right;
		this.selSort = sort;
		this.step = step;
		this.alreadySelected = alreadySelected;
	}

}
