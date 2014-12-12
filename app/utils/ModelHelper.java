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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.AbstractBaseTrans;
import models.Bank;
import models.BankExpense;
import models.BankTrans;
import models.BankTransSource;
import models.BaseModel;
import models.ChqbllPayroll;
import models.ChqbllPayrollSource;
import models.ChqbllTrans;
import models.ChqbllType;
import models.Contact;
import models.ContactExtraFields;
import models.ContactCategory;
import models.ContactTrans;
import models.ContactTransSource;
import models.GlobalProfile;
import models.InvoiceTrans;
import models.InvoiceTransSource;
import models.OrderTrans;
import models.OrderTransSource;
import models.Safe;
import models.SafeExpense;
import models.SafeTrans;
import models.SafeTransSource;
import models.SaleCampaign;
import models.SaleSeller;
import models.Stock;
import models.StockCostFactor;
import models.StockCosting;
import models.StockDepot;
import models.StockExtraFields;
import models.StockPriceList;
import models.StockPriceUpdate;
import models.StockTrans;
import models.StockTransSource;
import models.StockUnit;
import models.WaybillTrans;
import models.WaybillTransSource;
import models.search.AbstractSearchParam;
import models.temporal.Pair;
import play.db.ebean.Model;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;

import controllers.global.Profiles;
import enums.CacheKeys;
import enums.Module;
import enums.Right;
import enums.TransListingType;

@SuppressWarnings({"unchecked", "rawtypes"})
/**
 * @author mdpinar
*/
public class ModelHelper {

	public static final String CHBL_TYPE = "chbl.type";
	public static final String CHBL_TRANS = "chbl.trans";
	public static final String CHBL_PAYROLL = "chbl.payroll";
	public static final String CHBL_OPENING = "chbl.opening";
	public static final String CHBL_PAYROLL_SOURCE = "chbl.payroll.source";

	public static int getRowCount(Right right) {
		return finderMap.get(right).findRowCount();
	}

	public static <T extends BaseModel> T findById(Right right, Integer id, String... fetchFieldNames) {
		return (T) findById(false, right.name(), finderMap.get(right), id, fetchFieldNames);
	}

	public static <T extends BaseModel> T findById(Module module, Integer id, String... fetchFieldNames) {
		return (T) findById(false, module.name(), finderForTransMap.get(module), id, fetchFieldNames);
	}

	public static <T extends BaseModel> T findById(String name, Integer id, String... fetchFieldNames) {
		return (T) findById(false, name, finderByNameMap.get(name), id, fetchFieldNames);
	}

	public static <T extends BaseModel> T findByName(Right right, String name) {
		return (T) findByName(right.name(), finderMap.get(right), name);
	}

	private static <T extends BaseModel> T findById(boolean willBeCached, String key, Model.Finder<Integer, T> finder, Integer id, String... fetchFieldNames) {
		T result = null;
		if (willBeCached) {
			result = CacheUtils.get(true, key + CacheKeys.BY_ID.value + id);
		}

		if (result == null) {
			
			Query<T> query = finder.where().eq("id", id).query();
			
			if (fetchFieldNames != null && fetchFieldNames.length > 0) {
				for (String fieldName : fetchFieldNames) {
					query = query.fetch(fieldName);
				}
			}
	
			result = query.findUnique();
			
			if (willBeCached) CacheUtils.set(true, key + CacheKeys.BY_ID.value + id, result);
		}
		
		return result;
	}

	private static <T extends BaseModel> T findByName(String key, Model.Finder<Integer, T> finder, String name) {
		T result = CacheUtils.get(true, key + CacheKeys.BY_NAME.value + name);

		if (result == null) {
			result = finder.where()
							.eq("workspace", CacheUtils.getWorkspaceId())
							.eq("name", name)
					.findUnique();
			
			CacheUtils.set(true, key + CacheKeys.BY_NAME.value + name, result);
		}
		
		return result;
	}

	public static <T extends BaseModel> List<T> page(Right right, String fieldName) {
		return page(right, fieldName, null);
	}
	
	public static <T extends BaseModel> List<T> page(Right right, String fieldName, Expression exp) {
		ExpressionList<T> elList = finderMap.get(right)
											.where()
												.eq("workspace", CacheUtils.getWorkspaceId());
		if (exp != null) elList.add(exp);

		Pair sortInfo = CookieUtils.getSortInfo(right, fieldName);
		return elList
					.orderBy(sortInfo.key + " " + sortInfo.value)
				.findList();
	}

	public static <T extends Model> boolean isUsedForElse(Right right, String field, Object value, Integer id) {
		return isUsedForElse(right, field, value, id, null);
	}

