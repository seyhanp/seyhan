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
package models.temporal;

import java.util.ArrayList;
import java.util.List;

import models.ChqbllDetailPartial;
import enums.ChqbllSort;

/**
 * @author mdpinar
*/
public class ChqbllPartialList {

	public Integer detailId;

	public ChqbllSort sort; 
	public Boolean isCustomer;
	public String contactName;
	public Integer portfolioNo; 
	public String serialNo;
	public String dueDate;
	public Double amount;
	public String excCode;
	public String cbtype;
	public String owner;
	public String bankName;
	public String surety;
	public String paymentPlace;
	public String description;

	public Double paid = 0d;
	public Double remaining = 0d;

	public List<ChqbllDetailPartial> details = new ArrayList<ChqbllDetailPartial>();

}
