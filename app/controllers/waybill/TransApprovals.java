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
package controllers.waybill;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.AbstractStockTransDetail;
import models.Contact;
import models.InvoiceTrans;
import models.InvoiceTransDetail;
import models.InvoiceTransFactor;
import models.InvoiceTransRelation;
import models.Stock;
import models.StockTrans;
import models.WaybillTrans;
import models.WaybillTransFactor;
import models.search.TransSearchParam;
import models.temporal.ReceiptListModel;
import models.temporal.WaybillTransStatusForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CurrencyUtils;
import utils.DateUtils;
import utils.DocNoUtils;
import utils.GlobalCons;
import utils.NumericUtils;
import utils.RefModuleUtil;
import utils.StringUtils;
import utils.TransStatusHistoryUtils;
import views.html.waybills.trans_approval.change_status;
import views.html.waybills.trans_approval.form;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import controllers.global.Profiles;
import enums.DocNoIncType;
import enums.Module;
import enums.Right;
import enums.RightLevel;
import enums.TransApprovalType;
import enums.TransType;

/**
 * @author mdpinar
*/
public class TransApprovals extends Controller {

	private final static Logger log = LoggerFactory.getLogger(TransApprovals.class);

	private final static Right RIGHT = Right.IRSL_ONAYLAMA_ADIMLARI;
	private final static Form<TransSearchParam> dataForm = form(TransSearchParam.class);
	private final static Form<WaybillTransStatusForm> statusForm = form(WaybillTransStatusForm.class);

	private static int sourceCount;
	private static int targetCount;
	private static String receiptType;

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		TransSearchParam sp = new TransSearchParam();
		sp.transType = Right.IRSL_SATIS_IRSALIYESI;

