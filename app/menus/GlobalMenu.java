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
class GlobalMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.GNEL_ISLEM_NOKTALARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.GNEL_ISLEM_NOKTALARI.key), 
				controllers.global.routes.TransPoints.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.GNEL_OZEL_KODLAR, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.GNEL_OZEL_KODLAR.key), 
				controllers.global.routes.PrivateCodes.index().url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.GNEL_DOVIZ_BIRIMLERI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.GNEL_DOVIZ_BIRIMLERI.key), 
				controllers.global.routes.Currencies.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.GNEL_DOVIZ_KURLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.GNEL_DOVIZ_KURLARI.key), 
				controllers.global.routes.CurrencyRates.list().url()));
		}

		if (subMenu.size() > 0) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.GNEL_PROFIL_TANITIMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.GNEL_PROFIL_TANITIMI.key), 
				controllers.global.routes.Profiles.list().url()));
		}

		return subMenu;
	}

}
