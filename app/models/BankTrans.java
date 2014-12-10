/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.search.BankTransSearchParam;
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
public class BankTrans extends AbstractDocTrans {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@Constraints.Required
	public Bank bank;

	@ManyToOne
	public BankExpense expense;

	public Double expenseAmount;

	@ManyToOne
	public BankTransSource transSource;

	public BankTrans(Right right) {
		super(right);
	}

	public static Page<BankTrans> page(BankTransSearchParam searchParam, Right right) {
		ExpressionList<BankTrans> expList = ModelHelper.getExpressionList(right.module);

		expList.eq("right", right);

		if (searchParam.fullText != null && ! searchParam.fullText.isEmpty()) {
			expList.or(
				Expr.like("description", "%" + searchParam.fullText + "%"),
				Expr.or(
						Expr.like("transNo", "%" + searchParam.fullText + "%"),
						Expr.like("bank.name", "%" + searchParam.fullText + "%")
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

			if (searchParam.bank != null && searchParam.bank.id != null) {
				expList.eq("bank", searchParam.bank);
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

		return ModelHelper.getPage(right, expList, searchParam, "bank");
	}

	public static BankTrans findById(Integer id) {
		return ModelHelper.findById(Module.bank, id);
	}

	public static BankTrans findByRefIdAndRight(Integer id, Right searchRight) {
		return ModelHelper.findByRefIdAndRight(Module.bank, id, searchRight);
	}

	@Override
	@Transient
	public String getTableName() {
		return "bank_trans";
	}

}
