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
import play.i18n.Messages;
import utils.AuthManager;
import enums.MenuItemType;
import enums.Right;
import enums.RightLevel;
/**
 * @author mdpinar
*/
class SaleMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.SATS_SATIS_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.SATS_SATIS_RAPORU.key), 
				controllers.sale.reports.routes.SellingReport.index().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.SATS_SATICI_TANITIMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SATS_SATICI_TANITIMI.key), 
				controllers.sale.routes.Sellers.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.SATS_KAMPANYA_TANITIMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SATS_KAMPANYA_TANITIMI.key), 
				controllers.sale.routes.Campaigns.list().url()));
		}
		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
