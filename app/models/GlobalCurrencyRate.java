/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import models.search.CurrencyRateSearchParam;
import models.temporal.Pair;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;
import utils.CookieUtils;
import utils.DateUtils;
import utils.GlobalCons;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import controllers.global.Profiles;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class GlobalCurrencyRate extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Column(name = "_date")
	@DateTime(pattern = "dd/MM/yyyy")
	public Date date = new Date();

	@Constraints.MaxLength(100)
	public String source;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="currencyRate", orphanRemoval = true)
	public List<GlobalCurrencyRateDetail> details;

	@Version
	public Integer version;

	private static Model.Finder<Integer, GlobalCurrencyRate> find = new Model.Finder<Integer, GlobalCurrencyRate>(Integer.class, GlobalCurrencyRate.class);

	public GlobalCurrencyRate() {
		this.source = Messages.get("manual.entry", DateUtils.today("HH:mm:ss"));
	}

	public static Page<GlobalCurrencyRate> page(CurrencyRateSearchParam searchParam) {
		ExpressionList<GlobalCurrencyRate> expList = find.select("id, date").where();

		if (searchParam.date != null) {
			expList.eq("date", searchParam.date);
		}

		Pair sortInfo = CookieUtils.getSortInfo(Right.GNEL_DOVIZ_KURLARI, "date", "desc");

		Page<GlobalCurrencyRate> page = expList.orderBy(sortInfo.key + " " + sortInfo.value)
												.findPagingList(Profiles.chosen().gnel_pageRowNumber)
												.setFetchAhead(false)
											.getPage(searchParam.pageIndex);

		return page;
	}

	public void init() {
		List<GlobalCurrency> currencies = GlobalCurrency.getAll();

		details = new ArrayList<GlobalCurrencyRateDetail>();
		for (GlobalCurrency cur : currencies) {
			details.add(new GlobalCurrencyRateDetail(cur.code, cur.name));
		}
	}

	public static GlobalCurrencyRate findById(Integer id) {
		return find.fetch("details").where().eq("id", id).findUnique();
	}

	public static GlobalCurrencyRate findByDate(Date date) {
		if (GlobalCons.dbVendor.equals("postgresql") || GlobalCons.dbVendor.equals("oracle")) {
			return find.fetch("details").where().eq("to_char(date, 'yyyy-mm-dd')", DateUtils.formatReverseDate(date)).findUnique();
		} else {
			return find.fetch("details").where().eq("date", DateUtils.formatReverseDate(date)).findUnique();
		}
		
	}
	
	public static GlobalCurrencyRate findNearBy(Date date) {
		GlobalCurrencyRate gcr = find.fetch("details").
										where()
											.le("date", date)
										.order("id desc")
										.setMaxRows(1)
									.findUnique();
		if (gcr == null) gcr = getLastRate();
		return gcr;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<GlobalCurrencyRate> el = find.where().eq(field, value);
		if (id != null) el.ne("id", id);

		return el.findUnique() != null;
	}

	public static GlobalCurrencyRate getLastRate() {
		return find.fetch("details").order("id desc").setMaxRows(1).findUnique();
	}

}
