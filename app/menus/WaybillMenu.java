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
class WaybillMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();
		if (AuthManager.hasPrivilege(Right.IRSL_FIS_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.IRSL_FIS_LISTESI.key),
				controllers.waybill.reports.routes.ReceiptList.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.IRSL_DAGILIM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.IRSL_DAGILIM_RAPORU.key),
				controllers.waybill.reports.routes.DistReport.index().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.IRSL_SATIS_IRSALIYESI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.IRSL_SATIS_IRSALIYESI.key), 
				controllers.waybill.routes.Transes.list(new RightBind(Right.IRSL_SATIS_IRSALIYESI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.IRSL_ALIS_IRSALIYESI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.IRSL_ALIS_IRSALIYESI.key), 
					controllers.waybill.routes.Transes.list(new RightBind(Right.IRSL_ALIS_IRSALIYESI)).url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.IRSL_ONAYLAMA_ADIMLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.IRSL_ONAYLAMA_ADIMLARI.key), 
				controllers.waybill.routes.TransApprovals.index().url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.IRSL_IRSALIYE_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.IRSL_IRSALIYE_KAYNAKLARI.key), 
				controllers.waybill.routes.TransSources.index().url()));
		}

		if (subReportMenu.size() > 0) {
			if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));
			subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));
		}

		return subMenu;
	}

}
