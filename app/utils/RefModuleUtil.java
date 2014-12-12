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
package utils;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.OptimisticLockException;

import models.AbstractBaseTrans;
import models.AbstractDocTrans;
import models.AbstractStockTrans;
import models.Bank;
import models.BankTrans;
import models.BankTransSource;
import models.ChqbllPayroll;
import models.Contact;
import models.ContactTrans;
import models.ContactTransSource;
import models.Safe;
import models.SafeTrans;
import models.SafeTransSource;
import models.temporal.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;

import com.avaje.ebean.Ebean;

import controllers.global.Profiles;
import enums.DocNoIncType;
import enums.Module;
import enums.Right;
import enums.TransType;

/**
 * @author mdpinar
*/
public class RefModuleUtil {

	private final static Logger log = LoggerFactory.getLogger(RefModuleUtil.class);

	private static List<Module> BASE_MODULE_LIST;

	static {
		BASE_MODULE_LIST = new ArrayList<Module>();
		BASE_MODULE_LIST.add(Module.contact);
		BASE_MODULE_LIST.add(Module.safe);
		BASE_MODULE_LIST.add(Module.bank);
	}

	public static String save(AbstractBaseTrans source, Module module) {
		return save(source, module, null);
	}

	public static String save(AbstractBaseTrans source, Module module, Contact extraContact) {
		ContactTrans oldContactTrans = null;
		SafeTrans oldSafeTrans = null;
		BankTrans oldBankTrans = null;

		AbstractDocTrans newTrans = null;
		Module orphanModule = null;
		
		if (source.refId != null && source.refOldModule != null) {
			switch (source.refOldModule) {
				case contact: {
					oldContactTrans = ContactTrans.findById(source.refId);
					orphanModule = Module.contact;
					break;
				}
				case safe: {
					oldSafeTrans = SafeTrans.findById(source.refId);
					orphanModule = Module.safe;
					break;
				}
				case bank: {
					oldBankTrans = BankTrans.findById(source.refId);
					orphanModule = Module.bank;
					break;
				}
				default: {
					oldContactTrans = ContactTrans.findById(source.refId);
					orphanModule = Module.contact;
					break;
				}
			}
		}

		Ebean.beginTransaction();
		try {
			if (source.refModule != null) {
				switch (source.refModule) {

					case no: {
						source.refId = null;
						source.refModule = null;
						break;
					}
					case contact: {
						newTrans = (oldContactTrans != null ? oldContactTrans : new ContactTrans((module.equals(source.refModule) ? Right.CARI_VIRMAN : source.right)));
						((ContactTrans)newTrans).contact = source.refContact;
						((ContactTrans)newTrans).transSource = source.refContactTransSource;
						break;
					}
					case safe: {
						newTrans = (oldSafeTrans != null ? oldSafeTrans : new SafeTrans((module.equals(source.refModule) ? Right.KASA_VIRMAN : source.right)));
						((SafeTrans)newTrans).safe = source.refSafe;
						((SafeTrans)newTrans).transSource = source.refSafeTransSource;

						break;
					}
					case bank: {
						newTrans = (oldBankTrans != null ? oldBankTrans : new BankTrans((module.equals(source.refModule) ? Right.BANK_VIRMAN : source.right)));
						((BankTrans)newTrans).bank = source.refBank;
						((BankTrans)newTrans).transSource = source.refBankTransSource;

						break;
					}

				}
			}

			source.transYear = DateUtils.getYear(source.transDate);
			source.transMonth = DateUtils.getYearMonth(source.transDate);
			if (source.id == null) {
				if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) source.transNo = DocNoUtils.findLastTransNo(source.right);
				source.receiptNo = DocNoUtils.findLastReceiptNo(source.right);
			}

			double amount = 0d;
			double debt = 0d;
			double credit = 0d;
			if (source instanceof AbstractStockTrans) {
				AbstractStockTrans ast = (AbstractStockTrans) source;
				amount = ast.netTotal;
			} else if (source instanceof ChqbllPayroll) {
				ChqbllPayroll ast = (ChqbllPayroll) source;
				amount = ast.total;
			} else {
				AbstractDocTrans ast = (AbstractDocTrans) source;
				amount = ast.amount;
			}
			if (source.transType.equals(TransType.Debt) || source.transType.equals(TransType.Input)) {
				debt = amount;
			} else {
				credit = amount;
			}

			if (newTrans != null) {
				newTrans.workspace = source.workspace;
				newTrans.receiptNo = source.receiptNo;
				newTrans.refModule = module;
				newTrans.transType = (source.transType.equals(TransType.Debt) || source.transType.equals(TransType.Input) ? TransType.Credit : TransType.Debt);

				if (Profiles.chosen().gnel_hasExchangeSupport) {
					newTrans.amount = source.refExcEquivalent;
					if (newTrans.transType.equals(TransType.Debt)) {
						newTrans.debt = newTrans.amount;
						newTrans.credit = 0d;
					} else {
						newTrans.debt = 0d;
						newTrans.credit = newTrans.amount;
					}
					newTrans.excCode = source.refExcCode;
					newTrans.excRate = source.refExcRate;
					newTrans.excEquivalent = source.excEquivalent;
				} else {
					newTrans.amount = amount;
					newTrans.credit = debt;
					newTrans.debt = credit;
				}

				newTrans.transDate = source.transDate;
				newTrans.transNo = source.transNo;
				newTrans.description = source.description;
				newTrans.transYear = source.transYear;
				newTrans.transMonth = source.transMonth;

				if (newTrans.id == null) {
					if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) newTrans.transNo = DocNoUtils.findLastTransNo(newTrans.right);
					newTrans.singleSave();
				} else {
					newTrans.singleUpdate();
				}
				source.refId = newTrans.id;
			}

