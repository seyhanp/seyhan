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
package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import meta.RightBind;
import models.Contact;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.SaleCampaign;
import models.SaleSeller;
import models.Stock;
import models.StockCategory;
import models.StockPriceList;
import models.temporal.ContactModel;
import models.temporal.StockCostFactorModel;
import models.temporal.StockModel;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import utils.DocNoUtils;
import utils.NumericUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.global.CurrencyRates;
import controllers.global.Profiles;
import enums.EffectDirection;
import enums.EffectType;
import enums.Module;
import enums.TransType;

/**
 * @author mdpinar
*/
public class AjaxServices extends Controller {

	public static Result getLastContactCode(String q) {
		return ok(Json.toJson(DocNoUtils.findLastCode(Module.contact, q)));
	}

	public static Result getLastStockCode(String q) {
		return ok(Json.toJson(DocNoUtils.findLastCode(Module.stock, q)));
	}

	public static Result autocompleteContact(String q, String d, String choice) {
		String categoryJoin = "";
		String categoryWhere = "";

		if (Profiles.chosen().cari_hasCategoryControls) {
			categoryJoin  = " left join contact_category cc on cc.id = c.category_id ";
			if (d != null && ! d.isEmpty()) {
				categoryWhere = " and (cc.workingDir is null or cc.workingDir = '" + TransType.findType(d) + "') ";
			}
		}

		String queryPart = " AND UPPER(name) like UPPER('" + q + "%') ";

		if (choice.equals("code")) {
			queryPart = " AND UPPER(code) like UPPER('%" + q + "%') ";
		}

		String sql = "SELECT c.id, c.code, c.name, c.tax_office, c.tax_number, c.address1, c.address2 "
				   + "FROM contact c " + categoryJoin
				   + "WHERE c.workspace = " + CacheUtils.getWorkspaceId()
				   + "  AND c.is_active = :active "
				   + "  AND c.status = 'Normal' " + categoryWhere
				   + queryPart;

		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		Query<ContactModel> query = Ebean.find(ContactModel.class);
		query.setRawSql(rawSql);
		query.setParameter("active", Boolean.TRUE);
		query.setMaxRows(10);

		List<ContactModel> modelList = query.findList();
		
		if (modelList != null && modelList.size() > 0) {
			return ok(Json.toJson(modelList));
		} else {
			return ok();
		}
	}

	public static Result autocompleteStock(String q, String d, Integer c) {
		String queryPart = " AND UPPER(name) like UPPER('" + q + "%') ";

		if (d.equals("code")) {
			queryPart = " AND UPPER(code) like UPPER('%" + q + "%') ";
		}

		String sql = "SELECT id, code, name, exc_code, buy_tax, sell_tax, tax_rate2, tax_rate3, "
				   + "buy_price, sell_price, unit1, unit2, unit3, unit2ratio, unit3ratio "
				   + "FROM stock "
				   + "WHERE workspace = " + CacheUtils.getWorkspaceId()
				   + "  AND is_active = :active "
				   + queryPart
				   + "ORDER BY " + d;
		
		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		Query<StockModel> query = Ebean.find(StockModel.class);
		query.setRawSql(rawSql);
		query.setParameter("active", Boolean.TRUE);
		query.setMaxRows(10);

		List<StockModel> modelList = query.findList();
		
		if (modelList != null && modelList.size() > 0) {
			setDiscounts(modelList);
			setPriceByList(modelList, c);
			return ok(Json.toJson(modelList));
		} else {
			return ok();
		}
	}

	public static Result findStockByBarcode(String barcode, Integer contactId) {
		if (barcode == null || barcode.trim().isEmpty()) return ok();
		String[] parts = barcode.split("[^\\d]");

		String sql = "SELECT s.id, code, name, exc_code, buy_tax, sell_tax, tax_rate2, tax_rate3, "
				   + "buy_price, sell_price, unit1, unit2, unit3, unit2ratio, unit3ratio, prefix, name, suffix, unit_no "
				   + "FROM stock as s, stock_barcode as b "
				   + "WHERE s.is_active = :active"
				   + "  AND s.id = b.stock_id"
				   + "  AND b.workspace = " + CacheUtils.getWorkspaceId()
				   + "  AND b.barcode = '" + parts[(parts.length == 1 ? 0 : 1)] + "'";

		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		Query<StockModel> query = Ebean.find(StockModel.class);
		query.setRawSql(rawSql);
		query.setParameter("active", Boolean.TRUE);

		StockModel result = query.findUnique();
		if (result != null && parts.length == 2) {
			result.number = NumericUtils.strToInteger(parts[0], 1);
			if (result.number.intValue() > 1000) result.number = 1000;
			if (result.number.intValue() < 1) result.number = 1;
		}
		if (result != null) {
			setDiscounts(result);
			setPriceByList(result, contactId);
			return ok(Json.toJson(result));
		} else {
			return ok();
		}
	}


