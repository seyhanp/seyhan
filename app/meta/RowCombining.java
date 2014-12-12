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
package meta;

/**
 * For Stock Row Combining purposal
 * 
 * @author mdpinar
 */
/**
 * @author mdpinar
*/
public class RowCombining {
	
	public Integer row;
	public Integer stockId;
	public Double price;

	public RowCombining(Integer row, Integer stockId, Double price) {
		super();
		this.row = row;
		this.stockId = stockId;
		this.price = price;
	}

}