		return ok(form.render(dataForm.fill(sp), new ArrayList<ReceiptListModel>()));
	}

	public static Result submit() {
		Result hasProblem = AuthManager.hasProblem(RIGHT, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		sourceCount = 0;
		targetCount = 0;
		receiptType = "";

		Form<TransSearchParam> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(filledForm.errorsAsJson());
		} else {
			TransSearchParam model = filledForm.get();
			if (model.formAction != null) {
				if ("search".equals(model.formAction)) {
			    	return search(filledForm);
			    } else {
			    	if (model.details != null && model.details.size() > 0) {

			    		Ebean.beginTransaction();
			    		try {
			    			boolean isStatusChange = true;
		    				if ("change-status".equals(model.formAction)) {
		    					changeStatus(model);
		    				} else if ("redo".equals(model.formAction)) {
		    					redo(model.redoTransId);
		    				} else {
		    					isStatusChange = false;
				    			if (TransApprovalType.Contact.equals(model.approvalType) && ! "cancelled".equals(model.formAction)) { //ContactBased
				    				if ("invoice".equals(model.formAction)) {
										makeContactBasedInvoice(model);
									}
				    			} else { //ReceiptBased
				    				for (ReceiptListModel rlm : model.details) {
										if (rlm.isSelected && ! rlm.isCompleted) {
											if ("invoice".equals(model.formAction)) {
												makeReceiptBasedInvoice(rlm.id, model);
											}
										}
									}
				    			}
		    				}
		    				if (isStatusChange) {
					    		if (targetCount > 0) {
				    				flash("success", Messages.get("has.been.changed", targetCount));
				    			} else {
				    				flash("error", Messages.get("has.not.been.changed"));
				    			}
		    				} else {
				    			if (targetCount > 0) {
				    				flash("success", Messages.get("has.been.approved", sourceCount, Messages.get(model.transType.key), targetCount, Messages.get(receiptType)));
				    			} else {
				    				flash("error", Messages.get("has.not.been.created"));
				    			}
		    				}
				    		Ebean.commitTransaction();
				    
			    		} catch (Exception e) {
			    			Ebean.rollbackTransaction();
			    			flash("error", Messages.get("unexpected.problem.occured", e.getMessage()));
			    			log.error(e.getMessage(), e);
			    		}

			    	} else {
			    		flash("error", Messages.get("has.not.been.created"));
			    	}
			    	return search(filledForm);
			    }
			}

			flash("error", Messages.get("not.found", Messages.get("action")));
			return search(filledForm);
		}

	}

	private static Result search(Form<TransSearchParam> filledForm) {
		return ok(form.render(filledForm, WaybillTrans.findReceiptList(filledForm.get())));
	}

	private static void makeContactBasedInvoice(TransSearchParam approvalModel) {

		Right right = null;
		if (Right.IRSL_ALIS_IRSALIYESI.equals(approvalModel.transType)) {
			right = Right.FATR_ALIS_FATURASI;
		} else {
			right = Right.FATR_SATIS_FATURASI;
		}

		/*
		 * Her bir cari icin fis id leri toplanir
		 */
		Map<Integer, List<Integer>> transMap = new HashMap<Integer, List<Integer>>();
		for (ReceiptListModel rlm : approvalModel.details) {
			if (rlm.isSelected && ! rlm.isCompleted) {
				Integer contactId = -1;
				if (rlm.contactId != null) contactId = rlm.contactId;
				List<Integer> transIdList = transMap.get(contactId);
				if (transIdList == null) transIdList = new ArrayList<Integer>();

				transIdList.add(rlm.id);
				transMap.put(contactId, transIdList);

				sourceCount++;
				receiptType = right.key;
			}
		}

		double subtotal = 0;
		double taxTotal = 0;

		if (transMap.size() > 0) {
			for (Map.Entry<Integer, List<Integer>> entry : transMap.entrySet()) {

				String transIds = StringUtils.join(entry.getValue(), ",");

				String groupPart = "stock_id, name, unit, unit_ratio, unit2ratio, unit3ratio, unit1, unit2, unit3";
				if (Profiles.chosen().irsl_hasPrices) groupPart += ", base_price, price, tax_rate, exc_code, exc_rate";

				StringBuilder querySB = new StringBuilder();
				querySB.append("SELECT ");
				querySB.append(groupPart);
				querySB.append(", ");
				querySB.append("SUM(quantity) as sumQuantity, SUM(input) as sumInput, SUM(output) as sumOutput ");

				if (Profiles.chosen().irsl_hasPrices) {
					querySB.append(",SUM(amount) as sumAmount, SUM(tax_amount) as sumTax_amount, SUM(discount_amount) as sumDiscount_amount, ");
					querySB.append("SUM(total) as sumTotal, SUM(exc_equivalent) as sumExc_equivalent, SUM(plus_factor_amount) as sumPlus_factor_amount, ");
					querySB.append("SUM(minus_factor_amount) as sumMinus_factor_amount, SUM(in_total) as sumIn_total, SUM(out_total) as sumOut_total ");
				}

				querySB.append("FROM waybill_trans_detail ");
				querySB.append("WHERE trans_id IN (");
				querySB.append(transIds);
				querySB.append(") GROUP BY ");
				querySB.append(groupPart);
				querySB.append(" ORDER BY stock_id");

				List<SqlRow> detailList = Ebean.createSqlQuery(querySB.toString()).findList();
				if (detailList != null && detailList.size() > 0) {

					targetCount++;

					InvoiceTrans master = new InvoiceTrans();

					master.workspace = CacheUtils.getWorkspaceId();
					master.contact = Contact.findById(entry.getKey());
					master.receiptNo = DocNoUtils.findLastReceiptNo(right);
					master.right = right;
					master.transSource = approvalModel.invoiceTransSource;
					master.transPoint = approvalModel.submitTransPoint;
					master.privateCode = approvalModel.submitPrivateCode;
					master.depot = approvalModel.depot;
					master.seller = approvalModel.seller;

					if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) {
						master.transNo = DocNoUtils.findLastTransNo(right);
					} else {
						master.transNo = approvalModel.transNo;
					}

					master.transType = right.transType;
					master.isTaxInclude = Profiles.chosen().stok_isTaxInclude;
					master.roundingDigits = Profiles.chosen().stok_roundingDigits;
					master.excCode = Profiles.chosen().gnel_excCode;
					master.excRate = 1d;
					master.transYear = DateUtils.getYear(master.transDate);
					master.transMonth = DateUtils.getYearMonth(master.transDate);
					master.contactName = master.contact.name;
					master.contactTaxOffice = master.contact.taxOffice;
					master.contactTaxNumber = master.contact.taxNumber;
					master.contactAddress1 = master.contact.address1;
					master.contactAddress2 = master.contact.address2;

					List<InvoiceTransDetail> details = new ArrayList<InvoiceTransDetail>();
					for (SqlRow detailRow : detailList) {

						InvoiceTransDetail detail = new InvoiceTransDetail();
						detail.workspace = master.workspace;
						detail.trans = master;
						detail.receiptNo = master.receiptNo;
						detail.right = master.right;
						detail.transDate = master.transDate;
						detail.deliveryDate = master.deliveryDate;
						detail.transType = master.transType;
						detail.depot = master.depot;
						detail.contact = master.contact;
						detail.transPoint = master.transPoint;
						detail.privateCode = master.privateCode;
						detail.transYear = master.transYear; 
						detail.transMonth = master.transMonth;
						detail.seller = master.seller;

						detail.name = detailRow.getString("name");
						detail.quantity = detailRow.getDouble("sumQuantity");
						detail.unit = detailRow.getString("unit");
						detail.unit1 = detailRow.getString("unit1");
						detail.unit2 = detailRow.getString("unit2");
						detail.unit3 = detailRow.getString("unit3");
						detail.unitRatio = detailRow.getDouble("unit_ratio");
						detail.unit2Ratio = detailRow.getDouble("unit2ratio");
						detail.unit3Ratio = detailRow.getDouble("unit3ratio");
						detail.input = detailRow.getDouble("sumInput");
						detail.output = detailRow.getDouble("sumOutput");

						Stock stock = Stock.findById(detailRow.getInteger("stock_id"));
						detail.stock = stock;
						if (master.transType.equals(TransType.Input))
							detail.taxRate = stock.buyTax;
						else
							detail.taxRate = stock.sellTax;

						if (detailRow.getDouble("base_price") != null && detailRow.getDouble("base_price").doubleValue() > 0) {
							detail.basePrice = NumericUtils.round(detailRow.getDouble("base_price"));
							detail.price = NumericUtils.round(detailRow.getDouble("price"));
							detail.amount = NumericUtils.round(detailRow.getDouble("sumAmount"));
							detail.taxAmount = NumericUtils.round(detailRow.getDouble("sumTax_amount"));
							detail.discountAmount = NumericUtils.round(detailRow.getDouble("sumDiscount_amount"));
							detail.total = NumericUtils.round(detailRow.getDouble("sumTotal"));
							detail.excCode = detailRow.getString("exc_code");
							detail.excRate = detailRow.getDouble("exc_rate");
							detail.excEquivalent = NumericUtils.round(detailRow.getDouble("sumExc_equivalent"));
							detail.inTotal = detailRow.getDouble("sumIn_total");
							detail.outTotal = detailRow.getDouble("sumOut_total");
							detail.plusFactorAmount = detailRow.getDouble("sumPlus_factor_amount");
							detail.minusFactorAmount = detailRow.getDouble("sumMinus_factor_amount");
						} else {
							detail.basePrice = Stock.findBasePrice(master.contact, stock, right.transType);
							detail.price = Stock.findPriceByDetail(detail);
							detail.amount = NumericUtils.round(detail.quantity * detail.price);
							detail.taxAmount = NumericUtils.round((detail.amount * detail.taxRate) / 100);
							if (master.isTaxInclude) {
								detail.total = detail.amount + detail.taxAmount;
							} else {
								detail.total = detail.amount;
							}
							CurrencyUtils.setDetailExchange(detail, stock, master);

							if (right.transType.equals(TransType.Input)) {
								detail.inTotal = detail.total;
							} else {
								detail.outTotal = detail.total;
							}
						}

						if (detail.excEquivalent != null) subtotal += detail.excEquivalent.doubleValue();
						if (detail.taxAmount != null) taxTotal += detail.taxAmount.doubleValue();

						detail.netInput = detail.input;
						detail.netInTotal = detail.inTotal;
						detail.netOutput = detail.output;
						detail.netOutTotal = detail.outTotal;

						details.add(detail);
					}

					if (Profiles.chosen().irsl_hasPrices) {
						StringBuilder totalQuerySB = new StringBuilder();
						totalQuerySB.append("SELECT SUM(total) as sumTotal, SUM(rounding_discount) as sumRounding_discount, SUM(discount_total) as sumDiscount_total, ");
						totalQuerySB.append("SUM(subtotal) as sumSubtotal, SUM(plus_factor_total) as sumPlus_factor_total, SUM(minus_factor_total) as sumMinus_factor_total, ");
						totalQuerySB.append("SUM(tax_total) as sumTax_total, SUM(net_total) as sumNet_total, SUM(exc_equivalent) as sumExc_equivalent ");
						totalQuerySB.append("FROM waybill_trans ");
						totalQuerySB.append("WHERE id IN (");
						totalQuerySB.append(transIds);
						totalQuerySB.append(")");

						List<SqlRow> totalRow = Ebean.createSqlQuery(totalQuerySB.toString()).findList();
						if (totalRow != null) {
							master.total = totalRow.get(0).getDouble("sumTotal");
							master.roundingDiscount = totalRow.get(0).getDouble("sumRounding_discount");
							master.discountTotal = totalRow.get(0).getDouble("sumDiscount_total");
							master.subtotal = totalRow.get(0).getDouble("sumSubtotal");
							master.plusFactorTotal = totalRow.get(0).getDouble("sumPlus_factor_total");
							master.minusFactorTotal = totalRow.get(0).getDouble("sumMinus_factor_total");
							master.taxTotal = totalRow.get(0).getDouble("sumTax_total");
							master.netTotal = totalRow.get(0).getDouble("sumNet_total");
							master.excEquivalent = totalRow.get(0).getDouble("sumExc_equivalent");
						}
					} else {
						subtotal = NumericUtils.round(subtotal);
						taxTotal = NumericUtils.round(taxTotal);

						master.total = subtotal;
						master.subtotal = subtotal;
						master.taxTotal = taxTotal;
						master.roundingDiscount = NumericUtils.roundingDiscount(subtotal, master.roundingDigits);
						master.netTotal = subtotal - master.roundingDiscount;
						master.excEquivalent = subtotal - master.roundingDiscount;
					}

					/*
					 * Relations
					 */
					List<InvoiceTransRelation> relations = new ArrayList<InvoiceTransRelation>();

					StringBuilder relationSB = new StringBuilder();
					relationSB.append("SELECT id, receipt_no, _right ");
					relationSB.append("FROM waybill_trans ");
					relationSB.append("WHERE id IN (");
					relationSB.append(transIds);
					relationSB.append(") ORDER BY receipt_no");

					List<SqlRow> relationList = Ebean.createSqlQuery(relationSB.toString()).findList();
					if (relationList != null && relationList.size() > 0) {
						for (SqlRow relationRow : relationList) {
							InvoiceTransRelation rel = new InvoiceTransRelation();
							rel.trans = master;
							rel.relId = relationRow.getInteger("id");
							rel.relRight = Right.valueOf(relationRow.getString("_right"));
							rel.relReceiptNo = relationRow.getInteger("receipt_no");
							relations.add(rel);
						}
					}

					master.details = details;
					master.relations = relations;

					/*
					 * if there is only one waybill, we can set the real date of the invoice from that waybill
					 */
					if (entry.getValue().size() == 1) {
						WaybillTrans trans = WaybillTrans.findById(new Integer(entry.getValue().get(0)));
						master.realDate = (trans.realDate != null ? trans.realDate : trans.transDate);
					}

					RefModuleUtil.save(true, master, Module.invoice, master.contact, false);

					Ebean.createSqlUpdate("update waybill_trans set invoice_id = :invoice_id, is_completed = :is_completed where id in (:id)")
							.setParameter("id", entry.getValue())
							.setParameter("invoice_id", master.id)
							.setParameter("is_completed", GlobalCons.TRUE)
						.execute();

					Ebean.createSqlUpdate("update waybill_trans_detail set completed = (net_input+net_output), cancelled = 0 where trans_id in (:transId)")
							.setParameter("transId", entry.getValue())
						.execute();

				}

				/*
				 * Stok yansimalari artik fatura kismindan idare edilecegi icin, faturalasan irsaliyelerin stok yansimalari silinir.
				 */
				for (Integer id : entry.getValue()) {
					StockTrans trans = StockTrans.findByRefIdAndModule(id, right);
					if (trans != null) trans.delete();
				}
			}
		}

	}

	private static void makeReceiptBasedInvoice(Integer sourceId, TransSearchParam approvalModel) {
		WaybillTrans source = WaybillTrans.findById(sourceId);

		Right right = null;
		if (Right.IRSL_ALIS_IRSALIYESI.equals(source.right)) {
			right = Right.FATR_ALIS_FATURASI;
		} else {
			right = Right.FATR_SATIS_FATURASI;
		}

		sourceCount++;
		targetCount++;
		receiptType = right.key;

		InvoiceTrans master = new InvoiceTrans();

		master.workspace = CacheUtils.getWorkspaceId();
		master.receiptNo = DocNoUtils.findLastReceiptNo(right);
		master.right = right;
		master.transSource = approvalModel.invoiceTransSource;
		master.transPoint = approvalModel.submitTransPoint;
		master.privateCode = approvalModel.submitPrivateCode;
		master.transType = right.transType;

		if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) {
			master.transNo = DocNoUtils.findLastTransNo(right);
		} else {
			if (approvalModel.transNo != null) {
				master.transNo = approvalModel.transNo;
			} else {
				master.transNo = source.transNo;
			}
		}
		if (source.excCode == null || source.excCode.trim().isEmpty()) {
			master.excCode = source.excCode;
			master.excRate = source.excRate;
		}
		if (master.excCode == null || master.excCode.trim().isEmpty()) {
			master.excCode = Profiles.chosen().gnel_excCode;
			master.excRate = 1d;
		}
		master.excEquivalent = source.excEquivalent;
		master.description = source.description;
		master.transYear = DateUtils.getYear(master.transDate);
		master.transMonth = DateUtils.getYearMonth(master.transDate);
		master.realDate = source.realDate;
		master.deliveryDate = source.deliveryDate;
		master.contact = source.contact;
		master.isTaxInclude = source.isTaxInclude;
		master.depot = source.depot;
		master.contactName = source.contactName;
		master.contactTaxOffice = source.contactTaxOffice;
		master.contactTaxNumber = source.contactTaxNumber;
		master.contactAddress1 = source.contactAddress1;
		master.contactAddress2 = source.contactAddress2;
		master.consigner = source.consigner;
		master.recepient = source.recepient;
		master.roundingDigits = source.roundingDigits;
		master.roundingDiscount = source.roundingDiscount;
		master.plusFactorTotal = source.plusFactorTotal;
		master.minusFactorTotal = source.minusFactorTotal;
		master.seller = source.seller;

		double subtotal = 0;
		double taxTotal = 0;

		/*
		 * Details
		 */
		List<InvoiceTransDetail> details = new ArrayList<InvoiceTransDetail>();
		if (source.details != null && source.details.size() > 0) {
			for (AbstractStockTransDetail det : source.details) {

				InvoiceTransDetail detail = new InvoiceTransDetail();
				detail.workspace = master.workspace;
				detail.trans = master;
				detail.receiptNo = master.receiptNo;
				detail.right = master.right;
				detail.transDate = master.transDate;
				detail.deliveryDate = master.deliveryDate;
				detail.transType = master.transType;
				detail.depot = master.depot;
				detail.contact = master.contact;
				detail.transPoint = master.transPoint;
				detail.privateCode = master.privateCode;
				detail.transYear = master.transYear; 
				detail.transMonth = master.transMonth;
				detail.name = det.name;
				detail.quantity = det.quantity;
				detail.unit = det.unit;
				detail.unit1 = det.unit1;
				detail.unit2 = det.unit2;
				detail.unit3 = det.unit3;
				detail.unitRatio = det.unitRatio;
				detail.unit2Ratio = det.unit2Ratio;
				detail.unit3Ratio = det.unit3Ratio;
				detail.excCode = det.excCode;
				detail.excRate = det.excRate;
				detail.seller = det.seller;
				detail.input = det.input;
				detail.output = det.output;
				detail.description = det.description;

				Stock stock = Stock.findById(det.stock.id);
				detail.stock = stock;
				if (master.transType.equals(TransType.Input))
					detail.taxRate = stock.buyTax;
				else
					detail.taxRate = stock.sellTax;

				if (det.basePrice != null && det.basePrice.doubleValue() > 0) {
					detail.basePrice = det.basePrice;
					detail.price = det.price;
					detail.inTotal = det.inTotal;
					detail.outTotal = det.outTotal;
					detail.taxAmount = det.taxAmount;
					detail.discountRate1 = det.discountRate1;
					detail.discountRate2 = det.discountRate2;
					detail.discountRate3 = det.discountRate3;
					detail.discountAmount = det.discountAmount;
					detail.amount = det.amount;
					detail.total = det.total;
					detail.excEquivalent = det.excEquivalent;
					detail.plusFactorAmount = det.plusFactorAmount;
					detail.minusFactorAmount = det.minusFactorAmount;
				} else {
					detail.basePrice = Stock.findBasePrice(master.contact, stock, right.transType);
					detail.price = Stock.findPriceByDetail(detail);
					detail.amount = NumericUtils.round(detail.quantity * detail.price);
					detail.taxAmount = NumericUtils.round((detail.amount * detail.taxRate) / 100);
					if (master.isTaxInclude) {
						detail.total = detail.amount + detail.taxAmount;
					} else {
						detail.total = detail.amount;
					}
					CurrencyUtils.setDetailExchange(detail, stock, master);

					if (right.transType.equals(TransType.Input)) {
						detail.inTotal = detail.total;
					} else {
						detail.outTotal = detail.total;
					}
				}

				if (detail.excEquivalent != null) subtotal += detail.excEquivalent.doubleValue();
				if (detail.taxAmount != null) taxTotal += detail.taxAmount.doubleValue();

				detail.netInput = detail.input;
				detail.netInTotal = detail.inTotal;
				detail.netOutput = detail.output;
				detail.netOutTotal = detail.outTotal;

				details.add(detail);
			}
		}

		if (Profiles.chosen().irsl_hasPrices) {
			master.total = source.total;
			master.discountTotal = source.discountTotal;
			master.subtotal = source.subtotal;
			master.taxTotal = source.taxTotal;
			master.netTotal = source.netTotal;
		} else {
			subtotal = NumericUtils.round(subtotal);
			taxTotal = NumericUtils.round(taxTotal);

			master.total = subtotal;
			master.subtotal = subtotal;
			master.taxTotal = taxTotal;
			master.roundingDiscount = NumericUtils.roundingDiscount(subtotal, master.roundingDigits);
			master.netTotal = subtotal - master.roundingDiscount;
			master.excEquivalent = subtotal - master.roundingDiscount;
		}

		/*
		 * Factors
		 */
		List<InvoiceTransFactor> factors = new ArrayList<InvoiceTransFactor>();
		if (source.factors != null && source.factors.size() > 0) {
			for (WaybillTransFactor fct : source.factors) {
				InvoiceTransFactor factor = new InvoiceTransFactor();

				factor.trans = master;
				factor.factor = fct.factor;
				factor.effect = fct.effect;
				factor.amount = fct.amount;

				factors.add(factor);
			}
		}

		/*
		 * Relations
		 */
		List<InvoiceTransRelation> relations = new ArrayList<InvoiceTransRelation>();
		InvoiceTransRelation rel = new InvoiceTransRelation();
		rel.trans = master;
		rel.relId = source.id;
		rel.relRight = source.right;
		rel.relReceiptNo = source.receiptNo;
		relations.add(rel);

		/*
		 * Last settings
		 */
		master.details = details;
		master.factors = factors;
		master.relations = relations;

		RefModuleUtil.save(true, master, Module.invoice, master.contact, false);

		Ebean.createSqlUpdate("update waybill_trans set invoice_id = :invoice_id, is_completed = :is_completed where id = :id")
				.setParameter("id", sourceId)
				.setParameter("invoice_id", master.id)
				.setParameter("is_completed", GlobalCons.TRUE)
			.execute();

		Ebean.createSqlUpdate("update waybill_trans_detail set completed = (net_input+net_output), cancelled = 0 where trans_id = :trans_id")
				.setParameter("trans_id", sourceId)
			.execute();

		/*
		 * Stok yansimalari artik fatura kismindan idare edilecegi icin, faturalasan irsaliyelerin stok yansimalari silinir.
		 */
		StockTrans trans = StockTrans.findByRefIdAndModule(source.id, source.right);
		if (trans != null) trans.delete();
	}

	public static Result getChangeStatusForm(Integer oldStatusId) {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}
		
		return ok(
			change_status.render(statusForm.fill(new WaybillTransStatusForm()), oldStatusId).body()
		);
	}

	private static void changeStatus(TransSearchParam model) {
		if (model.newWaybillTransStatus == null) return;
		for (ReceiptListModel detail : model.details) {
			if (detail.isSelected && ! detail.isCompleted) {
				TransStatusHistoryUtils.goForward(Module.waybill, detail.id, model.newWaybillTransStatus.id, model.description);
				targetCount++;
			}
		}
	}

	private static void redo(Integer transId) {
		if (transId != null) {
			TransStatusHistoryUtils.goBack(Module.waybill, transId);
			targetCount++;
		}
	}

}
