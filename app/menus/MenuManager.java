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
package menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import meta.MenuItem;
import models.GlobalProfile;
import play.i18n.Messages;
import utils.AuthManager;
import utils.CacheUtils;
import utils.GlobalCons;
import views.html.tools.templates.button_with_dropdown;
import enums.ChqbllSort;
import enums.Module;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class MenuManager {

	public static List<MenuItem> getMenuTree() {
		List<MenuItem> menuItems = new ArrayList<MenuItem>();

		if (CacheUtils.getWorkspaceId() != null) {
			if (AuthManager.hasPrivilege(Right.CARI_TANITIMI, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get("contact"), "fa fa-credit-card", new ContactMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Right.STOK_TANITIMI, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get("stock"), "fa fa-cubes", new StockMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Module.order, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get("order"), "fa fa-newspaper-o", new OrderMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Module.waybill, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get("waybill"), "fa fa-list-alt", new WaybillMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Module.invoice, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get("invoice"), "fa fa-keyboard-o", new InvoiceMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Module.cheque, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get(ChqbllSort.Cheque.key), "fa fa-credit-card", new ChequeMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Module.bill, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get(ChqbllSort.Bill.key), "fa fa-money", new BillMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Right.KASA_TANITIMI, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get("safe"), "fa fa-turkish-lira", new SafeMenu().getMenu()));
			}
	
			if (AuthManager.hasPrivilege(Right.BANK_HESAP_TANITIMI, RightLevel.Enable)) {
				menuItems.add(new MenuItem(Messages.get("bank"), "fa fa-bank", new BankMenu().getMenu()));
			}
	
			List<MenuItem> saleMenu = new SaleMenu().getMenu();
			if (saleMenu.size() > 0) {
				menuItems.add(new MenuItem(Messages.get("sale"), "fa fa-line-chart", saleMenu));
			}
	
			List<MenuItem> globMenu = new GlobalMenu().getMenu();
			if (globMenu.size() > 0) {
				menuItems.add(new MenuItem(Messages.get("glob"), "fa fa-globe", globMenu));
			}
		}

		if (CacheUtils.isSuperUser()) {
			menuItems.add(new MenuItem("Admin", "fa fa-gears", new AdminMenu().getMenu()));
		}

		//menuItems.add(new MenuItem(CacheUtils.getUser().username, "fa fa-user", addUserMenus()));

		return menuItems;
	}

	/**
	 * USER MENU
	 */
//	private static List<MenuItem> addUserMenus() {
//		List<MenuItem> subMenu = new ArrayList<MenuItem>();
//		subMenu.add(new MenuItem(Messages.get("user.info"), "#", "userInfoEdit()"));
//		subMenu.add(new MenuItem(MenuItemType.Divider));
//		subMenu.add(new MenuItem(Messages.get("logout"), controllers.routes.Application.logout().url()));
//
//		return subMenu;
//	}

	/**
	 * Daha sonra kullanilabilir, kalsin
	 * 
	 * @param choosen
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String buildLangMenu(String choosen) {
		List<MenuItem> menu = new ArrayList<MenuItem>();

		String link = "<span class='icon-%s'></span>&nbsp;<img src='/assets/img/%s.png'/>&nbsp;%s";
		for (Entry<String, String> entry : GlobalCons.getLangMap().entrySet()) {
			if (choosen != null && entry.getKey().equals(choosen)) {
				menu.add(new MenuItem(String.format(link, "ok", entry.getKey(), entry.getValue()), "#"));
			} else {
				menu.add(new MenuItem(String.format(link, "?", entry.getKey(), entry.getValue()), "/change_lang/"+entry.getKey()));
			}
		}

		return button_with_dropdown.render(Messages.get("languages"), menu, true, "primary").body();
	}

	/**
	 * Daha sonra kullanilabilir, kalsin
	 * 
	 * @param choosen
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String buildProfileMenu(String choosen) {
		List<MenuItem> menu = new ArrayList<MenuItem>();

		String link = "<span class='icon-%s'></span>&nbsp;%s";
		for (String profile : GlobalProfile.getNames()) {
			if (choosen.equals(profile)) {
				menu.add(new MenuItem(String.format(link, "ok", profile), "#"));
			} else {
				menu.add(new MenuItem(String.format(link, "?", profile), "/change_profile/"+profile));
			}
		}

		return button_with_dropdown.render(Messages.get("profiles"), menu, true, "info").body();
	}

}
