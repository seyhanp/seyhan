/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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
