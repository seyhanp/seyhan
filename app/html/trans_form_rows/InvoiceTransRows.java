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
package html.trans_form_rows;

import java.util.List;
import java.util.Map;

import models.GlobalCurrency;
import models.SaleSeller;
import models.InvoiceTrans;
import models.InvoiceTransDetail;
import play.i18n.Messages;
import controllers.global.Profiles;
import enums.Right;

/**
 * @author mdpinar
*/
public class InvoiceTransRows {

	public static String build(InvoiceTrans invoiceTrans) {
		StringBuilder row = new StringBuilder();

		List<InvoiceTransDetail> details = invoiceTrans.details;
		if (details.size() == 0) details.add(new InvoiceTransDetail());

		for (int i = 0; i < details.size(); i++) {
			InvoiceTransDetail detail = details.get(i);
			row.append("<tr>");

				row.append("<td>");
					row.append("<a title='" + Messages.get("inspect") + "' style='width:75%;' class='btn btn-mini invid'>");
						row.append("<i class='icon-search' style='margin-top:2px'></i>");
						row.append("<span class='rowNumber'>"+(i+1)+"</span>");
					row.append("</a>");
					row.append("<input type='hidden' id='details["+i+"]_id' name='details["+i+"].id' value='"+detail.id+"' />");
					row.append("<input class='impid' type='hidden' id='details["+i+"]_stock_id' name='details["+i+"].stock.id' value='"+(detail.stock != null ? detail.stock.id : "")+"' />");
					row.append("<input type='hidden' id='details["+i+"]_workspace' name='details["+i+"].workspace' value='"+detail.workspace+"' />");
					row.append("<input type='hidden' id='details["+i+"]_basePrice' name='details["+i+"].basePrice' value='"+detail.basePrice+"' />");
					row.append("<input type='hidden' id='details["+i+"]_unit1' name='details["+i+"].unit1' value='"+detail.unit1+"' />");
					row.append("<input type='hidden' id='details["+i+"]_unit2' name='details["+i+"].unit2' value='"+detail.unit2+"' />");
					row.append("<input type='hidden' id='details["+i+"]_unit3' name='details["+i+"].unit3' value='"+detail.unit3+"' />");
					row.append("<input type='hidden' id='details["+i+"]_unitRatio' name='details["+i+"].unitRatio' value='"+detail.unitRatio+"' />");
					row.append("<input type='hidden' id='details["+i+"]_unit2Ratio' name='details["+i+"].unit2Ratio' value='"+detail.unit2Ratio+"' />");
					row.append("<input type='hidden' id='details["+i+"]_unit3Ratio' name='details["+i+"].unit3Ratio' value='"+detail.unit3Ratio+"' />");
					row.append("<input type='hidden' id='details["+i+"]_plusFactorAmount' name='details["+i+"].plusFactorAmount' value='"+detail.plusFactorAmount+"' />");
					row.append("<input type='hidden' id='details["+i+"]_minusFactorAmount' name='details["+i+"].minusFactorAmount' value='"+detail.minusFactorAmount+"' />");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' class='details mainInput stock' id='details["+i+"]_name' name='details["+i+"].name' value='"+detail.name+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' class='amount attention stocks' id='details["+i+"]_quantity' name='details["+i+"].quantity' value='"+detail.quantity+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				row.append("<td>");
					row.append("<select id='details["+i+"]_unit' name='details["+i+"].unit' class='stocks attention' style='width:100%;'>");
						if (detail.unit1 != null && ! detail.unit1.isEmpty()) {
							row.append("<option value='"+detail.unit1+"' " + (detail.unit.equals(detail.unit1) ? "selected>" : ">"));
								row.append(detail.unit1);
							row.append("</option>");
							if (detail.unit2 != null && ! detail.unit2.isEmpty()) {
								row.append("<option value='"+detail.unit2+"' " + (detail.unit.equals(detail.unit2) ? "selected>" : ">"));
									row.append(detail.unit2);
								row.append("</option>");
								if (detail.unit3 != null && ! detail.unit3.isEmpty()) {
									row.append("<option value='"+detail.unit3+"' " + (detail.unit.equals(detail.unit3) ? "selected>" : ">"));
										row.append(detail.unit3);
									row.append("</option>");
								}
							}
						}
					row.append("</select>");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' class='currency stocks attention' id='details["+i+"]_price' name='details["+i+"].price' value='"+detail.price+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' class='currency stocks attention' id='details["+i+"]_amount' name='details["+i+"].amount' value='"+detail.amount+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				
				row.append("<td>");
					row.append("<input type='text' class='rate stocks attention' id='details["+i+"]_discountRate1' name='details["+i+"].discountRate1' value='"+detail.discountRate1+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' class='rate stocks attention' id='details["+i+"]_discountRate2' name='details["+i+"].discountRate2' value='"+detail.discountRate2+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' class='rate stocks attention' id='details["+i+"]_discountRate3' name='details["+i+"].discountRate3' value='"+detail.discountRate3+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' class='currency' id='details["+i+"]_discountAmount' name='details["+i+"].discountAmount' value='"+detail.discountAmount+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' class='rate stocks attention' id='details["+i+"]_taxRate' name='details["+i+"].taxRate' value='"+detail.taxRate+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				if (Profiles.chosen().isFieldVisible(enums.Module.invoice, "taxRate2")) {
					row.append("<td>");
						row.append("<input type='text' class='rate stocks attention' id='details["+i+"]_taxRate2' name='details["+i+"].taxRate2' value='"+detail.taxRate2+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
				}
				if (Profiles.chosen().isFieldVisible(enums.Module.invoice, "taxRate3")) {
					row.append("<td>");
					row.append("<input type='text' class='rate attention' id='details["+i+"]_taxRate3' name='details["+i+"].taxRate3' value='"+detail.taxRate3+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
				}
				row.append("<td>");
					row.append("<input type='text' class='currency' id='details["+i+"]_taxAmount' name='details["+i+"].taxAmount' value='"+detail.taxAmount+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' class='currency' id='details["+i+"]_total' name='details["+i+"].total' value='"+detail.total+"' style='width:calc(100% - 5px);' />");
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
				if (Profiles.chosen().isFieldVisible(enums.Module.invoice, "serialNo")) {
					row.append("<td>");
						row.append("<input type='text' id='details["+i+"]_serialNo' name='details["+i+"].serialNo' value='"+detail.serialNo+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
				}
				row.append("<td>");
					row.append("<input type='text' id='details["+i+"]_description' name='details["+i+"].description' value='"+detail.description+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				if (Right.FATR_SATIS_FATURASI.equals(invoiceTrans.right)) {
					row.append("<td>");
						row.append("<select id='details["+i+"]_seller_id' name='details["+i+"].seller.id' style='width:100%;'>");
							row.append("<option class='blank' value=''>" + Messages.get("choose") + "</option>");
							for (Map.Entry<String, String> entry: SaleSeller.options().entrySet()) {
								row.append("<option value='"+entry.getKey()+"' " + (detail.seller != null && detail.seller.id != null && detail.seller.id.toString().equals(entry.getKey()) ? "selected>" : ">"));
									row.append(entry.getValue());
								row.append("</option>");
							}
						row.append("</select>");
					row.append("</td>");
				}

				row.append("<td>");
					row.append("<a class='btn btn-mini delRow' title='"+Messages.get("remove") + "'>");
						row.append("<i class='icon-remove' style='margin-top:1px'></i>");
					row.append("</a>");
				row.append("</td>");

			row.append("</tr>");
		}

		return row.toString().replaceAll("null", "");
	}

}
