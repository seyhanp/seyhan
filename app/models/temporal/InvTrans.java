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

import java.util.Date;

import enums.Right;

/**
 * @author mdpinar
*/
public class InvTrans {

	public Integer id;
	public Right right;

	public Double debt;
	public Double credit;

	public String link;

	/**
	 * For stock
	 */
	public String title;
	public Date date;
	public Double quantity;
	public Double price;
	public String excCode;

	public String depot;
	public String transType;

}
