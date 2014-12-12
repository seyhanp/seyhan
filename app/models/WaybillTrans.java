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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.search.OrderTransSearchParam;
import models.temporal.ReceiptListModel;
import utils.CacheUtils;
import utils.DateUtils;
import utils.Format;
import utils.ModelHelper;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.Module;
import enums.Right;
import enums.TransStatus;

@Entity
/**
 * @author mdpinar
*/
public class WaybillTrans extends AbstractStockTrans {

	private static final long serialVersionUID = 1L;

	public TransStatus status = TransStatus.Waiting;

	@ManyToOne
	public WaybillTransSource transSource;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<WaybillTransDetail> details;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<WaybillTransFactor> factors;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<WaybillTransRelation> relations;

	public Integer invoiceId;

	/*
	 * devir fisleri icin true degeri alir
	 */
	public Boolean isTransfer = Boolean.FALSE;

	public static Page<WaybillTrans> page(OrderTransSearchParam searchParam, Right right) {
		ExpressionList<WaybillTrans> expList = ModelHelper.getExpressionList(right.module);

		expList.eq("right", right);

		if (searchParam.fullText != null && ! searchParam.fullText.isEmpty()) {
			expList.or(
				Expr.like("contact.name", "%" + searchParam.fullText + "%"),
				Expr.or(
						Expr.like("description", "%" + searchParam.fullText + "%"),
						Expr.like("transNo", "%" + searchParam.fullText + "%")
					)
			);
		} else {
			if (searchParam.status != null) {
				expList.eq("status", searchParam.status);
			}
			if (searchParam.receiptNo != null && searchParam.receiptNo.intValue() > 0) {
				expList.eq("receiptNo", searchParam.receiptNo);
			}
			if (searchParam.transNo != null && ! searchParam.transNo.isEmpty()) {
				expList.eq("transNo", searchParam.transNo);
			}
			if (searchParam.startDate != null) {
				expList.ge("transDate", searchParam.startDate);
			}
			if (searchParam.endDate != null) {
				expList.le("transDate", searchParam.endDate);
			}
			if (searchParam.deliveryDate != null) {
				expList.eq("deliveryDate", searchParam.deliveryDate);
			}
			if (searchParam.refContact != null && searchParam.refContact.id != null) {
				expList.eq("contact", searchParam.refContact);
			}
			if (searchParam.seller != null && searchParam.seller.id != null) {
				expList.eq("seller", searchParam.seller);
			}
			if (searchParam.transPoint != null && searchParam.transPoint.id != null) {
				expList.eq("transPoint", searchParam.transPoint);
			}
			if (searchParam.privateCode != null && searchParam.privateCode.id != null) {
				expList.eq("privateCode", searchParam.privateCode);
			}
			if (searchParam.transSource != null && searchParam.transSource.id != null) {
				expList.eq("transSource", searchParam.transSource);
			}
		}

		return ModelHelper.getPage(right, expList, searchParam);
	}

