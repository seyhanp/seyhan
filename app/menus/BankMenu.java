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
class BankMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.BANK_GUNLUK_RAPOR, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.BANK_GUNLUK_RAPOR.key), 
				controllers.bank.reports.routes.DailyReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.BANK_ISLEM_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.BANK_ISLEM_LISTESI.key), 
					controllers.bank.reports.routes.TransactionList.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.BANK_DURUM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.BANK_DURUM_RAPORU.key), 
					controllers.bank.reports.routes.BalanceReport.index().url()));
		}

		if (subReportMenu.size() > 0) subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.BANK_HAREKET_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.BANK_HAREKET_RAPORU.key), 
				controllers.bank.reports.routes.TransReport.index().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		subMenu.add(new MenuItem(Messages.get(Right.BANK_HESAP_TANITIMI.key), controllers.bank.routes.Banks.index().url()));

		if (AuthManager.hasPrivilege(Right.BANK_HESABA_PARA_GIRISI, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.BANK_HESABA_PARA_GIRISI.key), 
				controllers.bank.routes.Transes.list(new RightBind(Right.BANK_HESABA_PARA_GIRISI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.BANK_HESAPTAN_PARA_CIKISI, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.BANK_HESAPTAN_PARA_CIKISI.key), 
				controllers.bank.routes.Transes.list(new RightBind(Right.BANK_HESAPTAN_PARA_CIKISI)).url()));
		}
		if (isDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.BANK_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.BANK_ACILIS_ISLEMI.key), 
				controllers.bank.routes.Transes.list(new RightBind(Right.BANK_ACILIS_ISLEMI)).url()));
			
			subMenu.add(new MenuItem(MenuItemType.Divider));
		}

		if (AuthManager.hasPrivilege(Right.BANK_ISLEM_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.BANK_ISLEM_KAYNAKLARI.key), 
				controllers.bank.routes.TransSources.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.BANK_MASRAF_TANITIMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.BANK_MASRAF_TANITIMI.key), 
				controllers.bank.routes.Expenses.index().url()));
		}
		
		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
