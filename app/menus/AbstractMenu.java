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

import java.util.List;

import meta.MenuItem;
import enums.MenuItemType;

/**
 * @author mdpinar
*/
public abstract class AbstractMenu {

	boolean isDividerAdded = false;

	public abstract List<MenuItem> getMenu();

	boolean addDivider(List<MenuItem> subMenu, boolean isAdded) {
		if (! isAdded) {
			subMenu.add(new MenuItem(MenuItemType.Divider));
		}
		return true;
	}

}
