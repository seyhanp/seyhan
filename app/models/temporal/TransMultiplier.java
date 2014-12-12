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

import javax.persistence.ManyToOne;

import models.Bank;
import models.Contact;
import models.Safe;
import models.SaleSeller;
import models.StockDepot;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import enums.Right;

/**
 * @author mdpinar
*/
public class TransMultiplier {

	@Constraints.Required
	public Integer id;

	public Contact contact;
	public Safe safe;
	public Bank bank;

	public Right right;

	@Constraints.Required
	@DateTime(pattern = "dd/MM/yyyy")
	public Date transDate = new Date();

	@Constraints.MaxLength(20)
	public String transNo;

	@Constraints.MaxLength(100)
	public String description;

	/**
	 * For Stock Transactions
	 */
	@ManyToOne
	public StockDepot depot;

	@ManyToOne
	public StockDepot refDepot; //transfer depot

	@ManyToOne
	public SaleSeller seller;

}