	public static <T extends Model> boolean isUsedForElse(Right right, String field, Object value, Integer id, Expression exp) {
		ExpressionList<T> el = finderMap.get(right)
									.where()
										.eq("workspace", CacheUtils.getWorkspaceId())
										.eq(field, value);
		if (id != null) el.ne("id", id);
		if (exp != null) el.add(exp);

		return el.findUnique() != null;
	}

	public static <T extends BaseModel> Map<String, String> options(Right right) {
		return expOptions(right, null);
	}

	public static <T extends BaseModel> Map<String, String> expOptions(Right right, Expression exp) {
		LinkedHashMap<String, String> result = CacheUtils.get(true, right.name() + CacheKeys.OPTIONS.value + (exp != null ? exp.toString() : ""));

		if (result == null) {
			result = new LinkedHashMap<String, String>();

			ExpressionList<T> elList = finderMap.get(right)
											.where()
												.eq("workspace", CacheUtils.getWorkspaceId())
												.eq("isActive", Boolean.TRUE);
			if (exp != null) elList.add(exp);
	
			List<T> modelList = elList
									.orderBy("name")
								.findList();
			for(T model: modelList) {
				result.put(model.id.toString(), model.toString());
			}

			CacheUtils.set(true, right.name() + CacheKeys.OPTIONS.value + (exp != null ? exp.toString() : ""), result);
		}

		return result;
	}

	public static <T extends BaseModel> List<T> getModelList(Right right) {
		List<T> result = CacheUtils.get(true, right.name() + CacheKeys.LIST_ALL.value);

		if (result == null) {
			result = finderMap.get(right)
								.where()
									.eq("workspace", CacheUtils.getWorkspaceId())
									.eq("isActive", Boolean.TRUE)
								.orderBy("name")
							.findList();
			CacheUtils.set(true, right.name() + CacheKeys.LIST_ALL.value, result);
		}

		return result;
	}

	public static <T extends BaseModel> Map<Integer, T> getModelIdMap(Right right) {
		Map<Integer, T> result = CacheUtils.get(true, right.name() + CacheKeys.ID_MAP.value);

		if (result == null) {
			result = new HashMap<Integer, T>();

			List<T> modelList = getModelList(right);
			for (T model: modelList) {
				result.put(model.id, model);
			}

			CacheUtils.set(true, right.name() + CacheKeys.ID_MAP.value, result);
		}
		
		return result;
	}

	public static <T extends BaseModel> Map<String, T> getModelNameMap(Right right) {
		Map<String, T> result = CacheUtils.get(true, right.name() + CacheKeys.NAME_MAP.value);

		if (result == null) {
			result = new HashMap<String, T>();

			List<T> modelList = getModelList(right);
			for (T model: modelList) {
				result.put(model.toString(), model);
			}

			CacheUtils.set(true, right.name() + CacheKeys.NAME_MAP.value, result);
		}
		
		return result;
	}

	public static <T extends BaseModel> List<String> getNameList(Right right) {
		List<String> result = CacheUtils.get(true, right.name() + CacheKeys.NAME_LIST.value);

		if (result == null) {
			result = new ArrayList<String>();

			List<T> modelList = getModelList(right);
			for (T model: modelList) {
				result.add(model.toString());
			}

			CacheUtils.set(true, right.name() + CacheKeys.NAME_LIST.value, result);
		}
		
		return result;
	}

	public static <T extends BaseModel> Map<String, String> options(Right right, Right suitableRight) {
		LinkedHashMap<String, String> result = CacheUtils.get(true, right.name() + CacheKeys.OPTIONS.value + (suitableRight != null ? suitableRight.name() : ""));

		if (result == null) {
			result = new LinkedHashMap<String, String>();

			ExpressionList<T> elList = finderMap.get(right)
											.where()
												.eq("workspace", CacheUtils.getWorkspaceId())
												.eq("isActive", Boolean.TRUE);
			if (suitableRight != null) {
				elList.or(
					Expr.isNull("suitableRight"),
					Expr.eq("suitableRight", suitableRight)
				);
			}
	
			List<T> modelList = elList.orderBy("name").findList();
			for(T model: modelList) {
				result.put(model.id.toString(), model.toString());
			}
	
			CacheUtils.set(true, right.name() + CacheKeys.OPTIONS.value + (suitableRight != null ? suitableRight.name() : ""), result);
		}
	
		return result;
	}

	public static <T extends BaseModel> Page<T> getPage(Right right, ExpressionList<T> expList, AbstractSearchParam searchParam, String... fetchFieldNames) {
		return getPage(right, expList, null, searchParam, true, fetchFieldNames);
	}

