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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.search.StockTransSearchParam;
import utils.CacheUtils;
import utils.CloneUtils;
import utils.ModelHelper;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.Module;
import enums.Right;
import enums.TransType;

@Entity
/**
 * @author mdpinar
*/
public class StockTrans extends AbstractStockTrans {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	public StockTransSource transSource;

	@ManyToOne
	public StockDepot refDepot; //transfer depot

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<StockTransDetail> details;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<StockTransFactor> factors;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<StockTransTax> taxes;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<StockTransCurrency> currencies;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<WaybillTransRelation> relations;

	public static Page<StockTrans> page(StockTransSearchParam searchParam, Right right) {
		ExpressionList<StockTrans> expList = ModelHelper.getExpressionList(right.module);

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
		}

		if (! Right.STOK_TRANSFER_FISI.equals(right)) {
			return ModelHelper.getPage(right, expList, searchParam);
		} else {
			return ModelHelper.getPage(right, expList, searchParam, "depot", "refDepot");
		}
	}

	public static StockTrans findById(Integer id) {
		return ModelHelper.findById(Module.stock, id, "details", "details.stock", "details.seller", "factors", "factors.factor", "relations");
	}

	public static StockTrans findByRefIdAndModule(Integer id, Right searchRight) {
		return ModelHelper.findByRefIdAndRight(Module.stock, id, searchRight);
	}

	@Override
	public void save() {
		/**
		 * Transfer fislerinde yansiyan depo icin kayitlar olusturulur
		 */
		if (right.equals(Right.STOK_TRANSFER_FISI)) {

			/**
			 * Daha onceden kaydedilmisse eski yansi silinir 
			 */
			StockTrans oldTrans = null;
			if (refId != null) {
				oldTrans = findById(refId);
				if (oldTrans != null) {
					oldTrans.delete();
				}
			}

			StockTrans transfer = CloneUtils.cloneTransaction(this);

			transfer.refId = id;
			transfer.refModule = right.module;

			transfer.workspace = CacheUtils.getWorkspaceId();
			transfer.right = Right.STOK_TRANSFER_YANSI;
			transfer.transType = TransType.Input;
			transfer.contact = null;
			transfer.depot = this.refDepot;
			transfer.refDepot = this.depot;

			for (StockTransDetail std : transfer.details) {
				std.id = null;
				std.workspace = transfer.workspace;
				std.depot = this.refDepot;
				std.trans = transfer;
				std.right = transfer.right;
				std.contact = transfer.contact;
				std.transType = transfer.transType;
				std.input = std.output;
				std.inTotal = std.outTotal;
				std.netInput = std.netOutput;
				std.netInTotal = std.netOutTotal;
				std.output = 0d;
				std.outTotal = 0d;
				std.netOutput = 0d;
				std.netOutTotal = 0d;

				CloneUtils.resetModel(std);
			}
			for (StockTransFactor stf : transfer.factors) {
				stf.id = null;
				stf.trans = transfer;

				CloneUtils.resetModel(stf);
			}

			transfer.save();

			this.refModule = Module.stock;
			this.refId = transfer.id;
		}

		super.save();
	}

	@Override
	public void delete() {
		if (right.equals(Right.STOK_TRANSFER_FISI) && refId != null) {
			Ebean.createSqlUpdate("delete from stock_trans_tax where trans_id = :trans_id").setParameter("trans_id", refId).execute();
			Ebean.createSqlUpdate("delete from stock_trans_factor where trans_id = :trans_id").setParameter("trans_id", refId).execute();
			Ebean.createSqlUpdate("delete from stock_trans_currency where trans_id = :trans_id").setParameter("trans_id", refId).execute();
			Ebean.createSqlUpdate("delete from stock_trans_detail where trans_id = :trans_id").setParameter("trans_id", refId).execute();
			Ebean.createSqlUpdate("delete from stock_trans where id = :trans_id").setParameter("trans_id", refId).execute();
		}

		super.delete();
	}

	@Override
	@Transient
	public String getTableName() {
		return "stock_trans";
	}

}
