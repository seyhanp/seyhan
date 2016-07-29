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
package models.temporal;

import java.util.Date;
import java.util.Map;

import javax.persistence.Transient;

import meta.SpecialFields;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.InvoiceTransStatus;
import models.OrderTransStatus;
import models.Safe;
import models.StockDepot;
import models.StockUnit;
import models.WaybillTransStatus;
import play.data.validation.Constraints;
import utils.GlobalCons;
import enums.DocNoIncType;
import enums.Module;
import enums.PrintAttitude;
import enums.ReceiptNoRnwType;
import enums.ReportUnit;
import enums.TransApprovalType;
import enums.TransListingType;

/**
 * @author mdpinar
*/
public class ProfileData {

	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	@Constraints.MaxLength(30)
	public String description;

	public Boolean isActive;

	public String insertBy;
	public String updateBy;
	public Date insertAt;
	public Date updateAt;

	/**
	 * Stock Tab
	 */
	public Boolean stok_isRowCombining;
	public Boolean stok_isSearchFormShowed;
	public StockDepot stok_depot;
	public StockUnit stok_unit;
	public Boolean stok_isTaxInclude;
	public Double stok_taxRate;
	public Boolean stok_hasLimitControls;
	public Integer stok_roundingDigits;
	public Map<String, Boolean> stok_specialFields;
	public Boolean stok_findLastSellingPrice;

	/**
	 * Contact Tab
	 */
	public Boolean cari_isSearchFormShowed;
	public Boolean cari_hasCategoryControls;

	/**
	 * Order Tab
	 */
	public Boolean sprs_isSearchFormShowed;
	public Boolean sprs_hasPrices;
	public OrderTransStatus sprs_status;
	public TransApprovalType sprs_approvalType;

	/**
	 * Waybill Tab
	 */
	public Boolean irsl_isSearchFormShowed;
	public Boolean irsl_hasPrices;
	public WaybillTransStatus irsl_status;
	public TransApprovalType irsl_approvalType;

	/**
	 * Invoice Tab
	 */
	public InvoiceTransStatus fatr_status;
	public Boolean fatr_isSearchFormShowed;
	public Boolean fatr_isCash;
	public Double fatr_withholding; //tevkifat orani 0, .2, .5, .7, .9
	public Map<String, Boolean> fatr_specialFields;

	/**
	 * Cheque/Bill Tab
	 */
	public Boolean cksn_isSearchFormShowed;
	public Boolean cksn_hasSuretyFields;

	/**
	 * General Tab
	 */
	public PrintAttitude gnel_printAttitude;
	public Boolean gnel_continuouslyRecording;
	public String gnel_excCode;
	public Boolean gnel_hasExchangeSupport;
	public ReportUnit gnel_reportUnit;
	public DocNoIncType gnel_docNoIncType;
	public ReceiptNoRnwType gnel_receiptNoRnwType;
	public Integer gnel_pennyDigitNumber;
	public Integer gnel_pageRowNumber;
	public GlobalTransPoint gnel_transPoint;
	public GlobalPrivateCode gnel_privateCode;
	public Safe gnel_safe;
	public TransListingType gnel_listingType;
	public Boolean kasa_isSearchFormShowed;
	public Boolean bank_isSearchFormShowed;

	public Integer version;
	
	public ProfileData() {
		this.name = "default";
		this.isActive = Boolean.TRUE;

		this.stok_unit = new StockUnit("");
		this.stok_isTaxInclude = Boolean.FALSE;
		this.stok_taxRate = 18d;
		this.stok_hasLimitControls = Boolean.TRUE;
		this.stok_specialFields = SpecialFields.stock;
		this.stok_isSearchFormShowed = Boolean.TRUE;
		this.stok_isRowCombining = Boolean.TRUE;
		this.stok_findLastSellingPrice = Boolean.FALSE;

		this.cari_hasCategoryControls = Boolean.FALSE;
		this.cari_isSearchFormShowed = Boolean.TRUE;

		this.sprs_hasPrices = Boolean.FALSE;
		this.sprs_approvalType = TransApprovalType.Contact;
		this.sprs_isSearchFormShowed = Boolean.TRUE;

		this.irsl_hasPrices = Boolean.FALSE;
		this.irsl_approvalType = TransApprovalType.Contact;
		this.irsl_isSearchFormShowed = Boolean.TRUE;

		this.fatr_withholding = 0d;
		this.fatr_isCash = Boolean.TRUE;
		this.fatr_specialFields = SpecialFields.invoice;
		this.fatr_isSearchFormShowed = Boolean.TRUE;

		this.cksn_hasSuretyFields = Boolean.FALSE;
		this.cksn_isSearchFormShowed = Boolean.TRUE;

		this.gnel_printAttitude = PrintAttitude.Manual;
		this.gnel_continuouslyRecording = Boolean.FALSE;

		this.gnel_excCode = GlobalCons.defaultExcCode;
		this.gnel_hasExchangeSupport = Boolean.TRUE;

		this.gnel_reportUnit = ReportUnit.Pdf;
		this.gnel_docNoIncType = DocNoIncType.Manual;
		this.gnel_receiptNoRnwType = ReceiptNoRnwType.Free;
		this.gnel_pennyDigitNumber = 2;
		this.gnel_pageRowNumber = 23;
		this.gnel_listingType = TransListingType.Free;

		this.kasa_isSearchFormShowed = Boolean.TRUE;
		this.bank_isSearchFormShowed = Boolean.TRUE;
		
		this.version = 0;
	}

	@Transient
	public boolean isFieldVisible(Module module, String field) {
		switch (module) {
			case stock: {
				if (stok_specialFields != null && stok_specialFields.containsKey(field)) return stok_specialFields.get(field);
				break;
			}
			case invoice: {
				if (fatr_specialFields != null && fatr_specialFields.containsKey(field)) return fatr_specialFields.get(field);
				break;
			}
		}

		return false;
	}

}
