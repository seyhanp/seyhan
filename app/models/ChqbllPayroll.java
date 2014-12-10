/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.search.ChqbllTransSearchParam;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints.Required;
import utils.ModelHelper;
import utils.QueryUtils;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Module;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class ChqbllPayroll extends AbstractBaseTrans {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	public Contact contact;

	public Integer rowCount;

	public Integer adat = 0;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date avarageDate;

	@Required
	public Double total = 0d;

	@ManyToOne
	public ChqbllPayrollSource transSource;

	public ChqbllSort sort = ChqbllSort.Cheque;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<ChqbllPayrollDetail> details;

	/*
	 * Detaydaki kayitlardan herhangi birisi hareket gormusse bordro kapatilir
	 */
	@Transient
	public Boolean isClosed = Boolean.FALSE;

	public static Page<ChqbllPayroll> page(ChqbllTransSearchParam searchParam, Right right) {
		ExpressionList<ChqbllPayroll> expList = ModelHelper.getExpressionList(ModelHelper.CHBL_PAYROLL);

		expList.eq("right", right);
		expList.eq("sort", (Module.cheque.equals(right.module) ? ChqbllSort.Cheque : ChqbllSort.Bill));

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

			if (searchParam.contact != null && searchParam.contact.id != null) {
				expList.eq("contact", searchParam.contact);
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

	public static ChqbllPayroll findById(Integer id) {
		ChqbllPayroll result = ModelHelper.findById(ModelHelper.CHBL_PAYROLL, id);

		if (Right.CEK_FIRMA_ACILIS_ISLEMI.equals(result.right) || Right.SENET_FIRMA_ACILIS_ISLEMI.equals(result.right)
		||  Right.CEK_MUSTERI_ACILIS_ISLEMI.equals(result.right) || Right.SENET_MUSTERI_ACILIS_ISLEMI.equals(result.right)) {
			result.isClosed = QueryUtils.isChqbllPayrollClosed(id);
		} else {
			ChqbllStep toStep = ChqbllStep.InPortfolio;
			if (Right.CEK_CIKIS_BORDROSU.equals(result.right) || Right.SENET_CIKIS_BORDROSU.equals(result.right)) toStep = ChqbllStep.Issued;
	
			for (ChqbllPayrollDetail detail : result.details) {
				if (! toStep.equals(detail.lastStep)) {
					result.isClosed = Boolean.TRUE;
					break;
				}
			}
		}

		return result;
	}

	public static ChqbllPayroll findByRefIdAndRight(Integer id, Right searchRight) {
		return ModelHelper.findByRefIdAndRight(ModelHelper.CHBL_PAYROLL, id, searchRight);
	}

	@Override
	@Transient
	public String getTableName() {
		return "chqbll_payroll";
	}

}
