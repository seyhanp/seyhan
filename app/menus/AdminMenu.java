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
import enums.MenuItemType;
import enums.Right;
/**
 * @author mdpinar
*/
class AdminMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * KULLANICI ISLEMLERI
		 */
		List<MenuItem> userMenu = new ArrayList<MenuItem>();
		userMenu.add(new MenuItem(Messages.get(Right.KULLANICI_TANITIMI.key), controllers.admin.routes.Users.list().url()));
		userMenu.add(new MenuItem(Messages.get(Right.KULLANICI_GURUPLARI.key), controllers.admin.routes.UserGroups.list().url()));
		userMenu.add(new MenuItem(Messages.get(Right.KULLANICI_ROLLERI.key), controllers.admin.routes.UserRoles.list().url()));
		userMenu.add(new MenuItem(MenuItemType.Divider));
		userMenu.add(new MenuItem(Messages.get(Right.KULLANICI_HAREKETLERI.key), controllers.admin.routes.UserAudits.list().url()));

		/*
		 * WORKSPACE ISLEMLERI
		 */
		List<MenuItem> wsMenu = new ArrayList<MenuItem>();
		wsMenu.add(new MenuItem(Messages.get(Right.CALISMA_ALANI.key), controllers.admin.routes.Workspaces.list().url()));
		wsMenu.add(new MenuItem(Messages.get(Right.CALISMA_ALANI_TRANSFER.key), controllers.admin.routes.Ws2WsDataTransfers.show().url()));

		/*
		 * BELGE ISLEMLERI
		 */
		List<MenuItem> docMenu = new ArrayList<MenuItem>();
		docMenu.add(new MenuItem(Messages.get(Right.BELGE_TASARIMI.key), controllers.admin.routes.Documents.list().url()));
		docMenu.add(new MenuItem(Messages.get(Right.BELGE_HEDEFLERI.key), controllers.admin.routes.DocumentTargets.index().url()));

		/*
		 * EKSTRA ALAN TANIMLARI
		 */
		List<MenuItem> extraFieldsMenu = new ArrayList<MenuItem>();
		extraFieldsMenu.add(new MenuItem(Messages.get(Right.EKSTRA_CARI_ALANLARI.key), controllers.admin.routes.ExtraFieldsForContacts.index().url()));
		extraFieldsMenu.add(new MenuItem(Messages.get(Right.EKSTRA_STOK_ALANLARI.key), controllers.admin.routes.ExtraFieldsForStocks.index().url()));

		/*
		 * ILK MENU
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();
		subMenu.add(new MenuItem(Messages.get(Right.GENEL_AYARLAR.key), controllers.admin.routes.Settings.edit().url()));
		subMenu.add(new MenuItem(MenuItemType.Divider));
		subMenu.add(new MenuItem(Messages.get("users"), "fa fa-user", userMenu));
		subMenu.add(new MenuItem(Messages.get("workspaces"), "fa fa-building-o", wsMenu));
		subMenu.add(new MenuItem(Messages.get("documents"), "fa fa-files-o", docMenu));
		subMenu.add(new MenuItem(MenuItemType.Divider));
		subMenu.add(new MenuItem(Messages.get("extra_fields"), "fa fa-paperclip", extraFieldsMenu));
		
		return subMenu;
	}

}
