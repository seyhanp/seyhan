/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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
