/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.AdminUserRight;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class UserRights {

	public static List<AdminUserRight> definedRights() {
		List<AdminUserRight> rights = new ArrayList<AdminUserRight>();
		for (Right right : Right.values()) {
			if (! right.isAdminMenu && ! right.isShadow && ! right.isHeader) rights.add(new AdminUserRight(right));
		}

		return rights;
	}

	public static List<AdminUserRight> definedRights(Map<String, String> data) {
		List<AdminUserRight> rights = definedRights();

		for (String f : data.keySet()) {
			if (f.startsWith("rights[")) { 
				int index = new Integer(f.replaceAll("\\D", ""));

				AdminUserRight ur = rights.get(index);
				ur.rightLevel = RightLevel.valueOf(data.get(f));

				rights.set(index, ur);
			}
		}

		return rights;
	}


}
