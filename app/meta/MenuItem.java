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
package meta;

import java.util.List;

import enums.MenuItemType;

/**
 * @author mdpinar
*/
public class MenuItem {

	public MenuItemType itemType;
	public String title;
	public String link;
	public String onclick;
	public List<MenuItem> subItems;
	public String icon;

	public MenuItem(MenuItemType itemType) {
		this.itemType = itemType;
	}

	public MenuItem(String title, String icon, List<MenuItem> subItems) {
		this(title, null, subItems, MenuItemType.Normal, null);
		this.icon = icon;
	}

	public MenuItem(String title, MenuItemType itemType) {
		this(title, null, null, itemType, null);
	}

	public MenuItem(String title, List<MenuItem> subItems, MenuItemType itemType) {
		this(title, null, subItems, itemType, null);
	}

	public MenuItem(String title, String link) {
		this(title, link, null, MenuItemType.Normal, null);
	}

	public MenuItem(String title, String link, String onclick) {
		this(title, link, null, MenuItemType.Normal, onclick);
	}

	public MenuItem(String title, String link, List<MenuItem> subItems, MenuItemType itemType, String onclick) {
		this.title = title;
		this.link = link;
		this.subItems = subItems;
		this.itemType = itemType;
		this.onclick = onclick;
	}

}