			if (source.id == null) {
				source.save();
			} else {
				source.update();
			}

			if (newTrans != null) {
				newTrans.refId = source.id;
				newTrans.refModule = source.right.module;
				newTrans.singleUpdate();
			}

			if (orphanModule != null && (source.refModule == null || ! source.refModule.equals(orphanModule))) {
				if (oldContactTrans != null) oldContactTrans.singleDelete();
				if (oldSafeTrans != null) oldSafeTrans.singleDelete();
				if (oldBankTrans != null) oldBankTrans.singleDelete();
			}

			/*
			 * Kasa, Banka ve Cari disindaki moduller ekstra Cari Hareket yansimasi yapabilirler
			 * 
			 * @see StockTrans 
			 */
			if (! BASE_MODULE_LIST.contains(source.right.module)) {
				ContactTrans extraTrans = ContactTrans.findByRefIdAndRight(source.id, source.right);

				boolean hasContact = (extraContact != null && extraContact.id != null);

				//Eski yansima var ve yeni yansima olmayacaksa eski yanisma silinir!
				if (extraTrans != null && ! hasContact) {
					extraTrans.singleDelete();
				// Yansima olacaksa; eski yanisma varsa update edilir, yoksa yeni bir tane olusturulur!
				} else if (hasContact) {
					if (extraTrans == null) extraTrans = new ContactTrans(source.right);

					extraTrans.workspace = source.workspace;
					extraTrans.contact = extraContact;
					extraTrans.receiptNo = source.receiptNo;
					extraTrans.transType = (source.transType.equals(TransType.Debt) || source.transType.equals(TransType.Input) ? TransType.Credit : TransType.Debt);
					extraTrans.amount = amount;
					extraTrans.debt = credit;
					extraTrans.credit = debt;
					extraTrans.excCode = source.excCode;
					extraTrans.excRate = source.excRate;
					extraTrans.excEquivalent = source.excEquivalent;
					extraTrans.transDate = source.transDate;
					extraTrans.transNo = source.transNo;
					extraTrans.description = source.description;
					extraTrans.transYear = source.transYear;
					extraTrans.transMonth = source.transMonth;

					extraTrans.refId = source.id;
					extraTrans.refModule = source.right.module;

					if (extraTrans.id == null) {
						extraTrans.singleSave();
					} else {
						extraTrans.singleUpdate();
					}
				}
			}

