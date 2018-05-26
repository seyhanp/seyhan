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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.BankTransSource;
import models.ContactTransSource;
import models.GlobalCurrency;
import models.InvoiceTransSource;
import models.OrderTransSource;
import models.SafeTransSource;
import models.StockTransSource;
import models.WaybillTransSource;
import play.cache.Cache;
import play.i18n.Messages;
import play.mvc.Controller;
import utils.CacheUtils;
import utils.GlobalCons;
import enums.ChqbllSort;
import enums.DocNoIncType;
import enums.ExchangeRatePeriod;
import enums.ExchangeRateSource;
import enums.Module;
import enums.ReceiptNoRnwType;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

@SuppressWarnings("unchecked")
/**
 * @author mdpinar
*/
public class Options extends Controller {

	public static Map<String, String> exchangeSource() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "exchange_source");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(ExchangeRateSource.TCMB_Exchange.toString(), Messages.get("exchange.rate.source.TCMB_Exchange"));
		options.put(ExchangeRateSource.TCMB_Effective.toString(), Messages.get("exchange.rate.source.TCMB_Effective"));
		options.put(ExchangeRateSource.NBP_Exchange.toString(), Messages.get("exchange.rate.source.NBP_Exchange"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> exchangePeriod() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "exchange_period");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(ExchangeRatePeriod.Manual.toString(), Messages.get("exchange.rate.period.Manual"));
		options.put(ExchangeRatePeriod.Once_A_Day.toString(), Messages.get("exchange.rate.period.Once_A_Day"));
		options.put(ExchangeRatePeriod.Hourly.toString(), Messages.get("exchange.rate.period.Hourly"));
		options.put(ExchangeRatePeriod.Every_3_Hours.toString(), Messages.get("exchange.rate.period.Every_3_Hours"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> currencies() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "currencies");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(GlobalCons.defaultExcCode, GlobalCons.defaultExcCode);

		List<GlobalCurrency> curs = GlobalCurrency.getAll();
		for (GlobalCurrency cur: curs) {
			options.put(cur.code, cur.code);
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> transSources(Module module) {
		return transSources(module, true);
	}

	public static Map<String, String> transSources(String moduleName, boolean hasReturn) {
		return transSources(Module.valueOf(moduleName), hasReturn, true);
	}
	
	public static Map<String, String> transSources(Module module, boolean hasReturn) {
		return transSources(module, hasReturn, true);
	}

	public static Map<String, String> transSources(String moduleName, boolean hasReturn, boolean hasOpening) {
		return transSources(Module.valueOf(moduleName), hasReturn, hasOpening);
	}

	public static Map<String, String> transSources(Module module, boolean hasReturn, boolean hasOpening) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, module, hasReturn, "trans_source"+hasOpening);

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		for (Right right : Right.values()) {
			if (right.transType != null && module.equals(right.module)) {
				if (right.isReturn && ! hasReturn) continue;
				if (right.isOpening && ! hasOpening) continue;
				options.put(right.name(), Messages.get(right.key));
			}
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> chqbllTransSources(boolean isPayroll, ChqbllSort sort) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, sort, isPayroll, "cb_trans_source");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();

		if (isPayroll) {
			if (ChqbllSort.Cheque.equals(sort)) {
				options.put(Right.CEK_GIRIS_BORDROSU.name(), Messages.get(Right.CEK_GIRIS_BORDROSU.key));
				options.put(Right.CEK_CIKIS_BORDROSU.name(), Messages.get(Right.CEK_CIKIS_BORDROSU.key));
			} else {
				options.put(Right.SENET_GIRIS_BORDROSU.name(), Messages.get(Right.SENET_GIRIS_BORDROSU.key));
				options.put(Right.SENET_CIKIS_BORDROSU.name(), Messages.get(Right.SENET_CIKIS_BORDROSU.key));
			}
		} else {
			if (ChqbllSort.Cheque.equals(sort)) {
				options.put(Right.CEK_MUSTERI_HAREKETLERI.name(), Messages.get(Right.CEK_MUSTERI_HAREKETLERI.key));
				options.put(Right.CEK_FIRMA_HAREKETLERI.name(), Messages.get(Right.CEK_FIRMA_HAREKETLERI.key));
			} else {
				options.put(Right.SENET_MUSTERI_HAREKETLERI.name(), Messages.get(Right.SENET_MUSTERI_HAREKETLERI.key));
				options.put(Right.SENET_FIRMA_HAREKETLERI.name(), Messages.get(Right.SENET_FIRMA_HAREKETLERI.key));
			}
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> chqbllPartialSources(ChqbllSort sort) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, sort, "cb_partial_source");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		
		if (ChqbllSort.Cheque.equals(sort)) {
			options.put(Right.CEK_PARCALI_ODEME.name(), Messages.get(Right.CEK_PARCALI_ODEME.key));
			options.put(Right.CEK_PARCALI_TAHSILAT.name(), Messages.get(Right.CEK_PARCALI_TAHSILAT.key));
		} else {
			options.put(Right.SENET_PARCALI_ODEME.name(), Messages.get(Right.SENET_PARCALI_ODEME.key));
			options.put(Right.SENET_PARCALI_TAHSILAT.name(), Messages.get(Right.SENET_PARCALI_TAHSILAT.key));
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> transSources(Right right) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, right, "trans_source");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();

		switch (right.module) {
			case contact: {
				return ContactTransSource.options(right);
			}
			case stock: {
				return StockTransSource.options(right);
			}
			case order: {
				return OrderTransSource.options(right);
			}
			case waybill: {
				return WaybillTransSource.options(right);
			}
			case invoice: {
				return InvoiceTransSource.options(right);
			}
			case safe: {
				return SafeTransSource.options(right);
			}
			case bank: {
				return BankTransSource.options(right);
			}
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> docNoIncType() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "doc_no_inc");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		for (DocNoIncType enm : DocNoIncType.values()) {
			options.put(enm.toString(), Messages.get(enm.key));
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> receiptNoRnwType() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "receipt_no_rnw");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		for (ReceiptNoRnwType enm : ReceiptNoRnwType.values()) {
			options.put(enm.toString(), Messages.get(enm.key));
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> stockPrices() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "stock_prices");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put("sell_price", Messages.get("sell_price"));
		options.put("buy_price", Messages.get("buy_price"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> yesno() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "yesno");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Boolean.TRUE.toString(), Messages.get("yes"));
		options.put(Boolean.FALSE.toString(), Messages.get("no"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> printerLocations() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "printer_locations");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Boolean.TRUE.toString(), Messages.get("local"));
		options.put(Boolean.FALSE.toString(), Messages.get("remote"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> fontTypes() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "font_types");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Boolean.TRUE.toString(), Messages.get("font.compessed"));
		options.put(Boolean.FALSE.toString(), Messages.get("font.normal"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> taxStatus() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "tax_status");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Boolean.FALSE.toString(), Messages.get("included"));
		options.put(Boolean.TRUE.toString(), Messages.get("excluded"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}
	
	public static Map<String, String> priceChoice() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "price_choice");
		
		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;
		
		options = new LinkedHashMap<String, String>();
		options.put(Boolean.TRUE.toString(), Messages.get("sell_price"));
		options.put(Boolean.FALSE.toString(), Messages.get("buy_price"));
		
		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);
		
		return options;
	}

	public static Map<String, String> instantRights() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "instant_rights");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put("special.none", Messages.get("instant_rights.special.none"));
		options.put("special.guest", Messages.get("instant_rights.special.guest"));
		options.put("special.admin", Messages.get("instant_rights.special.admin"));

		options.put("special.allow.read", Messages.get("instant_rights.special.allow", Messages.get("instant_rights.read")));
		options.put("special.allow.insert", Messages.get("instant_rights.special.allow", Messages.get("instant_rights.insert")));
		options.put("special.allow.update", Messages.get("instant_rights.special.allow", Messages.get("instant_rights.update")));
		options.put("special.allow.delete", Messages.get("instant_rights.special.allow", Messages.get("instant_rights.delete")));

		options.put("special.allow.report", Messages.get("instant_rights.special.allow", Messages.get("instant_rights.report")));
		options.put("special.deny.report", Messages.get("instant_rights.special.deny", Messages.get("instant_rights.report")));

		for (Module module: Module.values()) {
			if (module.equals(Module.admin) || module.equals(Module.no)) continue;
			options.put("module.allow." + module.name(), Messages.get("instant_rights.module.allow", Messages.get(module.name())));
			options.put("module.deny." + module.name(), Messages.get("instant_rights.module.deny", Messages.get(module.name())));
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> basicRightLevels() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "basic_rights");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(RightLevel.Disable.name(), Messages.get(RightLevel.Disable.key));
		options.put(RightLevel.Enable.name(), Messages.get(RightLevel.Enable.key));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> crudRightLevels() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "crud_rights");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(RightLevel.Disable.name(), Messages.get(RightLevel.Disable.key));
		options.put(RightLevel.Enable.name(), Messages.get("enum.Read"));
		options.put(RightLevel.Insert.name(), Messages.get(RightLevel.Insert.key));
		options.put(RightLevel.Update.name(), Messages.get(RightLevel.Update.key));
		options.put(RightLevel.Delete.name(), Messages.get(RightLevel.Delete.key));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> withholdingRates() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "withholding_rates");

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put("0", "0");
		options.put("0.2", "2/10");
		options.put("0.5", "5/10");
		options.put("0.7", "7/10");
		options.put("0.9", "9/10");

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static List<ReportUnit> getGraphicalReportUnits() {
		List<ReportUnit> options = new ArrayList<ReportUnit>();
		
		options.add(ReportUnit.Html);
		options.add(ReportUnit.Pdf);

		return options;
	}
	
}
