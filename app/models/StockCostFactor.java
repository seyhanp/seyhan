/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.ModelHelper;
import enums.CalcType;
import enums.CostFactorType;
import enums.EffectType;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class StockCostFactor extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.STOK_MALIYET_FAKTORLERI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public CostFactorType factorType = CostFactorType.Tax;
	public EffectType effectType = EffectType.Percent;
	public CalcType calcType = CalcType.Include;

	@Constraints.Required
	public Double effect;

	public Boolean isActive = Boolean.TRUE;

	public StockCostFactor(String name) {
		super();
		this.name = name;
	}

	public static Map<String, String> options() {
		return ModelHelper.options(RIGHT);
	}

	public static List<StockCostFactor> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static StockCostFactor findById(Integer id) {
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
		return Messages.get("audit.name") + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
