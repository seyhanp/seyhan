/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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