			Ebean.commitTransaction();

		} catch (Exception e) {
			Ebean.rollbackTransaction();
			log.error(e.getMessage(), e);
			if (e instanceof OptimisticLockException) {
				return "exception.optimistic.lock";
			}
		}
		
		return null;
	}

	public static void remove(AbstractBaseTrans trans) {
		Ebean.beginTransaction();
		try {
			if (trans.refModule != null && ! Module.no.equals(trans.refModule)) {
				switch (trans.refModule) {
					case contact: {
						ContactTrans.findById(trans.refId).singleDelete();
						break;
					}
					case safe: {
						SafeTrans.findById(trans.refId).singleDelete();
						break;
					}
					case bank: {
						BankTrans.findById(trans.refId).singleDelete();
						break;
					}
				}
			}

			/*
			 * Kasa, Banka ve Cari disindaki moduller ekstra Cari Hareket yansimasi yapmis olabilirler
			 * varsa bu kayit silinir
			 * 
			 * @see StockTrans 
			 */
			if (! BASE_MODULE_LIST.contains(trans.right.module)) {
				ContactTrans oldExtraTrans = ContactTrans.findByRefIdAndRight(trans.id, trans.right);
				if (oldExtraTrans != null) oldExtraTrans.singleDelete();
			}

			trans.singleDelete();
			Ebean.commitTransaction();

		} catch (Exception e) {
			Ebean.rollbackTransaction();
			log.error(e.getMessage(), e);
		}
	}

	public static void setTransientFields(AbstractBaseTrans trans) {
		AbstractDocTrans refTrans = null;

		Contact refContact = null;
		Safe refSafe = null;
		Bank refBank = null;

		ContactTransSource refContactTransSource = null;
		SafeTransSource refSafeTransSource = null;
		BankTransSource refBankTransSource = null;

		if (trans.refModule == null) return;

		switch (trans.refModule) {

			case contact: {
				refTrans = ContactTrans.findById(trans.refId);
				refContact = ((ContactTrans) refTrans).contact;
				refContactTransSource = ((ContactTrans) refTrans).transSource;
				break;
			}
			case safe: {
				refTrans = SafeTrans.findById(trans.refId);
				refSafe = ((SafeTrans) refTrans).safe;
				refSafeTransSource = ((SafeTrans) refTrans).transSource;
				break;
			}
			case bank: {
				refTrans = BankTrans.findById(trans.refId);
				refBank = ((BankTrans) refTrans).bank;
				refBankTransSource = ((BankTrans) refTrans).transSource;
				break;
			}
			default: {
				refTrans = ContactTrans.findByRefIdAndRight(trans.id, trans.right);
			}
		}

		trans.refOldModule = trans.refModule;
		trans.refContact = refContact;
		trans.refSafe = refSafe;
		trans.refBank = refBank;

		trans.refContactTransSource = refContactTransSource;
		trans.refSafeTransSource = refSafeTransSource;
		trans.refBankTransSource = refBankTransSource;

		if (refTrans != null) {
			trans.refExcCode = refTrans.excCode;
			trans.refExcRate = refTrans.excRate;
			trans.refExcEquivalent = refTrans.amount;
		}
	}
	
	public static Pair checkForRefAccounts(AbstractBaseTrans model) {
		return checkForRefAccounts(model, null, null, null);
	}

	public static Pair checkForRefAccounts(AbstractBaseTrans model, Contact contact) {
		return checkForRefAccounts(model, contact, null, null);
	}
	
	public static Pair checkForRefAccounts(AbstractBaseTrans model, Safe safe) {
		return checkForRefAccounts(model, null, safe, null);
	}
	
	public static Pair checkForRefAccounts(AbstractBaseTrans model, Bank bank) {
		return checkForRefAccounts(model, null, null, bank);
	}

	private static Pair checkForRefAccounts(AbstractBaseTrans model, Contact contact, Safe safe, Bank bank) {
		Pair result = new Pair(null, null);

		if (model.refModule != null) {
			switch (model.refModule) {
				case contact: {
					if (model.refContact.id == null) {
						result.key = "refContact.name";
						result.value = Messages.get("is.not.null", Messages.get("ref.contact"));
					} else {
						if (contact != null) {
							if (contact.id.equals(model.refContact.id)) {
								result.key = "refContact.name";
								result.value = Messages.get("ref.accounts.same");
							}
						}
						if (Profiles.chosen().gnel_hasExchangeSupport) {
							Contact cnt = Contact.findById(model.refContact.id);
							if (cnt.excCode != null && ! cnt.excCode.isEmpty() && ! cnt.excCode.equals(model.refExcCode)) {
								result.key = "refModule";
								result.value = Messages.get("error.exc_code", cnt.name, cnt.excCode);
							}
						}
					}
					break;
				}
				case safe: {
					if (model.refSafe.id == null) {
						result.key = "refSafe.id";
						result.value = Messages.get("is.not.null", Messages.get("ref.safe"));
					} else {
						if (safe != null) {
							if (safe.id.equals(model.refSafe.id)) {
								result.key = "refModule";
								result.value = Messages.get("ref.accounts.same");
							}
						}
						if (Profiles.chosen().gnel_hasExchangeSupport) {
							Safe sfe = Safe.findById(model.refSafe.id);
							if (sfe.excCode != null && ! sfe.excCode.isEmpty() && ! sfe.excCode.equals(model.refExcCode)) {
								result.key = "refModule";
								result.value = Messages.get("error.exc_code", sfe.name, sfe.excCode);
							}
						}
					}
					break;
				}
				case bank: {
					if (model.refBank.id == null) {
						result.key = "refBank.id";
						result.value = Messages.get("is.not.null", Messages.get("ref.bank"));
					} else {
						if (bank != null) {
							if (bank.id.equals(model.refBank.id)) {
								result.key = "refModule";
								result.value = Messages.get("ref.accounts.same");
							}
						}
						if (Profiles.chosen().gnel_hasExchangeSupport) {
							Bank bnk = Bank.findById(model.refBank.id);
							if (bnk.excCode != null && ! bnk.excCode.isEmpty() && ! bnk.excCode.equals(model.refExcCode)) {
								result.key = "refModule";
								result.value = Messages.get("error.exc_code", bnk.name, bnk.excCode);
							}
						}
					}
					break;
				}
			}
		}

		if (contact != null && contact.id != null && Profiles.chosen().gnel_hasExchangeSupport) {
			Contact cnt = Contact.findById(contact.id);
			if (cnt.excCode != null && ! cnt.excCode.isEmpty() && ! cnt.excCode.equals(model.excCode)) {
				result.key = "amount";
				result.value = Messages.get("error.exc_code", cnt.name, cnt.excCode);
			}
		}

		if (safe != null && Profiles.chosen().gnel_hasExchangeSupport) {
			Safe sfe = Safe.findById(safe.id);
			if (sfe.excCode != null && ! sfe.excCode.isEmpty() && ! sfe.excCode.equals(model.excCode)) {
				result.key = "amount";
				result.value = Messages.get("error.exc_code", sfe.name, sfe.excCode);
			}
		}

		if (bank != null && Profiles.chosen().gnel_hasExchangeSupport) {
			Bank bnk = Bank.findById(bank.id);
			if (bnk.excCode != null && ! bnk.excCode.isEmpty() && ! bnk.excCode.equals(model.excCode)) {
				result.key = "amount";
				result.value = Messages.get("error.exc_code", bnk.name, bnk.excCode);
			}
		}

		return result;
	}

}