	public static <T extends BaseModel> Page<T> getPage(Right right, ExpressionList<T> expList, AbstractSearchParam searchParam, boolean isTransTable, String... fetchFieldNames) {
		return getPage(right, expList, null, searchParam, isTransTable, fetchFieldNames);
	}

	public static <T extends BaseModel> Page<T> getPage(Right right, ExpressionList<T> expList, String orderField, AbstractSearchParam searchParam, boolean isTransTable, String... fetchFieldNames) {
		Pair sortInfo = null;
		int rowNumber = Profiles.chosen().gnel_pageRowNumber;

		expList.eq("workspace", CacheUtils.getWorkspaceId());

		if (isTransTable) {
			if (searchParam.startDate == null && searchParam.endDate == null) {
				TransListingType listingType = Profiles.chosen().gnel_listingType;
				switch (listingType) {
					case Daily: {
						rowNumber = 100;
						expList.eq("transDate", DateUtils.today());
						break;
					}
					case Monthly: {
						rowNumber = 100;
						expList.eq("transMonth", DateUtils.getYearMonth(DateUtils.today()));
						break;
					}
				}
			}
			sortInfo = CookieUtils.getSortInfo(right, (orderField != null ? orderField : "transDate"), "desc");
		} else {
			sortInfo = CookieUtils.getSortInfo(right, (orderField != null ? orderField : "name"));
		}

		Query<T> query = expList.orderBy(sortInfo.key + " " + sortInfo.value);

		if (fetchFieldNames != null && fetchFieldNames.length > 0) {
			for (String fieldName : fetchFieldNames) {
				query = query.fetch(fieldName);
			}
		}
		
		return query.findPagingList(rowNumber)
					.setFetchAhead(false)
				.getPage(searchParam.pageIndex);
	}

	public static <T extends BaseModel> Page<T> getPage(Right right, ExpressionList<T> expList, int pageIndex, String fetchFieldName) {
		Pair sortInfo = CookieUtils.getSortInfo(right, "name");

		expList.eq("workspace", CacheUtils.getWorkspaceId());
		
		return expList
				.orderBy(sortInfo.key + " " + sortInfo.value)
					.fetch("category")
				.findPagingList(Profiles.chosen().gnel_pageRowNumber)
				.setFetchAhead(false)
			.getPage(pageIndex);
	}

	public static <T extends BaseModel> ExpressionList<T> getExpressionList(Right right) {
		return finderMap.get(right).where();
	}

	public static <T extends BaseModel> ExpressionList<T> getExpressionList(Module module) {
		return finderForTransMap.get(module).where();
	}

	public static <T extends BaseModel> ExpressionList<T> getExpressionList(String name) {
		return finderByNameMap.get(name).where();
	}

	public static <T extends AbstractBaseTrans> T findByRefIdAndRight(Module module, Integer id, Right refRight) {
		return (T) findByRefIdAndRight(finderForTransMap.get(module), id, refRight);
	}

	public static <T extends AbstractBaseTrans> T findByRefIdAndRight(String name, Integer id, Right refRight) {
		return (T) findByRefIdAndRight(finderByNameMap.get(name), id, refRight);
	}

	private static <T extends AbstractBaseTrans> T findByRefIdAndRight(Model.Finder<Integer, T> finder, Integer id, Right refRight) {
		return finder
					.where()
						.eq("workspace", CacheUtils.getWorkspaceId())
						.eq("refId", id)
						.eq("right", refRight)
					.findUnique();
	}

	private static Map<Right, Model.Finder> finderMap;
	private static Map<Module, Model.Finder> finderForTransMap;
	private static Map<String, Model.Finder> finderByNameMap;

