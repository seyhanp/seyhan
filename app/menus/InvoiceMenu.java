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
class InvoiceMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();
		if (AuthManager.hasPrivilege(Right.FATR_FATURA_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.FATR_FATURA_LISTESI.key),
				controllers.invoice.reports.routes.InvoiceList.index().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.FATR_SATIS_FATURASI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.FATR_SATIS_FATURASI.key), 
				controllers.invoice.routes.Transes.list(new RightBind(Right.FATR_SATIS_FATURASI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.FATR_ALIS_FATURASI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.FATR_ALIS_FATURASI.key), 
				controllers.invoice.routes.Transes.list(new RightBind(Right.FATR_ALIS_FATURASI)).url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.FATR_SATIS_IADE_FATURASI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.FATR_SATIS_IADE_FATURASI.key), 
				controllers.invoice.routes.Transes.list(new RightBind(Right.FATR_SATIS_IADE_FATURASI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.FATR_ALIS_IADE_FATURASI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.FATR_ALIS_IADE_FATURASI.key), 
				controllers.invoice.routes.Transes.list(new RightBind(Right.FATR_ALIS_IADE_FATURASI)).url()));
		}

		if (AuthManager.hasPrivilege(Right.FATR_KAPAMA_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.FATR_KAPAMA_ISLEMI.key), 
					controllers.invoice.routes.TransApprovals.index().url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.FATR_FATURA_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.FATR_FATURA_KAYNAKLARI.key), 
				controllers.invoice.routes.TransSources.index().url()));
		}

		if (subReportMenu.size() > 0) {
			if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));
			subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));
		}

		return subMenu;
	}

}
