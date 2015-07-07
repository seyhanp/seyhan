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

import enums.Right;

/**
 * @author mdpinar
*/
public class ReceiptListModel {

	public Integer id;
	public Right right;
	public Boolean isSelected = Boolean.FALSE;
	public Boolean isCompleted;
	public Integer receiptNo;
	public Integer contactId;
	public String contactName;
	public String date;
	public String deliveryDate;
	public String transNo;
	public String amount;
	public String excCode;
	public String description;

	public Integer statusId;

}
