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
