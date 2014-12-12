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
