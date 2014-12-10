/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package utils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import menus.MenuManager;
import meta.RightBind;
import play.api.mvc.Call;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.libs.Json;
import views.html.tools.templates.dropdown_menu;
import controllers.global.Profiles;
import documents.Field;
import enums.CacheKeys;
import enums.ChqbllSort;

/**
 * @author mdpinar
*/
public class TemplateHelper {

	public static Call getSaveActionForChqbllPartial(ChqbllSort sort, RightBind rightBind) {
		if (enums.ChqbllSort.Cheque.equals(sort)) {
			return controllers.chqbll.routes.PartialsForCheque.save(rightBind);
		} else {
			return controllers.chqbll.routes.PartialsForBill.save(rightBind);
		}
	}

	public static Call getRemoveActionForChqbllPartial(ChqbllSort sort, Integer id, RightBind rightBind) {
		if (enums.ChqbllSort.Cheque.equals(sort)) {
			return controllers.chqbll.routes.PartialsForCheque.remove(id, rightBind);
		} else {
			return controllers.chqbll.routes.PartialsForBill.remove(id, rightBind);
		}
	}

	public static Call getSaveActionForChqbllPayroll(ChqbllSort sort, RightBind rightBind) {
		if (enums.ChqbllSort.Cheque.equals(sort)) {
			return controllers.chqbll.routes.PayrollsForCheque.save(rightBind);
		} else {
			return controllers.chqbll.routes.PayrollsForBill.save(rightBind);
		}
	}

	public static Call getRemoveActionForChqbllPayroll(ChqbllSort sort, Integer id, RightBind rightBind) {
		if (enums.ChqbllSort.Cheque.equals(sort)) {
			return controllers.chqbll.routes.PayrollsForCheque.remove(id, rightBind);
		} else {
			return controllers.chqbll.routes.PayrollsForBill.remove(id, rightBind);
		}
	}

	public static Call getSaveActionForChqbllTrans(ChqbllSort sort, RightBind rightBind) {
		if (enums.ChqbllSort.Cheque.equals(sort)) {
			return controllers.chqbll.routes.TransForCheque.save(rightBind);
		} else {
			return controllers.chqbll.routes.TransForBill.save(rightBind);
		}
	}

	public static Call getRemoveActionForChqbllTrans(ChqbllSort sort, Integer id, RightBind rightBind) {
		if (enums.ChqbllSort.Cheque.equals(sort)) {
			return controllers.chqbll.routes.TransForCheque.remove(id, rightBind);
		} else {
			return controllers.chqbll.routes.TransForBill.remove(id, rightBind);
		}
	}

	public static String getTransListTimingAlertMessage() {
		switch (Profiles.chosen().gnel_listingType) {
			case Daily: return Messages.get("trans_list.timing.restriction.alert", DateUtils.today("dd/MM/yyyy"));
			case Monthly: return Messages.get("trans_list.timing.restriction.alert", DateUtils.getYearMonth(new Date()));
		}

		return "";
	}

	public static String convertMessage(ValidationError ve) {
		return Messages.get(ve.message(), ve.arguments());
	}

	public static String getMainMenuTree() {
		if (! CacheUtils.isLoggedIn()) return null;

		String value = CacheUtils.get(CacheKeys.MENU);

		if (value == null) {
			value = dropdown_menu.render(MenuManager.getMenuTree()).body();
			CacheUtils.set(CacheKeys.MENU, value);
		}

		return value;
	}

	public static Map<String, String> buildOptions(String group, List<Field> fieldList) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		result.put("/start_" + group, (
				group.equals("tables")
					? Messages.get(group)
					: Messages.get("infosof", Messages.get(group))
		)); //start group
		for (Field field : fieldList) {
			result.put(Json.toJson(field).toString(), getFieldLabel(field.labelKey));
		}
		result.put("/end_" + group, null); //end group
		
		return result;
	}
	
	public static String getFieldLabel(String key) {
		String result = key;

		int slashIndex = key.indexOf("/");
		if (slashIndex > 0) {
			String left = key.substring(0, slashIndex);
			String right = key.substring(slashIndex+1);
			String msg = Messages.get(left, Messages.get(right));
			if (msg.indexOf(" ") > 0) {
				result = msg;
			} else {
				result = msg + " " + right;
			}
		} else {
			result = Messages.get(key);
		}
		
		return result;
	}
	
}
