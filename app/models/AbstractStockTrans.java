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
package models;

import java.util.Date;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import play.data.format.Formats.DateTime;
import controllers.global.Profiles;

@MappedSuperclass
/**
 * @author mdpinar
*/
public abstract class AbstractStockTrans extends AbstractBaseTrans {

	private static final long serialVersionUID = 1L;

	public Date realDate = new Date();

	@DateTime(pattern = "dd/MM/yyyy")
	public Date deliveryDate;

	@ManyToOne
	public Contact contact;

	public Boolean isTaxInclude = Profiles.chosen().stok_isTaxInclude;

	@ManyToOne
	public StockDepot depot = Profiles.chosen().stok_depot;

	public String contactName;
	public String contactTaxOffice;
	public String contactTaxNumber;
	public String contactAddress1;
	public String contactAddress2;
	public String consigner;
	public String recepient;
	public Integer roundingDigits = Profiles.chosen().stok_roundingDigits;
	public Double totalDiscountRate = 0d;

	public Double total = 0d;
	public Double roundingDiscount = 0d;
	public Double discountTotal = 0d;
	public Double subtotal = 0d;
	public Double plusFactorTotal = 0d;
	public Double minusFactorTotal = 0d;
	public Double taxTotal = 0d;
	public Double netTotal = 0d;

	@ManyToOne
	public SaleSeller seller;

}
