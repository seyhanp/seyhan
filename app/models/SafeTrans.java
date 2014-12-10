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

import models.search.SafeTransSearchParam;
import play.data.validation.Constraints;
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
public class SafeTrans extends AbstractDocTrans {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@Constraints.Required
	public Safe safe = Profiles.chosen().gnel_safe;

	@ManyToOne
	public SafeExpense expense;

	@ManyToOne
	public SafeTransSource transSource;

	public SafeTrans(Right right) {
		super(right);
	}

	public static Page<SafeTrans> page(SafeTransSearchParam searchParam, Right right) {
		ExpressionList<SafeTrans> expList = ModelHelper.getExpressionList(right.module);

		expList.eq("right", right);

		if (searchParam.fullText != null && ! searchParam.fullText.isEmpty()) {
			expList.or(
				Expr.like("description", "%" + searchParam.fullText + "%"),
				Expr.or(
						Expr.like("transNo", "%" + searchParam.fullText + "%"),
						Expr.like("safe.name", "%" + searchParam.fullText + "%")
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

			if (searchParam.safe != null && searchParam.safe.id != null) {
				expList.eq("safe", searchParam.safe);
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

		return ModelHelper.getPage(right, expList, searchParam, "safe", "expense");
	}

	public static SafeTrans findById(Integer id) {
		return ModelHelper.findById(Module.safe, id);
	}

	public static SafeTrans findByRefIdAndRight(Integer id, Right searchRight) {
		return ModelHelper.findByRefIdAndRight(Module.safe, id, searchRight);
	}

	@Override
	@Transient
	public String getTableName() {
		return "safe_trans";
	}

}
