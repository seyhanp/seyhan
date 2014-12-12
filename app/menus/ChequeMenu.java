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
class ChequeMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.CEK_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CEK_LISTESI.key), 
				controllers.chqbll.reports.routes.ChqbllList.index("Cheque").url()));
		}

		if (AuthManager.hasPrivilege(Right.CEK_PARCALI_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CEK_PARCALI_LISTESI.key), 
				controllers.chqbll.reports.routes.PartlyList.index("Cheque").url()));
		}

		subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.CEK_ISLEM_BORDRO_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CEK_ISLEM_BORDRO_LISTESI.key), 
				controllers.chqbll.reports.routes.PayrollList.index("Cheque").url()));
		}

		if (AuthManager.hasPrivilege(Right.CEK_HAREKET_BORDRO_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CEK_HAREKET_BORDRO_LISTESI.key), 
				controllers.chqbll.reports.routes.TransList.index("Cheque").url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		boolean willDividerAdded = false;
		if (AuthManager.hasPrivilege(Right.CEK_GIRIS_BORDROSU, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.CEK_GIRIS_BORDROSU.key), 
				controllers.chqbll.routes.PayrollsForCheque.list(new RightBind(Right.CEK_GIRIS_BORDROSU)).url()));
		}
		if (AuthManager.hasPrivilege(Right.CEK_MUSTERI_HAREKETLERI, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.CEK_MUSTERI_HAREKETLERI.key), 
				controllers.chqbll.routes.TransForCheque.list(new RightBind(Right.CEK_MUSTERI_HAREKETLERI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.CEK_PARCALI_TAHSILAT, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.CEK_PARCALI_TAHSILAT.key), 
				controllers.chqbll.routes.PartialsForCheque.list(new RightBind(Right.CEK_PARCALI_TAHSILAT)).url()));
		}

		if (willDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		willDividerAdded = false;
		if (AuthManager.hasPrivilege(Right.CEK_CIKIS_BORDROSU, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.CEK_CIKIS_BORDROSU.key), 
				controllers.chqbll.routes.PayrollsForCheque.list(new RightBind(Right.CEK_CIKIS_BORDROSU)).url()));
		}
		if (AuthManager.hasPrivilege(Right.CEK_FIRMA_HAREKETLERI, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.CEK_FIRMA_HAREKETLERI.key), 
				controllers.chqbll.routes.TransForCheque.list(new RightBind(Right.CEK_FIRMA_HAREKETLERI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.CEK_PARCALI_ODEME, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.CEK_PARCALI_ODEME.key), 
				controllers.chqbll.routes.PartialsForCheque.list(new RightBind(Right.CEK_PARCALI_ODEME)).url()));
		}

		if (willDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.CEK_MUSTERI_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.CEK_MUSTERI_ACILIS_ISLEMI.key), 
				controllers.chqbll.routes.PayrollsForCheque.list(new RightBind(Right.CEK_MUSTERI_ACILIS_ISLEMI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.CEK_FIRMA_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.CEK_FIRMA_ACILIS_ISLEMI.key), 
				controllers.chqbll.routes.PayrollsForCheque.list(new RightBind(Right.CEK_FIRMA_ACILIS_ISLEMI)).url()));
		}

		subMenu.add(new MenuItem(MenuItemType.Divider));
		
		if (AuthManager.hasPrivilege(Right.CEK_TURLERI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.CEK_TURLERI.key),
				controllers.chqbll.routes.TypesForCheque.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.CEK_BORDRO_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.CEK_BORDRO_KAYNAKLARI.key),
				controllers.chqbll.routes.PayrollSourcesForCheque.index().url()));
		}
		
		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
