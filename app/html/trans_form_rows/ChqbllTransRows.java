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

import java.util.ArrayList;
import java.util.List;

import models.ChqbllPayrollDetail;
import models.ChqbllTrans;
import play.i18n.Messages;
import utils.Format;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;

/**
 * @author mdpinar
*/
public class ChqbllTransRows {

	public static String build(ChqbllTrans trans) {
		StringBuilder row = new StringBuilder();

		List<ChqbllPayrollDetail> virtuals = trans.virtuals;
		if (virtuals == null) virtuals = new ArrayList<ChqbllPayrollDetail>();
		if (virtuals.size() == 0) virtuals.add(new ChqbllPayrollDetail());

		for (int i = 0; i < virtuals.size(); i++) {
			ChqbllPayrollDetail virtual = virtuals.get(i);
			row.append("<tr>");

				row.append("<td>");
					row.append("<a title='" + Messages.get("inspect") + "' style='width:75%;' class='btn btn-mini invid'>");
						row.append("<i class='icon-search' style='margin-top:2px'></i>");
						row.append("<span class='rowNumber'>"+(i+1)+"</span>");
					row.append("</a>");
					row.append("<input class='impid' type='hidden' id='virtuals["+i+"]_id' name='virtuals["+i+"].id' value='"+virtual.id+"' />");
					row.append("<input type='hidden' id='virtuals["+i+"]_transDetailId' name='virtuals["+i+"].transDetailId' value='"+virtual.transDetailId+"' />");
					row.append("<input type='hidden' id='virtuals["+i+"]_lastStep' name='virtuals["+i+"].lastStep' value='"+virtual.lastStep+"' />");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' readonly id='virtuals["+i+"]_portfolioNo' name='virtuals["+i+"].portfolioNo' value='"+virtual.portfolioNo+"' style='width:calc(100% - 5px);text-align:right' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' readonly id='virtuals["+i+"]_serialNo' name='virtuals["+i+"].serialNo' value='"+virtual.serialNo+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' readonly id='virtuals["+i+"]_dueDate' name='virtuals["+i+"].dueDate' value='"+Format.asDate(virtual.dueDate)+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");
				row.append("<td>");
					row.append("<input type='text' readonly  id='virtuals["+i+"]_amount' name='virtuals["+i+"].amount' value='"+virtual.amount+"' style='width:calc(100% - 5px);text-align:right' />");
				row.append("</td>");

				if(Profiles.chosen().gnel_hasExchangeSupport) {
					row.append("<td>");
						row.append("<input type='text' readonly id='virtuals["+i+"]_excCode' name='virtuals["+i+"].excCode' value='"+virtual.excCode+"' style='width:calc(100% - 5px);text-align:center'/>");
					row.append("</td>");
					row.append("<td>");
						row.append("<input type='text' readonly id='virtuals["+i+"]_excRate' name='virtuals["+i+"].excRate' value='"+virtual.excRate+"' style='width:calc(100% - 5px);text-align:right' />");
					row.append("</td>");
					row.append("<td>");
						row.append("<input type='text' readonly id='virtuals["+i+"]_excEquivalent' name='virtuals["+i+"].excEquivalent' value='"+virtual.excEquivalent+"' style='width:calc(100% - 5px);text-align:right' />");
					row.append("</td>");
				} else {
					row.append("<input type='hidden' id='virtuals["+i+"]_excCode' name='virtuals["+i+"].excCode' value='"+virtual.excCode+"' />");
					row.append("<input type='hidden' id='virtuals["+i+"]_excRate' name='virtuals["+i+"].excRate' value='"+virtual.excRate+"' />");
					row.append("<input type='hidden' id='virtuals["+i+"]_excEquivalent' name='virtuals["+i+"].excEquivalent' value='"+virtual.excEquivalent+"' />");
				}

				if (ChqbllStep.isCustomer(trans.right)) {
					row.append("<td>");
						row.append("<input type='text' readonly id='virtuals["+i+"]_owner' name='virtuals["+i+"].owner' value='"+virtual.owner+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
				}

				row.append("<td>");
					row.append("<input type='text' readonly id='virtuals["+i+"]_paymentPlace' name='virtuals["+i+"].paymentPlace' value='"+virtual.paymentPlace+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				if (ChqbllSort.Cheque.equals(trans.sort)) {
					row.append("<td>");
						row.append("<input type='text' readonly id='virtuals["+i+"]_bankName' name='virtuals["+i+"].bankName' value='"+virtual.bankName+"' style='width:calc(100% - 5px);' />");
					row.append("</td>");
					if (ChqbllStep.isCustomer(trans.right)) {
						row.append("<td>");
							row.append("<input type='text' readonly id='virtuals["+i+"]_bankBranch' name='virtuals["+i+"].bankBranch' value='"+virtual.bankBranch+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' readonly id='virtuals["+i+"]_bankAccountNo' name='virtuals["+i+"].bankAccountNo' value='"+virtual.bankAccountNo+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' readonly id='virtuals["+i+"]_correspondentBranch' name='virtuals["+i+"].correspondentBranch' value='"+virtual.correspondentBranch+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
					}
				} else {
					if (ChqbllStep.isCustomer(trans.right) && Profiles.chosen().cksn_hasSuretyFields) {
						row.append("<td>");
							row.append("<input type='text' readonly id='virtuals["+i+"]_surety' name='virtuals["+i+"].surety' value='"+virtual.surety+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' readonly id='virtuals["+i+"]_suretyAddress' name='virtuals["+i+"].suretyAddress' value='"+virtual.suretyAddress+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' readonly id='virtuals["+i+"]_suretyPhone1' name='virtuals["+i+"].suretyPhone1' value='"+virtual.suretyPhone1+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
						row.append("<td>");
							row.append("<input type='text' readonly id='virtuals["+i+"]_suretyPhone2' name='virtuals["+i+"].suretyPhone2' value='"+virtual.suretyPhone2+"' style='width:calc(100% - 5px);' />");
						row.append("</td>");
					}
				}

				row.append("<td>");
					row.append("<input type='text' readonly id='virtuals["+i+"]_cbtypeName' name='virtuals["+i+"]._cbtypeName' value='"+(virtual.cbtype != null ? virtual.cbtype.name : "")+"' style='width:calc(100% - 5px);' />");
				row.append("</td>");

				row.append("<td>");
					row.append("<input type='text' readonly id='virtuals["+i+"]_description' name='virtuals["+i+"].description' value='"+virtual.description+"' style='width:calc(100% - 5px);' />");
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

}