	public static List<ReceiptListModel> findReceiptList(OrderTransSearchParam searchParam) {
		ExpressionList<WaybillTrans> expList = ModelHelper.getExpressionList(Module.waybill);

		expList.eq("workspace", CacheUtils.getWorkspaceId());
		expList.eq("right", searchParam.transType);

		if (searchParam.status != null) {
			expList.eq("status", searchParam.status);
		}
		if (searchParam.receiptNo != null && searchParam.receiptNo.intValue() > 0) {
			expList.eq("receiptNo", searchParam.receiptNo);
		}
		if (searchParam.transNo != null && ! searchParam.transNo.isEmpty()) {
			expList.eq("transNo", searchParam.transNo);
		}
		if (searchParam.startDate != null) {
			expList.ge("transDate", searchParam.startDate);
		}
		if (searchParam.endDate != null) {
			expList.le("transDate", searchParam.endDate);
		}
		if (searchParam.refContact != null && searchParam.refContact.id != null) {
			expList.eq("contact", searchParam.refContact);
		}
		if (searchParam.seller != null && searchParam.seller.id != null) {
			expList.eq("seller", searchParam.seller);
		}
		if (searchParam.transPoint != null && searchParam.transPoint.id != null) {
			expList.eq("transPoint", searchParam.transPoint);
		}
		if (searchParam.privateCode != null && searchParam.privateCode.id != null) {
			expList.eq("privateCode", searchParam.privateCode);
		}
		if (searchParam.transSource != null && searchParam.transSource.id != null) {
			expList.eq("transSource", searchParam.transSource);
		}

		List<WaybillTrans> modelList = expList
										.order("contact, transDate")
										.findPagingList(searchParam.rowNumber)
										.setFetchAhead(false)
									.getAsList();

		List<ReceiptListModel> result = new ArrayList<ReceiptListModel>();
		for (WaybillTrans trans : modelList) {
			ReceiptListModel receipt = new ReceiptListModel();
			receipt.id = trans.id;
			receipt.right = trans.right;
			receipt.status = trans.status;
			receipt.receiptNo = trans.receiptNo;
			receipt.contactName = trans.contactName;
			receipt.date = DateUtils.formatDateStandart(trans.transDate);
			receipt.transNo = trans.transNo;
			receipt.amount = Format.asMoney(trans.netTotal);
			receipt.excCode = trans.excCode;
			receipt.description = trans.description;

			if (trans.contact != null) {
				receipt.contactId = trans.contact.id;
			}

			result.add(receipt);
		}

		return result;
	}

	public static WaybillTrans findById(Integer id) {
		return ModelHelper.findById(Module.waybill, id, "details", "details.stock", "factors", "factors.factor", "relations");
	}

	@Override
	public void delete() {
		if (! isTransfer) deleteStockTrans(true);
		super.delete();
	}

	@Override
	public void save() {
		super.save();
		if (! isTransfer) addStockTrans();
	}

	@Override
	public void update() {
		super.update();
		if (! isTransfer) {
			deleteStockTrans(false);
			addStockTrans();
		}
	}

	/**
	 * Devir fisleri icin
	 * 
	 * @param id
	 * @return
	 */
	public static List<WaybillTrans> findTransferList(int sourceWS) {
		ExpressionList<WaybillTrans> expList = ModelHelper.getExpressionList(Module.waybill);

		expList.eq("workspace", sourceWS);
		expList.eq("status", TransStatus.Waiting);
		return expList
					.order("receiptNo")
					.fetch("details")
					.fetch("details.stock")
				.findList();
	}

	private void deleteStockTrans(boolean willBeRelationsDeleted) {
		StockTrans refTrans = StockTrans.findByRefIdAndModule(this.id, this.right);
		if (refTrans != null) {
			refTrans.delete();

			if (willBeRelationsDeleted && relations != null && relations.size() > 0) {
				List<Integer> idList = new ArrayList<Integer>();
				for (WaybillTransRelation rel : relations) {
					idList.add(rel.relId);
				}
				Ebean.createSqlUpdate("update order_trans_detail set status = 'Waiting', completed = 0, cancelled = 0 where trans_id in (:idList) and workspace = :workspace;")
						.setParameter("workspace", CacheUtils.getWorkspaceId())
						.setParameter("idList", idList)
					.execute();
				Ebean.createSqlUpdate("update order_trans set status = 'Waiting', waybill_id = null, invoice_id = null where id in (:idList) and workspace = :workspace;")
						.setParameter("workspace", CacheUtils.getWorkspaceId())
						.setParameter("idList", idList)
					.execute();
			}
		}
	}

