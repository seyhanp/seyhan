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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.search.ChqbllTransSearchParam;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints.Required;
import utils.ModelHelper;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Module;
import enums.Right;
import enums.TransType;

@Entity
/**
 * @author mdpinar
*/
public class ChqbllTrans extends AbstractBaseTrans {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	public Safe safe;

	@ManyToOne
	public Bank bank;

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

	public ChqbllStep fromStep;
	public ChqbllStep toStep;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="trans", orphanRemoval = true)
	public List<ChqbllTransDetail> details;

	@Transient
	public List<ChqbllPayrollDetail> virtuals;

	/*
	 * Detaydaki kayitlardan herhangi birisi hareket gormusse bordro kapatilir
	 */
	@Transient
	public Boolean isClosed = Boolean.FALSE;

	public static Page<ChqbllTrans> page(ChqbllTransSearchParam searchParam, Right right) {
		ExpressionList<ChqbllTrans> expList = ModelHelper.getExpressionList(ModelHelper.CHBL_TRANS);

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

			if (searchParam.toStep != null) {
				expList.eq("toStep", searchParam.toStep);
			}

			if (searchParam.contact != null && searchParam.contact.id != null) {
				expList.eq("contact", searchParam.contact);
			}
			if (searchParam.bank != null && searchParam.bank.id != null) {
				expList.eq("bank", searchParam.bank);
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

		return ModelHelper.getPage(right, expList, searchParam);
	}

	public static ChqbllTrans findById(Integer id) {
		ChqbllTrans result = ModelHelper.findById(ModelHelper.CHBL_TRANS, id);

		result.virtuals = new ArrayList<ChqbllPayrollDetail>();
		if (result != null) {
			for (ChqbllTransDetail td: result.details) {
				td.detail.transDetailId = td.id;
				result.virtuals.add(td.detail);
			}
		}

		for (ChqbllPayrollDetail detail : result.virtuals) {
			if (! result.toStep.equals(detail.lastStep)) {
				result.isClosed = Boolean.TRUE;
				break;
			}
		}

		return result;
	}

	public static ChqbllTrans findByRefIdAndRight(Integer id, Right searchRight) {
		return ModelHelper.findByRefIdAndRight(ModelHelper.CHBL_TRANS, id, searchRight);
	}

	@Override
	public void save() {
		saveOrUpdate();
		super.save();
		reflections();
	}

	@Override
	public void update() {
		saveOrUpdate();
		super.update();
		reflections();
	}

	@Override
	public void delete() {
		/*
		 * kayitlar eski haline getirilir
		 */
		for (ChqbllPayrollDetail virtual: virtuals) {
			if (toStep.equals(virtual.lastStep)) {
				ChqbllDetailHistory.goBack(virtual);
			}
		}

		/*
		 * yansimalar silinir
		 */
		Module refModule = ChqbllStep.findRefModule(fromStep, toStep);
		switch (refModule) {
			case safe: {
				SafeTrans extraTrans = SafeTrans.findByRefIdAndRight(id, right);
				if (extraTrans != null) extraTrans.singleDelete();
				break;
			}
			case bank: {
				BankTrans extraTrans = BankTrans.findByRefIdAndRight(id, right);
				if (extraTrans != null) extraTrans.singleDelete();
				break;
			}
			case contact: {
				ContactTrans extraTrans = ContactTrans.findByRefIdAndRight(id, right);
				if (extraTrans != null) extraTrans.singleDelete();
				break;
			}
		}

		super.delete();
	}

	private void saveOrUpdate() {
		/*
		 * Daha onceden kaydedilmis bir fis ise;
		 * varsa eski kayitlar, bunlar arasindan silinmisler eski haline getirilir. silinmeyip kalanlara birsey yapilmaz.
		 * 
		 * Yeni bir fis ise;
		 * standart kayit islemleri yapilir
		 */
		List<ChqbllTransDetail> removedTransDetails = new ArrayList<ChqbllTransDetail>();

		details = null;
		if (id != null) {
			details = findById(id).details;

			Set<Integer> virtualIdSet = new HashSet<Integer>();
			for (ChqbllPayrollDetail virtual: virtuals) {
				virtualIdSet.add(virtual.transDetailId);
			}
			for (ChqbllTransDetail detail: details) {
				if (! virtualIdSet.contains(detail.id)) removedTransDetails.add(detail);
			}

		} else {
			details = new ArrayList<ChqbllTransDetail>();
		}

		for (ChqbllPayrollDetail virtual: virtuals) {
			if (fromStep.equals(virtual.lastStep)) {
				ChqbllTransDetail detail = new ChqbllTransDetail();
				detail.trans = this;
				detail.detail =  ChqbllPayrollDetail.findById(virtual.id);

				ChqbllDetailHistory.goForward(detail.detail, toStep, contact, bank, safe);
				details.add(detail);
			}
		}

		if (removedTransDetails.size() > 0) {
			for (ChqbllTransDetail detail: removedTransDetails) {
				ChqbllDetailHistory.goBack(detail.detail);
				details.remove(detail);
			}
		}
	}

	private void reflections() {
		/*
		 * Varsa Kasa yansimasi yapilir
		 * Varsa Banka yansimasi yapilir
		 * Daha onceden Kasa yansimasi olmus fakat yeni kayitta yoksa yansima geri alinir
		 * Daha onceden Banka yansimasi olmus fakat yeni kayitta yoksa yansima geri alinir
		 */
		Module refModule = ChqbllStep.findRefModule(fromStep, toStep);
		switch (refModule) {
			case safe: {
				SafeTrans extraTrans = SafeTrans.findByRefIdAndRight(id, right);
				if (extraTrans == null) extraTrans = new SafeTrans(right);

				extraTrans.safe = safe;
				extraTrans.receiptNo = receiptNo;
				extraTrans.amount = excEquivalent;
				extraTrans.transNo = transNo;
				extraTrans.description = description;
				extraTrans.transYear = transYear;
				extraTrans.transMonth = transMonth;

				if (ChqbllStep.Collected.equals(toStep)) {
					extraTrans.transType = TransType.Debt;
					extraTrans.debt = extraTrans.amount;
					extraTrans.credit = 0d;
				} else {
					extraTrans.transType = TransType.Credit;
					extraTrans.debt = 0d;
					extraTrans.credit = extraTrans.amount;
				}
				extraTrans.excEquivalent = extraTrans.amount;
				extraTrans.excCode = excCode;
				extraTrans.excRate = excRate;

				extraTrans.refId = id;
				extraTrans.refModule = right.module;

				if (extraTrans.id == null) {
					extraTrans.singleSave();
				} else {
					extraTrans.singleUpdate();
				}

				break;
			}
			case bank: {
				BankTrans extraTrans = BankTrans.findByRefIdAndRight(id, right);
				if (extraTrans == null) extraTrans = new BankTrans(right);

				extraTrans.bank = bank;
				extraTrans.receiptNo = receiptNo;
				extraTrans.amount = excEquivalent;
				extraTrans.transNo = transNo;
				extraTrans.description = description;
				extraTrans.transYear = transYear;
				extraTrans.transMonth = transMonth;

				if (ChqbllStep.Deposited.equals(toStep)) {
					extraTrans.transType = TransType.Debt;
					extraTrans.debt = extraTrans.amount;
					extraTrans.credit = 0d;
				} else {
					extraTrans.transType = TransType.Credit;
					extraTrans.debt = 0d;
					extraTrans.credit = extraTrans.amount;
				}
				extraTrans.excEquivalent = extraTrans.amount;
				extraTrans.excCode = excCode;
				extraTrans.excRate = excRate;

				extraTrans.refId = id;
				extraTrans.refModule = right.module;

				if (extraTrans.id == null) {
					extraTrans.singleSave();
				} else {
					extraTrans.singleUpdate();
				}
				break;
			}
			case contact: {
				ContactTrans extraTrans = ContactTrans.findByRefIdAndRight(id, right);
				if (extraTrans == null) extraTrans = new ContactTrans(right);

				extraTrans.contact = contact;
				extraTrans.receiptNo = receiptNo;
				extraTrans.amount = excEquivalent;
				extraTrans.transNo = transNo;
				extraTrans.description = description;
				extraTrans.transYear = transYear;
				extraTrans.transMonth = transMonth;

				if (ChqbllStep.Endorsed.equals(toStep)) {
					extraTrans.transType = TransType.Debt;
					extraTrans.debt = extraTrans.amount;
					extraTrans.credit = 0d;
				} else {
					extraTrans.transType = TransType.Credit;
					extraTrans.debt = 0d;
					extraTrans.credit = extraTrans.amount;
				}
				extraTrans.excEquivalent = extraTrans.amount;
				extraTrans.excCode = excCode;
				extraTrans.excRate = excRate;

				extraTrans.refId = id;
				extraTrans.refModule = right.module;

				if (extraTrans.id == null) {
					extraTrans.singleSave();
				} else {
					extraTrans.singleUpdate();
				}
				break;
			}

			default: {
				if (id != null) {
					if (safe != null) {
						SafeTrans trans = SafeTrans.findByRefIdAndRight(id, right);
						if (trans != null) {
							trans.singleDelete();
						}
					}
					if (bank != null) {
						BankTrans trans = BankTrans.findByRefIdAndRight(id, right);
						if (trans != null) {
							trans.singleDelete();
						}
					}
					if (contact != null) {
						ContactTrans trans = ContactTrans.findByRefIdAndRight(id, right);
						if (trans != null) {
							trans.singleDelete();
						}
					}
				}
			}
		}
	}


	@Override
	@Transient
	public String getTableName() {
		return "chqbll_trans";
	}

}
