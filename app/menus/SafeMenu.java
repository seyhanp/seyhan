/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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
class SafeMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.KASA_GUNLUK_RAPOR, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.KASA_GUNLUK_RAPOR.key), 
				controllers.safe.reports.routes.DailyReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.KASA_KASA_DEFTERI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.KASA_KASA_DEFTERI.key), 
				controllers.safe.reports.routes.CashbookList.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.KASA_ISLEM_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.KASA_ISLEM_LISTESI.key), 
					controllers.safe.reports.routes.TransactionList.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.KASA_DURUM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.KASA_DURUM_RAPORU.key), 
					controllers.safe.reports.routes.BalanceReport.index().url()));
		}

		if (subReportMenu.size() > 0) subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.KASA_HAREKET_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.KASA_HAREKET_RAPORU.key), 
				controllers.safe.reports.routes.TransReport.index().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		subMenu.add(new MenuItem(Messages.get(Right.KASA_TANITIMI.key), controllers.safe.routes.Safes.index().url()));

		isDividerAdded = false;
		if (AuthManager.hasPrivilege(Right.KASA_TAHSIL_FISI, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.KASA_TAHSIL_FISI.key), 
				controllers.safe.routes.Transes.list(new RightBind(Right.KASA_TAHSIL_FISI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.KASA_TEDIYE_FISI, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.KASA_TEDIYE_FISI.key), 
				controllers.safe.routes.Transes.list(new RightBind(Right.KASA_TEDIYE_FISI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.KASA_MAHSUP_FISI, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.KASA_MAHSUP_FISI.key), 
				controllers.safe.routes.Transes.list(new RightBind(Right.KASA_MAHSUP_FISI)).url()));
		}
		if (isDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.KASA_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.KASA_ACILIS_ISLEMI.key), 
				controllers.safe.routes.Transes.list(new RightBind(Right.KASA_ACILIS_ISLEMI)).url()));
		}

		subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.KASA_ISLEM_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.KASA_ISLEM_KAYNAKLARI.key), 
				controllers.safe.routes.TransSources.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.KASA_GIDER_TANITIMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.KASA_GIDER_TANITIMI.key), 
				controllers.safe.routes.Expenses.index().url()));
		}

		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
