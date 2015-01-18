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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.InvoiceTransSource;
import models.SaleSeller;
import models.StockDepot;
import models.WaybillTransSource;
import models.temporal.ReceiptListModel;
import play.data.format.Formats.DateTime;
import controllers.global.Profiles;
import enums.Right;
import enums.TransApprovalType;
import enums.TransStatus;

/**
 * @author mdpinar
*/
public class OrderTransSearchParam extends StockTransSearchParam {

	/*
	 * Arama kismi icin gereken alanlar
	 */
	public Right transType;

	public TransStatus status;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date startDate;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date endDate;

	public Integer rowNumber = 50;

	public Boolean isCash;

	/*
	 * Onaylanan / Onaylanacak satirlar
	 */
	public List<ReceiptListModel> details = new ArrayList<ReceiptListModel>();

	/*
	 * Onaylama sartlari
	 */
	public String formAction;
	public TransApprovalType approvalType = Profiles.chosen().sprs_approvalType;
	public WaybillTransSource waybillTransSource;
	public InvoiceTransSource invoiceTransSource;
	public GlobalTransPoint submitTransPoint = Profiles.chosen().gnel_transPoint;
	public GlobalPrivateCode submitPrivateCode = Profiles.chosen().gnel_privateCode;
	public SaleSeller seller;
	public StockDepot depot = Profiles.chosen().stok_depot;

}
