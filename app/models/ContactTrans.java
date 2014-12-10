/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.search.ContactTransSearchParam;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import utils.ModelHelper;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.Module;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class ContactTrans extends AbstractDocTrans {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@Constraints.Required
	public Contact contact;

	@ManyToOne
	public ContactTransSource transSource;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date maturity;

	public ContactTrans(Right right) {
		super(right);
	}

	public static Page<ContactTrans> page(ContactTransSearchParam searchParam, Right right) {
		ExpressionList<ContactTrans> expList = ModelHelper.getExpressionList(right.module);

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
			if (searchParam.maturity != null) {
				expList.eq("maturity", searchParam.maturity);
			}

			if (searchParam.refContact != null && searchParam.refContact.id != null) {
				expList.eq("contact", searchParam.refContact);
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

		return ModelHelper.getPage(right, expList, searchParam, "contact");
	}

	public static ContactTrans findById(Integer id) {
		return ModelHelper.findById(Module.contact, id);
	}

	public static ContactTrans findByRefIdAndRight(Integer id, Right searchRight) {
		return ModelHelper.findByRefIdAndRight(Module.contact, id, searchRight);
	}

	@Override
	@Transient
	public String getTableName() {
		return "contact_trans";
	}

}
