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

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import controllers.global.Profiles;
import enums.Module;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class OrderTrans extends AbstractStockTrans {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	public OrderTransStatus status = Profiles.chosen().sprs_status;

	@ManyToOne
	public OrderTransSource transSource;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<OrderTransDetail> details;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<OrderTransFactor> factors;

	/*
	 * devir fisleri icin true degeri alir
	 */
	public Boolean isTransfer = Boolean.FALSE;

	public Integer waybillId;
	public Integer invoiceId;

	public static Page<OrderTrans> page(OrderTransSearchParam searchParam, Right right) {
		ExpressionList<OrderTrans> expList = ModelHelper.getExpressionList(Module.order);

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
			if (searchParam.orderTransStatus != null && searchParam.orderTransStatus.id != null) {
				expList.eq("status", searchParam.orderTransStatus);
			}
		}

		return ModelHelper.getPage(right, expList, searchParam);
	}

	public static List<ReceiptListModel> findReceiptList(OrderTransSearchParam searchParam) {
		ExpressionList<OrderTrans> expList = ModelHelper.getExpressionList(Module.order);

		expList.eq("workspace", CacheUtils.getWorkspaceId());
		expList.eq("right", searchParam.transType);

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
		if (searchParam.orderTransStatus != null && searchParam.orderTransStatus.id != null) {
			expList.eq("status", searchParam.orderTransStatus);
		}

		List<OrderTrans> modelList = expList
										.order("contact, transDate")
										.findPagingList(searchParam.rowNumber)
										.setFetchAhead(false)
									.getAsList();

		List<ReceiptListModel> result = new ArrayList<ReceiptListModel>();
		for (OrderTrans trans : modelList) {
			ReceiptListModel receipt = new ReceiptListModel();
			receipt.id = trans.id;
			receipt.right = trans.right;
			receipt.receiptNo = trans.receiptNo;
			receipt.contactName = trans.contactName;
			receipt.date = DateUtils.formatDateStandart(trans.transDate);
			receipt.deliveryDate = DateUtils.formatDateStandart(trans.deliveryDate);
			receipt.transNo = trans.transNo;
			receipt.amount = Format.asMoney(trans.netTotal);
			receipt.excCode = trans.excCode;
			receipt.description = trans.description;

			if (trans.contact != null) {
				receipt.contactId = trans.contact.id;
			}
			if (trans.status != null) {
				receipt.statusId = trans.status.id;
			}

			result.add(receipt);
		}

		return result;
	}

	public static OrderTrans findById(Integer id) {
		return ModelHelper.findById(Module.order, id, "details", "details.stock", "factors", "factors.factor");
	}

	/**
	 * Devir fisleri icin
	 * 
	 * @param id
	 * @return
	 */
	public static List<OrderTrans> findTransferList(int sourceWS) {
		ExpressionList<OrderTrans> expList = ModelHelper.getExpressionList(Module.order);

		expList.eq("workspace", sourceWS);
		expList.eq("isCompleted", Boolean.FALSE);
		return expList
					.order("receiptNo")
					.fetch("details")
					.fetch("details.stock")
				.findList();
	}

	@Override
	@Transient
	public String getTableName() {
		return "order_trans";
	}

}
