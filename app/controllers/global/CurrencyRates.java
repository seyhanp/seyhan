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
package controllers.global;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OptimisticLockException;

import meta.GridHeader;
import meta.PageExtend;
import models.GlobalCurrency;
import models.GlobalCurrencyRate;
import models.GlobalCurrencyRateDetail;
import models.search.CurrencyRateSearchParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import utils.GlobalCons;
import utils.NumericUtils;
import views.html.globals.currency_rate.form;
import views.html.globals.currency_rate.list;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;

import controllers.Application;
import controllers.admin.Settings;
import enums.ExchangeRateSource;
import enums.Right;
import enums.RightLevel;
import enums.TransType;
import external.NBPExchanges;
import external.TCMBExchanges;
import external.model.ExchangeRate;

/**
 * @author mdpinar
*/
public class CurrencyRates extends Controller {

	private final static Right RIGHT_SCOPE = Right.GNEL_DOVIZ_KURLARI;
	private final static Logger log = LoggerFactory.getLogger(CurrencyRates.class);

	private final static Form<GlobalCurrencyRate> dataForm = form(GlobalCurrencyRate.class);
	private final static Form<CurrencyRateSearchParam> paramForm = form(CurrencyRateSearchParam.class);

	private static Map<String, GlobalCurrencyRateDetail> actualExchangeRatesMap;

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("date"), "7%", "center", "red").sortable("date"));
		headerList.add(new GridHeader(Messages.get("source"), true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<GlobalCurrencyRate> buildPage(CurrencyRateSearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<GlobalCurrencyRate> page = GlobalCurrencyRate.page(searchParam);
		List<GlobalCurrencyRate> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (GlobalCurrencyRate model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, DateUtils.formatDateStandart(model.date));
				dataMap.put(i++, model.source);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<GlobalCurrencyRate>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.global.routes.CurrencyRates.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<CurrencyRateSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(buildPage(filledParamForm.get()), filledParamForm)
		);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<GlobalCurrencyRate> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			GlobalCurrencyRate model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			checkConstraints(filledForm);

			if(filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			try {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(dataForm.fill(model)));
			}
			actualExchangeRatesMap = null;

			flash("success", Messages.get("saved", model.date));
			if (Profiles.chosen().gnel_continuouslyRecording)
				return create();
			else
				return GO_HOME;
		}

	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		GlobalCurrencyRate neu = GlobalCurrencyRate.findByDate(DateUtils.today());
		if (neu == null) {
			neu = new GlobalCurrencyRate();
			neu.init();
		}

		return ok(form.render(dataForm.fill(neu)));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			GlobalCurrencyRate model = GlobalCurrencyRate.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("exchange_rate")));
			} else {
				return ok(form.render(dataForm.fill(model)));
			}
		}
		return GO_HOME;
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Delete);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			GlobalCurrencyRate model = GlobalCurrencyRate.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("exchange_rate")));
			} else {
				try {
					model.delete();
					actualExchangeRatesMap = null;
					flash("success", Messages.get("deleted", model.date));
				} catch (Exception pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", model.date) + " " + pe.getMessage());
					return badRequest(form.render(dataForm.fill(model)));
				}
			}
		}
		return GO_HOME;
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<GlobalCurrencyRate> filledForm) {
		GlobalCurrencyRate model = filledForm.get();

		if (GlobalCurrencyRate.isUsedForElse("date", model.date, model.id)) {
			filledForm.reject("date", Messages.get("not.unique", model.date));
		}

		if (model.details != null || model.details.size() > 0) {
			List<ValidationError> veList = new ArrayList<ValidationError>();

			for (int i = 0; i < model.details.size(); i++) {
				GlobalCurrencyRateDetail detail = model.details.get(i);
				detail.currencyRate = model;
				detail.date = model.date;

				if (detail.buying == 0) {
					veList.add(new ValidationError("xmlDetail", Messages.get("table.zero.value.alert", i+1, Messages.get("buying"))));
				}
				if (detail.selling == 0) {
					veList.add(new ValidationError("xmlDetail", Messages.get("table.zero.value.alert", i+1, Messages.get("selling"))));
				}
			}
			if (veList.size() > 0) {
				filledForm.errors().put("xmlDetail", veList);
			}

		} else {
			filledForm.reject("xmlDetail", Messages.get("table.min.row.alert"));
		}

	}

	public static Map<String, GlobalCurrencyRateDetail> getActualExchangeRatesMap() {
		if (actualExchangeRatesMap == null) {
			GlobalCurrencyRate lastRate = GlobalCurrencyRate.getLastRate();
			actualExchangeRatesMap = new HashMap<String, GlobalCurrencyRateDetail>();

			Set<String> actCurSet = new HashSet<String>();
			List<GlobalCurrency> actCurList = GlobalCurrency.getAll();
			for (GlobalCurrency cur: actCurList) {
				actCurSet.add(cur.code);
			}

			if (lastRate != null && lastRate.details.size() > 0) {
				for (GlobalCurrencyRateDetail crd : lastRate.details) {
					if (actCurSet.contains(crd.code)) {
						actualExchangeRatesMap.put(crd.code, new GlobalCurrencyRateDetail(crd.code, crd.buying, crd.selling));
					}
				}
				actualExchangeRatesMap.put("info", new GlobalCurrencyRateDetail("info", DateUtils.formatReverseDate(lastRate.date) + " - " + lastRate.source));
			} else {
				actualExchangeRatesMap.put("info", new GlobalCurrencyRateDetail("info", Messages.get("not.found", Messages.get("actual_exchange_rates"))));
			}

			actualExchangeRatesMap.put(Profiles.chosen().gnel_excCode, new GlobalCurrencyRateDetail(Profiles.chosen().gnel_excCode, 1d, 1d));
			for (GlobalCurrency cur: actCurList) {
				if (! actualExchangeRatesMap.containsKey(cur.code)) {
					actualExchangeRatesMap.put(cur.code, new GlobalCurrencyRateDetail(cur.code, 1d, 1d));
				}
			}
			
		}

		return actualExchangeRatesMap;
	}

	public static double getExchangeRate(String code, TransType transType) {
		double result = 1d;

		if (getActualExchangeRatesMap() != null) {
			GlobalCurrencyRateDetail actualCRD = actualExchangeRatesMap.get(code);
			if (actualCRD != null) {
				if (transType.equals(TransType.Debt) || transType.equals(TransType.Input)) {
					result = actualCRD.buying;
				} else {
					result = actualCRD.selling;
				}
			}
		}

		return result;
	}

	public static Result pullTCMBExcange() {
		String result = getExchangeRates(ExchangeRateSource.TCMB_Exchange);

		if (Http.Context.current.get() != null) {
			if (result == null) {
				flash("success", Messages.get("pulled", Messages.get("pull.TCMB_Exchange")));
				log.info(Messages.get("pulled", Messages.get("pull.TCMB_Exchange")));
			} else {
				flash("error", result);
				log.error(result);
			}
		}

		return GO_HOME;
	}

	public static Result pullTCMBEffective() {
		String result = getExchangeRates(ExchangeRateSource.TCMB_Effective);

		if (Http.Context.current.get() != null) {

			if (result == null) {
				flash("success", Messages.get("pulled", Messages.get("pull.TCMB_Effective")));
				log.info(Messages.get("pulled", Messages.get("pull.TCMB_Effective")));
			} else {
				flash("error", result);
				log.error(result);
			}
		}

		return GO_HOME;
	}

	public static Result pullNBPExcange() {
		String result = getExchangeRates(ExchangeRateSource.NBP_Exchange);

		if (Http.Context.current.get() != null) {
			if (result == null) {
				flash("success", Messages.get("pulled", Messages.get("pull.NBP_Exchange")));
				log.info(Messages.get("pulled", Messages.get("pull.NBP_Exchange")));
			} else {
				flash("error", result);
				log.error(result);
			}
		}

		return GO_HOME;
	}

	private static String getExchangeRates(ExchangeRateSource source) {
		String error = null;
		List<ExchangeRate> rates = null;
		try {
			switch (source) {
				case NBP_Exchange:
					rates = NBPExchanges.getRates();
					break;
				default:
					rates = TCMBExchanges.getRates();
			}
		} catch (Exception e) {
			error = e.getMessage();
			log.error(error);
			return error;
		}

		if (rates != null && rates.size() > 0) {
			Ebean.beginTransaction();
			try {
				GlobalCurrencyRate old = GlobalCurrencyRate.findByDate(rates.get(0).getDate());
				if (old != null) old.delete();

				GlobalCurrencyRate cr = new GlobalCurrencyRate();
				cr.date = rates.get(0).getDate();

				if (Http.Context.current.get() != null) {
					cr.source = Messages.get("pull." + source.name());
				} else {
					cr.source = GlobalCons.getMessages().getString("pull." + source.name());
				}
				if (! source.equals(ExchangeRateSource.NBP_Exchange)) cr.source = "15:30:00 - " + cr.source;

				cr.details = new ArrayList<GlobalCurrencyRateDetail>();

				Map<String, GlobalCurrency> curMap = Currencies.getCurrencyMap();

				for (int i = 0; i < rates.size(); i++) {
					ExchangeRate rate = rates.get(i);
					GlobalCurrency cur = curMap.get(rate.getCode());
					if (cur != null) {
						GlobalCurrencyRateDetail crd = new GlobalCurrencyRateDetail(cur.code, cur.name);
						crd.date = cr.date;
						if (source.equals(ExchangeRateSource.TCMB_Effective)) {
							crd.buying = new Double(rate.getEffBuying());
							crd.selling = new Double(rate.getEffSelling());
						} else {
							crd.buying = new Double(rate.getExcBuying());
							crd.selling = new Double(rate.getExcSelling());
						}

						if (Settings.getGlobal().exchangeDiffRateForBuying != null && Settings.getGlobal().exchangeDiffRateForBuying.doubleValue() != 0) {
							crd.buying = new Double(crd.buying + ((crd.buying * Settings.getGlobal().exchangeDiffRateForBuying) / 100));
						}
						if (Settings.getGlobal().exchangeDiffRateForBuying != null && Settings.getGlobal().exchangeDiffRateForBuying.doubleValue() != 0) {
							crd.selling = new Double(crd.selling + ((crd.selling * Settings.getGlobal().exchangeDiffRateForSelling) / 100));
						}

						crd.buying = NumericUtils.round(crd.buying, 4);
						crd.selling = NumericUtils.round(crd.selling, 4);

						cr.details.add(crd);
					}
				}
				cr.save();
				Ebean.commitTransaction();

				actualExchangeRatesMap = null;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				Ebean.rollbackTransaction();
			}
		}

		return null;
	}
	
	public static void refreshCurrencies() {
		actualExchangeRatesMap = null;
	}

}