	static {
		finderMap = new HashMap<Right, Model.Finder>();
		finderForTransMap = new HashMap<Module, Model.Finder>();
		finderByNameMap = new HashMap<String, Model.Finder>();

		finderMap.put(Right.CARI_TANITIMI, new Model.Finder<Integer, Contact>(Integer.class, Contact.class));
		finderMap.put(Right.CARI_EKSTRA_ALANLAR, new Model.Finder<Integer, ContactExtraFields>(Integer.class, ContactExtraFields.class));
		finderMap.put(Right.CARI_KATEGORI_TANITIMI, new Model.Finder<Integer, ContactCategory>(Integer.class, ContactCategory.class));
		finderMap.put(Right.CARI_ISLEM_KAYNAKLARI, new Model.Finder<Integer, ContactTransSource>(Integer.class, ContactTransSource.class));
		finderForTransMap.put(Module.contact, new Model.Finder<Integer, ContactTrans>(Integer.class, ContactTrans.class));

		finderMap.put(Right.KASA_TANITIMI, new Model.Finder<Integer, Safe>(Integer.class, Safe.class));
		finderMap.put(Right.KASA_GIDER_TANITIMI, new Model.Finder<Integer, SafeExpense>(Integer.class, SafeExpense.class));
		finderMap.put(Right.KASA_ISLEM_KAYNAKLARI, new Model.Finder<Integer, SafeTransSource>(Integer.class, SafeTransSource.class));
		finderForTransMap.put(Module.safe, new Model.Finder<Integer, SafeTrans>(Integer.class, SafeTrans.class));

		finderMap.put(Right.BANK_HESAP_TANITIMI, new Model.Finder<Integer, Bank>(Integer.class, Bank.class));
		finderMap.put(Right.BANK_MASRAF_TANITIMI, new Model.Finder<Integer, BankExpense>(Integer.class, BankExpense.class));
		finderMap.put(Right.BANK_ISLEM_KAYNAKLARI, new Model.Finder<Integer, BankTransSource>(Integer.class, BankTransSource.class));
		finderForTransMap.put(Module.bank, new Model.Finder<Integer, BankTrans>(Integer.class, BankTrans.class));

		finderByNameMap.put(CHBL_TYPE, new Model.Finder<Integer, ChqbllType>(Integer.class, ChqbllType.class));
		finderByNameMap.put(CHBL_TRANS, new Model.Finder<Integer, ChqbllTrans>(Integer.class, ChqbllTrans.class));
		finderByNameMap.put(CHBL_PAYROLL, new Model.Finder<Integer, ChqbllPayroll>(Integer.class, ChqbllPayroll.class));
		finderByNameMap.put(CHBL_PAYROLL_SOURCE, new Model.Finder<Integer, ChqbllPayrollSource>(Integer.class, ChqbllPayrollSource.class));

		finderMap.put(Right.SPRS_FIS_KAYNAKLARI, new Model.Finder<Integer, OrderTransSource>(Integer.class, OrderTransSource.class));
		finderForTransMap.put(Module.order, new Model.Finder<Integer, OrderTrans>(Integer.class, OrderTrans.class));

		finderMap.put(Right.IRSL_IRSALIYE_KAYNAKLARI, new Model.Finder<Integer, WaybillTransSource>(Integer.class, WaybillTransSource.class));
		finderForTransMap.put(Module.waybill, new Model.Finder<Integer, WaybillTrans>(Integer.class, WaybillTrans.class));

		finderMap.put(Right.FATR_FATURA_KAYNAKLARI, new Model.Finder<Integer, InvoiceTransSource>(Integer.class, InvoiceTransSource.class));
		finderForTransMap.put(Module.invoice, new Model.Finder<Integer, InvoiceTrans>(Integer.class, InvoiceTrans.class));

		finderMap.put(Right.SATS_SATICI_TANITIMI, new Model.Finder<Integer, SaleSeller>(Integer.class, SaleSeller.class));
		finderMap.put(Right.SATS_KAMPANYA_TANITIMI, new Model.Finder<Integer, SaleCampaign>(Integer.class, SaleCampaign.class));
		finderMap.put(Right.GNEL_PROFIL_TANITIMI, new Model.Finder<Integer, GlobalProfile>(Integer.class, GlobalProfile.class));

		finderMap.put(Right.STOK_TANITIMI, new Model.Finder<Integer, Stock>(Integer.class, Stock.class));
		finderMap.put(Right.STOK_EKSTRA_ALANLAR, new Model.Finder<Integer, StockExtraFields>(Integer.class, StockExtraFields.class));
		finderMap.put(Right.STOK_MALIYET_FAKTORLERI, new Model.Finder<Integer, StockCostFactor>(Integer.class, StockCostFactor.class));
		finderMap.put(Right.STOK_MALIYET_HESAPLAMALARI, new Model.Finder<Integer, StockCosting>(Integer.class, StockCosting.class));
		finderMap.put(Right.STOK_DEPO_TANITIMI, new Model.Finder<Integer, StockDepot>(Integer.class, StockDepot.class));
		finderMap.put(Right.STOK_FIYAT_GUNCELLEME, new Model.Finder<Integer, StockPriceUpdate>(Integer.class, StockPriceUpdate.class));
		finderMap.put(Right.STOK_FIS_KAYNAKLARI, new Model.Finder<Integer, StockTransSource>(Integer.class, StockTransSource.class));
		finderMap.put(Right.STOK_BIRIM_TANITIMI, new Model.Finder<Integer, StockUnit>(Integer.class, StockUnit.class));
		finderMap.put(Right.STOK_FIYAT_LISTESI, new Model.Finder<Integer, StockPriceList>(Integer.class, StockPriceList.class));
		finderForTransMap.put(Module.stock, new Model.Finder<Integer, StockTrans>(Integer.class, StockTrans.class));
	}
	
}
