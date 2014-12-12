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

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

@Entity
@Sql
/**
 * @author mdpinar
*/
public class StockModel {

	public Integer id;
	public String code;
	public String name;
	public Integer number = 1;
	public String excCode;
	public Double buyTax;
	public Double sellTax;
	public Double taxRate2;
	public Double taxRate3;
	public Double buyPrice;
	public Double sellPrice;
	public Double discountRate1;
	public Double discountRate2;
	public Double discountRate3;
	public String unit1;
	public String unit2;
	public String unit3;
	public Double unit2ratio;
	public Double unit3ratio;

	public String prefix;
	public String suffix;
	public String unitNo;

}
