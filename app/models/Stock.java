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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.search.StockSearchParam;
import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.ModelHelper;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import controllers.global.Profiles;
import enums.Right;
import enums.TransType;

@Entity
/**
 * @author mdpinar
*/
public class Stock extends BaseStockExtraFieldsModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.STOK_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(2)
	@Constraints.MaxLength(30)
	public String code;

	@Constraints.Required
	@Constraints.MinLength(2)
	@Constraints.MaxLength(100)
	public String name;

	@Constraints.MaxLength(30)
	public String providerCode;

	public Boolean isActive = Boolean.TRUE;

	@Constraints.MaxLength(3)
	public String excCode;

	public Double buyTax = 0d;
	public Double sellTax = 0d;

	public Double taxRate2 = 0d;
	public Double taxRate3 = 0d;

	public Double buyPrice = 0d;
	public Double sellPrice = 0d;

	@Constraints.Required
	@Constraints.MaxLength(6)
	public String unit1;

	@Constraints.MaxLength(6)
	public String unit2;

	@Constraints.MaxLength(6)
	public String unit3;

	public Double unit2Ratio = 0d;
	public Double unit3Ratio = 0d;

	public Double maxLimit = 0d;
	public Double minLimit = 0d;

	public Double primRate = 0d;

	@ManyToOne
	public StockCategory category;

	@Lob
	public String note;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="stock", orphanRemoval = true)
	public List<StockBarcode> barcodes;

	@Transient
	public String barcode;

	public Stock() {
		super();
		this.excCode = Profiles.chosen().gnel_excCode;
		this.buyTax = Profiles.chosen().stok_taxRate;
		this.sellTax = Profiles.chosen().stok_taxRate;
		this.unit1 = Profiles.chosen().stok_unit.name;
	}

	public static Page<Stock> page(StockSearchParam searchParam) {
		ExpressionList<Stock> expList = ModelHelper.getExpressionList(RIGHT);

		if (searchParam.fullText != null && ! searchParam.fullText.isEmpty()) {
			expList.or(
				Expr.like("name", "%" + searchParam.fullText + "%"),
				Expr.like("code", "%" + searchParam.fullText + "%")
			);
		} else {
			if (searchParam.category != null && searchParam.category.id != null) {
				expList.eq("category_id", searchParam.category.id); 
			}
			if (searchParam.code != null && ! searchParam.code.isEmpty()) {
				expList.like("code", searchParam.code + "%");
			}
			if (searchParam.name != null && ! searchParam.name.isEmpty()) {
				expList.like("name", searchParam.name + "%");
			}
			if (searchParam.providerCode != null && ! searchParam.providerCode.isEmpty()) {
				expList.eq("providerCode", searchParam.providerCode);
			}
			if (searchParam.extraField0 != null && searchParam.extraField0.id != null) expList.eq("extraField0", searchParam.extraField0);
			if (searchParam.extraField1 != null && searchParam.extraField1.id != null) expList.eq("extraField1", searchParam.extraField1);
			if (searchParam.extraField2 != null && searchParam.extraField2.id != null) expList.eq("extraField2", searchParam.extraField2);
			if (searchParam.extraField3 != null && searchParam.extraField3.id != null) expList.eq("extraField3", searchParam.extraField3);
			if (searchParam.extraField4 != null && searchParam.extraField4.id != null) expList.eq("extraField4", searchParam.extraField4);
			if (searchParam.extraField5 != null && searchParam.extraField5.id != null) expList.eq("extraField5", searchParam.extraField5);
			if (searchParam.extraField6 != null && searchParam.extraField6.id != null) expList.eq("extraField6", searchParam.extraField6);
			if (searchParam.extraField7 != null && searchParam.extraField7.id != null) expList.eq("extraField7", searchParam.extraField7);
			if (searchParam.extraField8 != null && searchParam.extraField8.id != null) expList.eq("extraField8", searchParam.extraField8);
			if (searchParam.extraField9 != null && searchParam.extraField9.id != null) expList.eq("extraField9", searchParam.extraField9);
		}

		return ModelHelper.getPage(RIGHT, expList, searchParam, false);
	}

	public static Stock findByCode(String code) {
		return ModelHelper.findByCode(RIGHT, code);
	}

	public static Stock findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static Stock findByIdWithBarcodes(Integer id) {
		return ModelHelper.findById(RIGHT, id, "barcodes");
	}

	public static double findBasePrice(Contact contact, Stock stock, TransType transType) {
		if (stock == null) return 0;

		if (TransType.Input.equals(transType) || TransType.Debt.equals(transType)) {
			return stock.buyPrice;
		} else {
			return stock.sellPrice;
		}
	}

	public static double findPriceByDetail(AbstractStockTransDetail detail) {
		if (detail == null || detail.basePrice == null || detail.unit == null  || detail.basePrice <= 0|| detail.unit.trim().isEmpty()) return 0;

		if (detail.unit.equals(detail.unit2) && detail.unit2Ratio != null)  return detail.basePrice * detail.unit2Ratio.doubleValue();
		if (detail.unit.equals(detail.unit3) && detail.unit3Ratio != null)  return detail.basePrice * detail.unit3Ratio.doubleValue();

		return detail.basePrice;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id);
	}

	@Override
	public Right getAuditRight() {
		return RIGHT;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.code") + this.code + " - " +
				Messages.get("audit.name") + this.name + " - " +
				Messages.get("buy_price") + this.buyPrice + " - " +
				Messages.get("sell_price") + this.sellPrice;
	}

	@Override
	public String toString() {
		return name;
	}

}
