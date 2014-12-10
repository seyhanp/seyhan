/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.ModelHelper;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class SaleSeller extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.SATS_SATICI_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public Double primRate = 0d;
	public Boolean isActive = Boolean.TRUE;

	public SaleSeller(String name) {
		super();
		this.name = name;
	}

	public static Map<String, String> options() {
		return ModelHelper.options(RIGHT);
	}

	public static Map<Integer, SaleSeller> getModelMap() {
		return ModelHelper.getModelIdMap(RIGHT);
	}

	public static Map<String, String> crossOptions() {
		List<SaleSeller> modelList = ModelHelper.getModelList(RIGHT);
		
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		for(SaleSeller model: modelList) {
			options.put(model.id.toString(), model.name);
			options.put(model.name, model.id.toString());
		}
		return options;
	}

	public static List<SaleSeller> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static SaleSeller findById(Integer id) {
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
