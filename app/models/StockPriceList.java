/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;

import models.search.NameOnlySearchParam;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import utils.ModelHelper;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.EffectDirection;
import enums.EffectType;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class StockPriceList extends BaseStockExtraFieldsModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.STOK_FIYAT_LISTESI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date startDate;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date endDate;

	public StockCategory category;
	public String providerCode;

	public Boolean isSellPrice = Boolean.TRUE;

	public EffectType effectType = EffectType.Percent;
	public EffectDirection effectDirection = EffectDirection.Increase;

	@Constraints.Required
	public Double effect;

	@Constraints.MaxLength(50)
	public String description;

	public Boolean isActive = Boolean.TRUE;

	public static Page<StockPriceList> page(NameOnlySearchParam searchParam) {
		ExpressionList<StockPriceList> expList = ModelHelper.getExpressionList(RIGHT);

		if (searchParam.name != null && ! searchParam.name.isEmpty()) {
			expList.like("name", searchParam.name + "%");
		}

		return ModelHelper.getPage(RIGHT, expList, "id", searchParam, false);
	}

	public static Map<String, String> options() {
		return ModelHelper.options(RIGHT);
	}

	public static StockPriceList findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id);
	}

	@Override
	public Right getAuditRight() {
		return RIGHT;
	}

	@Override
	public String getAuditDescription() {
		return this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