	private static void setDiscounts(StockModel stockModel) {
		List<StockModel> modelList = new ArrayList<StockModel>();
		modelList.add(stockModel);
		setDiscounts(modelList);
	}
	
	private static void setDiscounts(List<StockModel> modelList) {
		try {
			List<SaleCampaign> campaignList = SaleCampaign.findActiveCampaigns();
			if (campaignList != null && campaignList.size() > 0) {

				for (StockModel stockModel : modelList) {
					Stock stock = Stock.findById(stockModel.id);

					if (stock != null) {
						boolean[] check = new boolean[11];
						for (int i = 0; i < check.length; i++) {
							check[i] = true;
						}

						for (SaleCampaign camp: campaignList) {
							if (camp.category != null && ! camp.category.equals(stock.category)) check[0] = false; 
							if (camp.extraField0 != null && ! camp.extraField0.equals(stock.extraField0)) check[1] = false;
							if (camp.extraField1 != null && ! camp.extraField1.equals(stock.extraField1)) check[2] = false;
							if (camp.extraField2 != null && ! camp.extraField2.equals(stock.extraField2)) check[3] = false;
							if (camp.extraField3 != null && ! camp.extraField3.equals(stock.extraField3)) check[4] = false;
							if (camp.extraField4 != null && ! camp.extraField4.equals(stock.extraField4)) check[5] = false;
							if (camp.extraField5 != null && ! camp.extraField5.equals(stock.extraField5)) check[6] = false;
							if (camp.extraField6 != null && ! camp.extraField6.equals(stock.extraField6)) check[7] = false;
							if (camp.extraField7 != null && ! camp.extraField7.equals(stock.extraField7)) check[8] = false;
							if (camp.extraField8 != null && ! camp.extraField8.equals(stock.extraField8)) check[9] = false;
							if (camp.extraField9 != null && ! camp.extraField9.equals(stock.extraField9)) check[10] = false;

							boolean isSuitable = true;
							for (int i = 0; i < check.length; i++) {
								if (! check[i]) {
									isSuitable = false;
									break;
								}
							}

							if (isSuitable) {
								stockModel.discountRate1 = (camp.discountRate1 != null ? camp.discountRate1 : 0);
								stockModel.discountRate2 = (camp.discountRate2 != null ? camp.discountRate2 : 0);
								stockModel.discountRate3 = (camp.discountRate3 != null ? camp.discountRate3 : 0);
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			;
		}
	}

	private static void setPriceByList(StockModel stockModel, Integer contactId) {
		if (contactId != null) {
			List<StockModel> modelList = new ArrayList<StockModel>();
			modelList.add(stockModel);
			setPriceByList(modelList, contactId);
		}
	}
	
	private static void setPriceByList(List<StockModel> modelList, Integer contactId) {
		if (contactId != null && contactId.intValue() > 0) {
			try {
				Contact contact = Contact.findById(contactId);
				StockPriceList priceList = contact.priceList;
				if (priceList != null && priceList.isActive) {
					
					Date today = new Date();
					if (DateUtils.isLessThan(today, priceList.startDate)) return;
					if (DateUtils.isGreateThan(today, priceList.endDate)) return;
					
					for (StockModel stockModel : modelList) {
						Stock stock = Stock.findById(stockModel.id);
						if (stock != null) {
							boolean[] check = new boolean[12];
							for (int i = 0; i < check.length; i++) {
								check[i] = true;
							}
							if (priceList.category != null && ! priceList.category.equals(stock.category)) check[0] = false; 
							if (priceList.providerCode != null && ! priceList.providerCode.equals(stock.providerCode)) check[1] = false;
							if (priceList.extraField0 != null && ! priceList.extraField0.equals(stock.extraField0)) check[2] = false;
							if (priceList.extraField1 != null && ! priceList.extraField1.equals(stock.extraField1)) check[3] = false;
							if (priceList.extraField2 != null && ! priceList.extraField2.equals(stock.extraField2)) check[4] = false;
							if (priceList.extraField3 != null && ! priceList.extraField3.equals(stock.extraField3)) check[5] = false;
							if (priceList.extraField4 != null && ! priceList.extraField4.equals(stock.extraField4)) check[6] = false;
							if (priceList.extraField5 != null && ! priceList.extraField5.equals(stock.extraField5)) check[7] = false;
							if (priceList.extraField6 != null && ! priceList.extraField6.equals(stock.extraField6)) check[8] = false;
							if (priceList.extraField7 != null && ! priceList.extraField7.equals(stock.extraField7)) check[9] = false;
							if (priceList.extraField8 != null && ! priceList.extraField8.equals(stock.extraField8)) check[10] = false;
							if (priceList.extraField9 != null && ! priceList.extraField9.equals(stock.extraField9)) check[11] = false;
							
							boolean isSuitable = true;
							for (int i = 0; i < check.length; i++) {
								if (! check[i]) {
									isSuitable = false;
									break;
								}
							}
							if (isSuitable) {
								
								boolean isSellPrice = (priceList.isSellPrice != null && priceList.isSellPrice);
								double price = (isSellPrice ? stock.sellPrice : stock.buyPrice);
								
								if (EffectDirection.Increase.equals(priceList.effectDirection)) {
									if (EffectType.Percent.equals(priceList.effectType)) {
										price = price + ((price * priceList.effect) / 100);
									} else {
										price = price + priceList.effect;
									}
								} else {
									if (EffectType.Percent.equals(priceList.effectType)) {
										price = price - ((price * priceList.effect) / 100);
									} else {
										price = price - priceList.effect;
									}
								}
								
								if (isSellPrice) {
									stockModel.sellPrice = NumericUtils.round(price);
								} else {
									stockModel.buyPrice = NumericUtils.round(price);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				;
			}
		}
	}

	public static Result findCostFactorById(String id) {
		String sql = "SELECT id, name, factor_type, calc_type, effect_type, effect "
				   + "FROM stock_cost_factor "
				   + "WHERE workspace = " + CacheUtils.getWorkspaceId()
				   + "  AND is_active = :active "
				   + "  AND id = " + id;

		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		Query<StockCostFactorModel> query = Ebean.find(StockCostFactorModel.class);
		query.setRawSql(rawSql);
		query.setParameter("active", Boolean.TRUE);

		StockCostFactorModel result = query.findUnique();
		if (result != null) {
			result.factorTypeOri = result.factorType;
			result.calcTypeOri = result.calcType;
			result.effectTypeOri = result.effectType;
			result.factorType = Messages.get(result.factorTypeOri.toLowerCase());
			result.calcType = Messages.get(result.calcTypeOri.toLowerCase());
			result.effectType = Messages.get(result.effectTypeOri.toLowerCase());
			return ok(Json.toJson(result));
		} else {
			return ok();
		}
	}
	
	public static Result checkUserForPService(String username, String password) {
		return ok(AuthManager.simpleAuthenticate(username, password));
	}

	public static Result getExchangeRates() {
		return ok(Json.toJson(CurrencyRates.getActualExchangeRatesMap()));
	}

	public static Result findLastTransNo(RightBind rightBind) {
		return ok(Json.toJson(DocNoUtils.findLastTransNo(rightBind.value)));
	}

	public static Result getSimpleData() {
		ObjectNode result = Json.newObject();

		result.put("exchange_rates", Json.toJson(CurrencyRates.getActualExchangeRatesMap()));
		result.put("sellers", Json.toJson(SaleSeller.crossOptions()));

		return ok(result);
	}

	public static Result getPrivateCodeTree() {
		return ok(GlobalPrivateCode.listAllAsJson());
	}

	public static Result getTransPointTree() {
		return ok(GlobalTransPoint.listAllAsJson());
	}

	public static Result getStockCategoryTree() {
		return ok(StockCategory.listAllAsJson());
	}

}
