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
