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

import javax.persistence.ManyToMany;

import models.AdminWorkspace;
import models.StockCosting;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;

/**
 * @author mdpinar
*/
public class Ws2WsTransferModel {

	@ManyToMany
	@Constraints.Required
	public AdminWorkspace sourceWS;

	@ManyToMany
	@Constraints.Required
	public AdminWorkspace targetWS;
	
	@DateTime(pattern = "dd/MM/yyyy")
	public Date transDate = new Date();

	public String description;

	@ManyToMany
	public StockCosting costing;

	public boolean stockInfo;
	public boolean stockTrans;

	public boolean contactInfo;
	public boolean contactTrans;

	public boolean safeInfo;
	public boolean safeTrans;

	public boolean bankInfo;
	public boolean bankTrans;

	public boolean orderInfo;
	public boolean orderTrans;

	public boolean waybillInfo;
	public boolean waybillTrans;
	
	public boolean invoiceInfo;

	public boolean chqbllInfo;
	public boolean chqbllTrans;

	public boolean saleInfo;
	public boolean globalInfo;

	public Ws2WsTransferModel() {
		this.stockInfo = true;
		this.stockTrans = true;
		this.contactInfo = true;
		this.contactTrans = true;
		this.safeInfo = true;
		this.safeTrans = true;
		this.bankInfo = true;
		this.bankTrans = true;
		this.orderInfo = true;
		this.waybillInfo = true;
		this.invoiceInfo = true;
		this.chqbllInfo = true;
		this.chqbllTrans = true;
		this.saleInfo = true;
		this.globalInfo = true;
	}

}
