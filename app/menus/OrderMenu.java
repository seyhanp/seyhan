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

import meta.MenuItem;
import meta.RightBind;
import play.i18n.Messages;
import utils.AuthManager;
import enums.MenuItemType;
import enums.Right;
import enums.RightLevel;
/**
 * @author mdpinar
*/
class OrderMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();
		if (AuthManager.hasPrivilege(Right.SPRS_FIS_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.SPRS_FIS_LISTESI.key),
				controllers.order.reports.routes.ReceiptList.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.SPRS_DAGILIM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.SPRS_DAGILIM_RAPORU.key),
				controllers.order.reports.routes.DistReport.index().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.SPRS_ALINAN_SIPARIS_FISI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SPRS_ALINAN_SIPARIS_FISI.key), 
				controllers.order.routes.Transes.list(new RightBind(Right.SPRS_ALINAN_SIPARIS_FISI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.SPRS_VERILEN_SIPARIS_FISI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SPRS_VERILEN_SIPARIS_FISI.key), 
				controllers.order.routes.Transes.list(new RightBind(Right.SPRS_VERILEN_SIPARIS_FISI)).url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.SPRS_ONAYLAMA_ADIMLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SPRS_ONAYLAMA_ADIMLARI.key), 
				controllers.order.routes.TransApprovals.index().url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.SPRS_FIS_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SPRS_FIS_KAYNAKLARI.key), 
				controllers.order.routes.TransSources.index().url()));
		}

		if (subReportMenu.size() > 0) {
			if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));
			subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));
		}

		return subMenu;
	}

}
