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
package data.transfer.ws2ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.ChqbllDetailHistory;
import models.ChqbllPayroll;
import models.ChqbllPayrollDetail;
import models.ChqbllPayrollSource;
import models.ChqbllType;
import utils.DateUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import controllers.global.Profiles;

import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Module;
import enums.Right;
import enums.TransType;
/**
 * @author mdpinar
*/
class ChqbllTransfer extends BaseTransfer {

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new ChqbllType(ChqbllSort.Cheque), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new ChqbllPayrollSource(ChqbllSort.Cheque), sourceWS, targetWS);
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("chqbll_type", targetWS);
		executeDeleteQueryForInfoTables("chqbll_payroll_source", targetWS);
	}

	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		if (willBeDestroyedAll) {
			Ebean.createSqlUpdate("delete from chqbll_detail_history").execute();
			Ebean.createSqlUpdate("delete from chqbll_detail_partial").execute();
			Ebean.createSqlUpdate("delete from chqbll_trans_detail").execute();
			Ebean.createSqlUpdate("delete from chqbll_trans where workspace = :workspace").setParameter("workspace", targetWS).execute();
			Ebean.createSqlUpdate("delete from chqbll_payroll_detail where workspace = :workspace").setParameter("workspace", targetWS).execute();
			Ebean.createSqlUpdate("delete from chqbll_payroll where workspace = :workspace").setParameter("workspace", targetWS).execute();
		} else {
			List<Right> rightList = new ArrayList<Right>();
			rightList.add(Right.CEK_MUSTERI_ACILIS_ISLEMI);
			rightList.add(Right.CEK_FIRMA_ACILIS_ISLEMI);
			rightList.add(Right.SENET_MUSTERI_ACILIS_ISLEMI);
			rightList.add(Right.SENET_FIRMA_ACILIS_ISLEMI);

			List<ChqbllPayroll> payrollList = Ebean.find(ChqbllPayroll.class)
														.where()
															.eq("workspace", targetWS)
															.in("right", rightList)
													.findList();
			if (payrollList != null && payrollList.size() > 0) {
				for (ChqbllPayroll payroll : payrollList) {
					if (! payroll.isClosed) payroll.delete();
				}
			}
		}
	}

	@Override
	public void transferTransaction(Date transDate, String description, int sourceWS, int targetWS) {
		transferTransaction(true, ChqbllSort.Cheque, transDate, description, sourceWS, targetWS);
		transferTransaction(true, ChqbllSort.Bill, transDate, description, sourceWS, targetWS);
		transferTransaction(false, ChqbllSort.Cheque, transDate, description, sourceWS, targetWS);
		transferTransaction(false, ChqbllSort.Bill, transDate, description, sourceWS, targetWS);
	}

	private void transferTransaction(boolean isCustomer, ChqbllSort sort, Date transDate, String description, int sourceWS, int targetWS) {
		/*
		 * Suitable steps for transfer
		 */
		List<ChqbllStep> stepList = new ArrayList<ChqbllStep>();
		stepList.add(ChqbllStep.Issued);
		stepList.add(ChqbllStep.InPortfolio);
		stepList.add(ChqbllStep.InCollection);
		stepList.add(ChqbllStep.Warrantee);
		stepList.add(ChqbllStep.InPursue);
		stepList.add(ChqbllStep.Bounced);
		stepList.add(ChqbllStep.PartCollection);
		stepList.add(ChqbllStep.PartPayment);

		List<ChqbllPayrollDetail> detailList = Ebean.find(ChqbllPayrollDetail.class)
													.fetch("cbtype")
													.fetch("contact")
													.fetch("bank")
													.where()
														.eq("workspace", sourceWS)
														.eq("sort", sort)
														.eq("isCustomer", isCustomer)
														.in("lastStep", stepList)
													.orderBy("dueDate, lastStep, contact")
												.findList();

		Right right = null;
		if (isCustomer) {
			if (sort.equals(ChqbllSort.Cheque)) {
				right = Right.CEK_MUSTERI_ACILIS_ISLEMI;
			} else {
				right = Right.SENET_MUSTERI_ACILIS_ISLEMI;
			}
		} else {
			if (sort.equals(ChqbllSort.Cheque)) {
				right = Right.CEK_FIRMA_ACILIS_ISLEMI;
			} else {
				right = Right.SENET_FIRMA_ACILIS_ISLEMI;
			}
		}

		if (detailList != null && detailList.size() > 0) {

			ChqbllPayroll payroll = new ChqbllPayroll();
			payroll.workspace = targetWS;
			payroll.right = right;
			payroll.receiptNo = 1;
			payroll.transType = TransType.Debt;
			payroll.description = description;
			payroll.transDate = transDate;
			payroll.avarageDate = transDate;
			payroll.transYear = DateUtils.getYear(transDate);
			payroll.transMonth = DateUtils.getYearMonth(transDate);
			payroll.insertBy = "super";
			payroll.insertAt = new Date();
			payroll.details = new ArrayList<ChqbllPayrollDetail>(); 

			int rowNo = 0;
			double total = 0;

			for(ChqbllPayrollDetail det: detailList) {
				
				ChqbllPayrollDetail detail = new ChqbllPayrollDetail();
				detail.trans = payroll;
				detail.workspace = targetWS;
				detail.isCustomer = isCustomer;
				detail.sort = sort;
				detail.portfolioNo = det.portfolioNo;
				detail.rowNo = ++rowNo;
				detail.serialNo = det.serialNo;

				if (det.contact != null) {
					detail.contact = findContactInTargetWS(det.contact.id, targetWS);
				}
				if (det.bank != null) {
					detail.bank = findBankInTargetWS(det.bank.id, targetWS);
				}
				if (det.cbtype != null) {
					detail.cbtype = findCBTypeInTargetWS(det.cbtype.id, targetWS);
				}

				detail.lastStep = det.lastStep;
				detail.owner = det.owner;
				detail.paymentPlace = det.paymentPlace;
				detail.bankAccountNo = det.bankAccountNo;
				detail.bankName = det.bankName;
				detail.bankBranch = det.bankBranch;
				detail.correspondentBranch = det.correspondentBranch;
				detail.dueDate = det.dueDate;
				detail.amount = det.amount;
				detail.excCode = det.excCode;
				detail.excRate = det.excRate;
				detail.excEquivalent = det.excEquivalent;
				detail.description = det.description;
				detail.contactName = det.contactName;
				detail.lastContactName = det.lastContactName;
				detail.dueYear = det.dueYear;
				detail.dueMonth = det.dueMonth;
				detail.surety = det.surety;
				detail.suretyAddress = det.suretyAddress;
				detail.suretyPhone1 = det.suretyPhone1;
				detail.suretyPhone2 = det.suretyPhone2;
				detail.totalPaid = det.totalPaid;

				payroll.details.add(detail);
				
				total += det.amount;

				//history
				ChqbllDetailHistory history = new ChqbllDetailHistory();
				history.detail = detail;
				history.sort = sort;
				history.stepDate = transDate;
				history.step = detail.lastStep;
				history.contact = detail.contact;
				history.bank = detail.bank;
				history.insertBy = payroll.insertBy;
				history.insertAt = payroll.insertAt;
				history.save();
			}
			
			payroll.rowCount = rowNo;
			payroll.excCode = Profiles.chosen().gnel_excCode;
			payroll.excRate = 1d;
			payroll.excEquivalent = total;
			payroll.total = total;

			payroll.saveForOpening();
		}
	}

	private ChqbllType findCBTypeInTargetWS(Integer id, int targetWS) {
		if (id == null) return null;

		SqlRow row = Ebean.createSqlQuery("select id from chqbll_type where workspace = :workspace and name = (select name from chqbll_type where id = :id )")
							.setParameter("id", id)
							.setParameter("workspace", targetWS)
						.findUnique();
		if (row != null) {
			Integer idValue = row.getInteger("id");
			if (idValue != null) {
				return Ebean.find(ChqbllType.class, idValue);
			}
		}
		return null;
	}
	
	@Override
	public Module getModule() {
		return Module.cheque;
	}
	
}
