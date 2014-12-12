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

import models.Bank;
import models.Contact;
import models.ContactTransSource;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.Safe;
import play.data.validation.Constraints;
import enums.ChqbllSort;
import enums.ChqbllStep;

/**
 * @author mdpinar
*/
public class ChqbllTransSearchParam extends AbstractSearchParam {

	/*
	 * For Only I/O Payrolls
	 */
	public Contact contact;

	/*
	 * For Only Trans Payrolls
	 */
	public ChqbllStep toStep;
	public Bank bank;
	public Safe safe;

	/*
	 * Shared fields
	 */
	@Constraints.Required
	public ChqbllSort sort = ChqbllSort.Cheque;

	public Integer receiptNo;
	public String transNo;

	public ContactTransSource transSource;
	public GlobalTransPoint transPoint;
	public GlobalPrivateCode privateCode;

}
