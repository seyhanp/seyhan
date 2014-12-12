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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.AdminUser;
import models.AdminWorkspace;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import controllers.Application;
import controllers.admin.Workspaces;
import enums.CacheKeys;
import enums.Module;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class AuthManager {

	private final static Logger log = LoggerFactory.getLogger(AuthManager.class);

	public static String simpleAuthenticate(String username, String password) {
		AdminUser user = AdminUser.findByUsername(username);

		if (user != null) {
			boolean result = user.passwordHash.equals(AuthManager.md5Hash(password));
			if (result) return "ok";
		}

		return Messages.get("invalid.user.or.password");
	}

	public static String authenticate(String username, String password) {
		AdminUser user = AdminUser.findByUsername(username);

		if (user != null) {
			boolean result = user.passwordHash.equals(AuthManager.md5Hash(password));
			if (result) {
				int wsCount = setUserRights(user);
				if (user.id.intValue() == 1 || user.isAdmin || wsCount > 0) {
					if (user.id.intValue() == 1 && (user.workspace == null || user.workspace.intValue() == 0)) {
						log.info("super user has no workspace, will used first one!");
						CacheUtils.setWorkspace(1);
					}
					return null;
				} else {
					return Messages.get("any.not.found.workspace");
				}
			}
		}

		return Messages.get("invalid.user.or.password");
	}

	public static String md5Hash(String data) {
		if (data != null && ! data.isEmpty()) {
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) { }

			byte[] out = md.digest(data.getBytes());
			return new String(Hex.encodeHex(out));
		}

		return null;
	}

	public static boolean hasSavePrivilege(Right right) {
		return hasPrivilege(right, RightLevel.Insert);
	}

	public static boolean hasSavePrivilege(Right right, String id) {
		if (id == null || id.isEmpty())
			return hasPrivilege(right, RightLevel.Insert);
		else
			return hasPrivilege(right, RightLevel.Update);
	}

	public static boolean hasPrivilege(Module module, RightLevel level) {
		Set<Right> rightSet = Right.getModuleRightSet(module);
		for (Right right : rightSet) {
			boolean hasPrivilege = hasPrivilege(right, level);
			if (hasPrivilege) return true;
		}

		return false;
	}

	public static boolean hasPrivilege(Right right, RightLevel level) {
		if (CacheUtils.getUser() == null) return false;

		if (CacheUtils.isSuperUser()) {
			return (right.isAdminMenu || CacheUtils.getWorkspaceId() != null);
		} else if (CacheUtils.isAdminUser()) {
			return (! right.isAdminMenu && CacheUtils.getWorkspaceId() != null);
		} else {
			Map<Integer, Map<String, RightLevel>> rightMap = CacheUtils.get(CacheKeys.RIGHTS);
			if (rightMap != null) {
				Map<String, RightLevel> subMap = rightMap.get(CacheUtils.getWorkspaceId());
				if (subMap != null) {
					RightLevel rl = subMap.get(right.name());
					return (rl != null && rl.ordinal() >= level.ordinal());
				}
			}
		}

		return false;
	}

	public static Result hasProblem(Right right, RightLevel level, Right[] acceptedRights) {
		Result result = hasProblem(right, level);
		if (result == null) {
			if (! Arrays.asList(acceptedRights).contains(right)) {
				return Application.getBadRequestResult();
			} else {
				return null;
			}
		} else {
			return result;
		}
	}

	public static Result hasProblem(Right right, RightLevel level) {
		if (CacheUtils.getUser() == null) return Application.login();

		if (CacheUtils.isSpecialUser()) {
			if (CacheUtils.getWorkspaceName() == null) {
				List<AdminWorkspace> wsList = Workspaces.getAll();
				if (wsList.size() > 0) {
					return Application.workspaces(Messages.get("firstly.select", Messages.get("workspace")));
				} else if (CacheUtils.isSuperUser()) {
					return Workspaces.list(Messages.get("any.not.found.firstly.do", Messages.get("workspace")));
				}
			}
			return null;
		} else {
			Map<Integer, Map<String, RightLevel>> rightMap = CacheUtils.get(CacheKeys.RIGHTS);
			if (rightMap != null) {
				Map<String, RightLevel> subMap = rightMap.get(CacheUtils.getWorkspaceId());
				if (subMap != null) {
					RightLevel rl = subMap.get(right.name());
					if (rl != null && rl.ordinal() >= level.ordinal()) return null;
				} else if (CacheUtils.isLoggedIn()){
					List<AdminWorkspace> wsList = Workspaces.getAll();
					if (wsList.size() > 0) {
						return Application.workspaces(Messages.get("firstly.select", Messages.get("workspace")));
					} else {
						return Application.getNoWorkspaceResult();
					}
				} else {
					return Application.login();
				}
			} 
		}

		return Application.getForbiddenResult();
	}

	private static int setUserRights(AdminUser user) {
		Map<Integer, Map<String, RightLevel>> rightMap = new HashMap<Integer, Map<String, RightLevel>>();

		log.info("Setting the user rights for : " + user.username);
		
		if (! (user.id.intValue() == 1 || user.isAdmin) && user.userGroup != null) {
			List<SqlRow> roleRows = Ebean.createSqlQuery("select workspace_id, user_role_id from admin_user_given_role where user_group_id = " + user.userGroup.id).findList();
			if (roleRows != null && roleRows.size() > 0) {
				for(SqlRow roleRow: roleRows) {

					String rightQuery = "select name, right_level from admin_user_right " +
										"where user_role_id = " + roleRow.getLong("user_role_id");
					List<SqlRow> rightRows = Ebean.createSqlQuery(rightQuery).findList();
					if (rightRows != null && rightRows.size() > 0) {
						Map<String, RightLevel> subMap = new HashMap<String, RightLevel>();

						for(SqlRow rightRow: rightRows) {
							String name = rightRow.getString("name");
							RightLevel level = RightLevel.findLevel(rightRow.getString("right_level"));
							subMap.put(name, level);
						}

						rightMap.put(roleRow.getInteger("workspace_id"), subMap);
					}
				}
			}
		}

		if (user.id.intValue() == 1 || user.isAdmin || rightMap.size() > 0) {
			log.info("The user rights have setted for : " + user.username);
			CacheUtils.setUser(user);
			CacheUtils.set(user.authToken, CacheKeys.RIGHTS, rightMap);
			return rightMap.size();
		}

		return 0;
	}

}
