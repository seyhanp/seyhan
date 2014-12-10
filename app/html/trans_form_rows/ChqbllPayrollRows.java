/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package html.trans_form_rows;

import java.util.List;
import java.util.Map;

import models.Bank;
import models.ChqbllPayroll;
import models.ChqbllPayrollDetail;
import models.ChqbllType;
import models.GlobalCurrency;
import play.i18n.Messages;
import utils.Format;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Right;

/**
 * @author mdpinar
*/
public class ChqbllPayrollRows {

	public static String build(ChqbllPayroll payroll) {
		StringBuilder row = new StringBuilder();

		List<ChqbllPayrollDetail> details = payroll.details;
		if (details.size() == 0) details.add(new ChqbllPayrollDetail());

		for (int i = 0; i < details.size(); i++) {
			ChqbllPayrollDetail detail = details.get(i);
			row.append("<tr>");

				row.append("<td>");
					row.append("<a title='" + Messages.get("inspect") + "' style='width:75%;' class='btn btn-mini invid'>");
						row.append("<i class='icon-search' style='margin-top:2px'></i>");
						row.append("<span class='rowNumber'>"+(i+1)+"</span>");
					row.append("</a>");
					row.append("<input class='impid' type='hidden' id='details["+i+"]_id' name='details["+i+"].id' value='"+detail.id+"' />");
					row.append("<input type='hidden' id='details["+i+"]_workspace' name='details["+i+"].workspace' value='"+detail.workspace+"' />");
					if (isOpening(payroll.right)) {
						row.append("<input type='hidden' id='details["+i+"]_contact_id' name='details["+i+"].contact.id' value='"+(detail.contact != null ? detail.contact.id : "")+"' />");
					} else {
						row.append("<input type='hidden' id='details["+i+"]_lastStep' name='details["+i+"].lastStep' value='"+detail.lastStep+"' />");
					}
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' readonly id='details["+i+"]_portfolioNo' name='details["+i+"].portfolioNo' value='"+detail.portfolioNo+"' style='width:calc(100% - 5px);text-align:right' />");
				row.append("</td>");

				if (isOpening(payroll.right)) {
					row.append("<td>");
						row.append("<input type='text' readonly id='details["+i+"]_lastContactName' name='details["+i+"].lastContactName' value='"+detail.lastContactName+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
					row.append("<td>");
						row.append("<select id='details["+i+"]_lastStep' name='details["+i+"].lastStep' style='width:100%;'>");
							List<String> steps = ChqbllStep.openingOptions(payroll.right);
							for (String step: steps) {
								row.append("<option value='"+step+"' " + (detail.lastStep != null && detail.lastStep.name().equals(step) ? "selected>" : ">"));
									row.append(Messages.get("enum.cqbl.step."+step));
								row.append("</option>");
							}
						row.append("</select>");
					row.append("</td>");
				}

				row.append("<td>");
					row.append("<input type='text' id='details["+i+"]_serialNo' name='details["+i+"].serialNo' value='"+detail.serialNo+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' class='attention mainInput input-small date' id='details["+i+"]_dueDate' name='details["+i+"].dueDate' value='"+Format.asDate(detail.dueDate)+"' style='width:calc(100% - 29px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' class='currency attention' id='details["+i+"]_amount' name='details["+i+"].amount' value='"+detail.amount+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				if(Profiles.chosen().gnel_hasExchangeSupport) {
					row.append("<td>");
						row.append("<select id='details["+i+"]_excCode' name='details["+i+"].excCode' class='stocks attention' style='width:100%;'>");
							for (String cur: GlobalCurrency.options()) {
								row.append("<option value='"+cur+"' " + (detail.excCode != null && detail.excCode.equals(cur) ? "selected>" : ">"));
									row.append(cur);
								row.append("</option>");
							}
						row.append("</select>");
					row.append("</td>");
					row.append("<td>");
						row.append("<input type='text' class='rate attention' id='details["+i+"]_excRate' name='details["+i+"].excRate' value='"+detail.excRate+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
					row.append("<td>");
						row.append("<input type='text' class='currency attention' id='details["+i+"]_excEquivalent' name='details["+i+"].excEquivalent' value='"+detail.excEquivalent+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
				} else {
					row.append("<input type='hidden' id='details["+i+"]_excCode' name='details["+i+"].excCode' value='"+detail.excCode+"' />");
					row.append("<input type='hidden' id='details["+i+"]_excRate' name='details["+i+"].excRate' value='"+detail.excRate+"' />");
					row.append("<input type='hidden' id='details["+i+"]_excEquivalent' name='details["+i+"].excEquivalent' value='"+detail.excEquivalent+"' />");
				}
				if (ChqbllStep.isCustomer(payroll.right)) {
					row.append("<td>");
						row.append("<input type='text' id='details["+i+"]_owner' name='details["+i+"].owner' value='"+detail.owner+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
				}
				row.append("<td>");
					row.append("<input type='text' id='details["+i+"]_paymentPlace' name='details["+i+"].paymentPlace' value='"+detail.paymentPlace+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				if (ChqbllSort.Cheque.equals(payroll.sort)) {
					if (! ChqbllStep.isCustomer(payroll.right)) {
						row.append("<td>");
							row.append("<select id='details["+i+"]_bank_id' name='details["+i+"].bank.id' style='width:100%;'>");
								row.append("<option class='blank' value=''>" + Messages.get("choose") + "</option>");
								for (Map.Entry<String, String> entry: Bank.options().entrySet()) {
									row.append("<option value='"+entry.getKey()+"' " + (detail.bank != null && detail.bank.id != null && detail.bank.id.toString().equals(entry.getKey()) ? "selected>" : ">"));
									row.append(entry.getValue());
									row.append("</option>");
								}
							row.append("</select>");
						row.append("</td>");
					} else {
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_bankName' name='details["+i+"].bankName' value='"+detail.bankName+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_bankBranch' name='details["+i+"].bankBranch' value='"+detail.bankBranch+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_bankAccountNo' name='details["+i+"].bankAccountNo' value='"+detail.bankAccountNo+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_correspondentBranch' name='details["+i+"].correspondentBranch' value='"+detail.correspondentBranch+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
					}
				} else {
					if (ChqbllStep.isCustomer(payroll.right) && Profiles.chosen().cksn_hasSuretyFields) {
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_surety' name='details["+i+"].surety' value='"+detail.surety+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_suretyAddress' name='details["+i+"].suretyAddress' value='"+detail.suretyAddress+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_suretyPhone1' name='details["+i+"].suretyPhone1' value='"+detail.suretyPhone1+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' id='details["+i+"]_suretyPhone2' name='details["+i+"].suretyPhone2' value='"+detail.suretyPhone2+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
					}
				}

				row.append("<td>");
					row.append("<select id='details["+i+"]_cbtype_id' name='details["+i+"].cbtype.id' style='width:100%;'>");
						row.append("<option class='blank' value=''>" + Messages.get("choose") + "</option>");
						for (Map.Entry<String, String> entry: ChqbllType.options(payroll.sort).entrySet()) {
							row.append("<option value='"+entry.getKey()+"' " + (detail.cbtype != null && detail.cbtype.id != null && detail.cbtype.id.toString().equals(entry.getKey()) ? "selected>" : ">"));
							row.append(entry.getValue());
							row.append("</option>");
						}
					row.append("</select>");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' id='details["+i+"]_description' name='details["+i+"].description' value='"+detail.description+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				
				row.append("<td>");
					row.append("<a class='btn btn-mini delRow' title='"+Messages.get("remove") + "'>");
						row.append("<i class='icon-remove' style='margin-top:1px'></i>");
					row.append("</a>");
				row.append("</td>");

			row.append("</tr>");
		}

		return row.toString().replaceAll("null", "");
	}

	private static boolean isOpening(Right right) {
		return (Right.CEK_FIRMA_ACILIS_ISLEMI.equals(right) || Right.SENET_FIRMA_ACILIS_ISLEMI.equals(right)
			||  Right.CEK_MUSTERI_ACILIS_ISLEMI.equals(right) || Right.SENET_MUSTERI_ACILIS_ISLEMI.equals(right));
	}
}
