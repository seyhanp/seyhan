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

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.search.ContactSearchParam;
import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.ModelHelper;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.ContactStatus;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class Contact extends BaseContactExtraFieldsModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.CARI_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String code;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(100)
	public String name;

	@Constraints.MaxLength(20)
	public String taxOffice;

	@Constraints.MaxLength(15)
	public String taxNumber;

	public Integer tcKimlik;

	@Constraints.MaxLength(30)
	public String relevant;

	@Constraints.MaxLength(15)
	public String phone;

	@Constraints.MaxLength(15)
	public String fax;

	@Constraints.MaxLength(15)
	public String mobilePhone;

	@Constraints.MaxLength(100)
	public String address1;

	@Constraints.MaxLength(100)
	public String address2;

	@Constraints.MaxLength(20)
	public String city;

	@Constraints.Email
	@Constraints.MaxLength(100)
	public String email;

	@Constraints.MaxLength(100)
	public String website;

	@Constraints.MaxLength(3)
	public String excCode;

	@Lob
	public String note;

	public ContactStatus status = ContactStatus.Normal;

	@ManyToOne
	public ContactCategory category;

	@ManyToOne
	public SaleSeller seller;
	
	@ManyToOne
	public StockPriceList priceList;

	public Boolean isActive = Boolean.TRUE;

	public Contact() {
		super();
	}

	public Contact(Integer id) {
		this();
		this.id = id;
	}

	public Contact(String name) {
		this();
		this.name = name;
	}

	public static Page<Contact> page(ContactSearchParam searchParam) {
		ExpressionList<Contact> expList = ModelHelper.getExpressionList(RIGHT);

		if (searchParam.fullText != null && ! searchParam.fullText.isEmpty()) {
			expList.or(
				Expr.like("name", "%" + searchParam.fullText + "%"),
				Expr.like("code", "%" + searchParam.fullText + "%")
			);
		} else {
			if (searchParam.code != null && ! searchParam.code.isEmpty()) {
				expList.like("code", searchParam.code + "%");
			}
			if (searchParam.name != null && ! searchParam.name.isEmpty()) {
				expList.like("name", searchParam.name + "%");
			}
			if (searchParam.status != null) {
				expList.eq("status", searchParam.status);
			}
			if (searchParam.category != null && searchParam.category.id != null) {
				expList.eq("category", searchParam.category);
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

		return ModelHelper.getPage(RIGHT, expList, searchParam, false, "category");
	}

	public static Contact findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
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
		return Messages.get("audit.code") + this.code + " - " + Messages.get("audit.name") + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
