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
import enums.Right;
import enums.RightLevel;
/**
 * @author mdpinar
*/
class NovaposhtaMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.NOVAPOSHTA_GUNLUK_ISLEM_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.NOVAPOSHTA_GUNLUK_ISLEM_LISTESI.key), 
					controllers.novaposhta.reports.routes.DailyReport.index().url()));
		}
/*
		if (AuthManager.hasPrivilege(Right.NOVAPOSHTA_AYLIK_OZET_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.NOVAPOSHTA_AYLIK_OZET_RAPORU.key), 
					controllers.novaposhta.routes.Cargos.list().url()));
		}
*/
		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.NOVAPOSHTA_KARGO_HAREKETLERI, RightLevel.Enable)) {
			RightBind rightBind = new RightBind(Right.NOVAPOSHTA_KARGO_HAREKETLERI);

			subMenu.add(new MenuItem(Messages.get(Right.NOVAPOSHTA_KARGO_HAREKETLERI.key), 
				controllers.novaposhta.routes.Transes.list(rightBind).url()));
		}

		subMenu.add(new MenuItem(Messages.get(Right.NOVAPOSHTA_KARGO_TANITIMI.key), controllers.novaposhta.routes.Cargos.index().url()));

		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
