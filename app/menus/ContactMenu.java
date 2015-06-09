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
import models.AdminExtraFields;
import play.i18n.Messages;
import utils.AuthManager;
import enums.MenuItemType;
import enums.Module;
import enums.Right;
import enums.RightLevel;
/**
 * @author mdpinar
*/
class ContactMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.CARI_HESAP_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_HESAP_LISTESI.key), 
				controllers.contact.reports.routes.ContactList.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.CARI_ISLEM_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_ISLEM_LISTESI.key), 
				controllers.contact.reports.routes.TransactionList.index().url()));
		}

		if (subReportMenu.size() > 0) subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.CARI_DURUM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_DURUM_RAPORU.key), 
				controllers.contact.reports.routes.BalanceReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.CARI_HAREKET_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_HAREKET_RAPORU.key), 
				controllers.contact.reports.routes.TransReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.CARI_ANALIZ_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_ANALIZ_RAPORU.key), 
				controllers.contact.reports.routes.AnalyzeReport.index().url()));
		}

		if (subReportMenu.size() > 0) subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.CARI_SON_ISLEM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_SON_ISLEM_RAPORU.key), 
				controllers.contact.reports.routes.LastTransReport.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.CARI_HAREKETSIZ_CARILER_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_HAREKETSIZ_CARILER_LISTESI.key),
				controllers.contact.reports.routes.InactiveContactsList.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.CARI_YASLANDIRMA_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.CARI_YASLANDIRMA_RAPORU.key), 
				controllers.contact.reports.routes.AgingReport.index().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		subMenu.add(new MenuItem(Messages.get(Right.CARI_TANITIMI.key), controllers.contact.routes.Contacts.list().url()));

		isDividerAdded = false;
		if (AuthManager.hasPrivilege(Right.CARI_BORC_DEKONTU, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.CARI_BORC_DEKONTU.key), 
				controllers.contact.routes.Transes.list(new RightBind(Right.CARI_BORC_DEKONTU)).url()));
		}
		if (AuthManager.hasPrivilege(Right.CARI_ALACAK_DEKONTU, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.CARI_ALACAK_DEKONTU.key), 
				controllers.contact.routes.Transes.list(new RightBind(Right.CARI_ALACAK_DEKONTU)).url()));
		}
		if (isDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.CARI_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.CARI_ACILIS_ISLEMI.key), 
				controllers.contact.routes.Transes.list(new RightBind(Right.CARI_ACILIS_ISLEMI)).url()));
			
			subMenu.add(new MenuItem(MenuItemType.Divider));
		}

		subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.CARI_KATEGORI_TANITIMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.CARI_KATEGORI_TANITIMI.key), 
				controllers.contact.routes.Categories.index().url()));
			subMenu.add(new MenuItem(MenuItemType.Divider));
		}
		if (AuthManager.hasPrivilege(Right.CARI_ISLEM_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.CARI_ISLEM_KAYNAKLARI.key), 
				controllers.contact.routes.TransSources.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.CARI_EKSTRA_ALANLAR, RightLevel.Enable)) {
			List<AdminExtraFields> extraFieldList = AdminExtraFields.listAll(Module.contact.name());
			if (extraFieldList != null && extraFieldList.size() > 0) {
				isDividerAdded = false;

				for (AdminExtraFields ef : extraFieldList) {
					subMenu.add(new MenuItem(Messages.get("definition.of", ef.name), controllers.contact.routes.ExtraFields.index(ef.idno).url()));
				}

			}
		}
		
		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