	private void addStockTrans() {
		StockTrans refTrans = new StockTrans();

		refTrans.refId = this.id;
		refTrans.refModule = Module.waybill;

		refTrans.workspace = this.workspace;
		refTrans.right = this.right;
		refTrans.receiptNo = this.receiptNo;
		refTrans.transNo = this.transNo;
		refTrans.transType = this.right.transType;
		refTrans.excCode = this.excCode; 
		refTrans.excRate = this.excRate; 
		refTrans.excEquivalent = this.excEquivalent;
		refTrans.transPoint = this.transPoint;
		refTrans.privateCode = this.privateCode;
		refTrans.description = this.description;
		refTrans.transYear = DateUtils.getYear(this.transDate);
		refTrans.transMonth = DateUtils.getYearMonth(this.transDate);
		refTrans.realDate = this.realDate;
		refTrans.deliveryDate = this.deliveryDate;
		refTrans.contact = this.contact;
		refTrans.isTaxInclude = this.isTaxInclude;
		refTrans.depot = this.depot;
		refTrans.contactName = this.contactName;
		refTrans.contactTaxOffice = this.contactTaxOffice;
		refTrans.contactTaxNumber = this.contactTaxNumber;
		refTrans.contactAddress1 = this.contactAddress1;
		refTrans.contactAddress2 = this.contactAddress2;
		refTrans.consigner = this.consigner;
		refTrans.recepient = this.recepient;
		refTrans.roundingDigits = this.roundingDigits;
		refTrans.totalDiscountRate = this.totalDiscountRate;
		refTrans.total = this.total;
		refTrans.roundingDiscount = this.roundingDiscount;
		refTrans.discountTotal = this.discountTotal;
		refTrans.subtotal = this.subtotal;
		refTrans.plusFactorTotal = this.plusFactorTotal;
		refTrans.minusFactorTotal = this.minusFactorTotal;
		refTrans.taxTotal = this.taxTotal;
		refTrans.netTotal = this.netTotal;
		refTrans.seller = this.seller;

		List<StockTransDetail> refDetails = new ArrayList<StockTransDetail>();
		for (WaybillTransDetail detail : details) {
			StockTransDetail stockDet = new StockTransDetail();
			stockDet.workspace = this.workspace;
			stockDet.receiptNo = this.receiptNo;
			stockDet.right = this.right;
			stockDet.stock = detail.stock;
			stockDet.transDate = detail.transDate;
			stockDet.deliveryDate = detail.deliveryDate;
			stockDet.transType = detail.transType;
			stockDet.depot = detail.depot;
			stockDet.contact = detail.contact;
			stockDet.seller = detail.seller;
			stockDet.transPoint = detail.transPoint;
			stockDet.privateCode = detail.privateCode;
			stockDet.name = detail.name;
			stockDet.quantity = detail.quantity;
			stockDet.unit = detail.unit;
			stockDet.unitRatio = detail.unitRatio;
			stockDet.basePrice = detail.basePrice;
			stockDet.price = detail.price;
			stockDet.taxRate = detail.taxRate;
			stockDet.discountRate1 = detail.discountRate1;
			stockDet.discountRate2 = detail.discountRate2;
			stockDet.discountRate3 = detail.discountRate3;
			stockDet.amount = detail.amount;
			stockDet.taxAmount = detail.taxAmount;
			stockDet.discountAmount = detail.discountAmount;
			stockDet.total = detail.total;
			stockDet.description = detail.description;
			stockDet.transYear = detail.transYear;
			stockDet.transMonth = detail.transMonth;
			stockDet.unit1 = detail.unit1;
			stockDet.unit2 = detail.unit2;
			stockDet.unit3 = detail.unit3;
			stockDet.unit2Ratio = detail.unit2Ratio;
			stockDet.unit3Ratio = detail.unit3Ratio;
			stockDet.input = detail.input;
			stockDet.output = detail.output;
			stockDet.inTotal = detail.inTotal;
			stockDet.outTotal = detail.outTotal;
			stockDet.netInput = detail.input;
			stockDet.netOutput = detail.output;
			stockDet.netInTotal = detail.inTotal;
			stockDet.netOutTotal = detail.outTotal;
			stockDet.excCode = detail.excCode;
			stockDet.excRate = detail.excRate;
			stockDet.excEquivalent = detail.excEquivalent;
			stockDet.plusFactorAmount = detail.plusFactorAmount;
			stockDet.minusFactorAmount = detail.minusFactorAmount;

			refDetails.add(stockDet);
		}
		refTrans.details = refDetails;
		refTrans.save();
	}

	@Override
	@Transient
	public String getTableName() {
		return "waybill_trans";
	}

}
