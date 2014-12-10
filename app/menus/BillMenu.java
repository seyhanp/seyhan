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
class BillMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.SENET_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.SENET_LISTESI.key), 
				controllers.chqbll.reports.routes.ChqbllList.index("Bill").url()));
		}

		if (AuthManager.hasPrivilege(Right.SENET_PARCALI_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.SENET_PARCALI_LISTESI.key), 
				controllers.chqbll.reports.routes.PartlyList.index("Bill").url()));
		}

		subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.SENET_ISLEM_BORDRO_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.SENET_ISLEM_BORDRO_LISTESI.key), 
				controllers.chqbll.reports.routes.PayrollList.index("Bill").url()));
		}

		if (AuthManager.hasPrivilege(Right.SENET_HAREKET_BORDRO_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.SENET_HAREKET_BORDRO_LISTESI.key), 
				controllers.chqbll.reports.routes.TransList.index("Bill").url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		boolean willDividerAdded = false;
		if (AuthManager.hasPrivilege(Right.SENET_GIRIS_BORDROSU, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.SENET_GIRIS_BORDROSU.key), 
				controllers.chqbll.routes.PayrollsForBill.list(new RightBind(Right.SENET_GIRIS_BORDROSU)).url()));
		}
		if (AuthManager.hasPrivilege(Right.SENET_MUSTERI_HAREKETLERI, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.SENET_MUSTERI_HAREKETLERI.key), 
				controllers.chqbll.routes.TransForBill.list(new RightBind(Right.SENET_MUSTERI_HAREKETLERI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.SENET_PARCALI_TAHSILAT, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.SENET_PARCALI_TAHSILAT.key), 
				controllers.chqbll.routes.PartialsForBill.list(new RightBind(Right.SENET_PARCALI_TAHSILAT)).url()));
		}

		if (willDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		willDividerAdded = false;
		if (AuthManager.hasPrivilege(Right.SENET_CIKIS_BORDROSU, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.SENET_CIKIS_BORDROSU.key), 
				controllers.chqbll.routes.PayrollsForBill.list(new RightBind(Right.SENET_CIKIS_BORDROSU)).url()));
		}
		if (AuthManager.hasPrivilege(Right.SENET_FIRMA_HAREKETLERI, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.SENET_FIRMA_HAREKETLERI.key), 
				controllers.chqbll.routes.TransForBill.list(new RightBind(Right.SENET_FIRMA_HAREKETLERI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.SENET_PARCALI_ODEME, RightLevel.Enable)) {
			willDividerAdded = true;

			subMenu.add(new MenuItem(Messages.get(Right.SENET_PARCALI_ODEME.key), 
				controllers.chqbll.routes.PartialsForBill.list(new RightBind(Right.SENET_PARCALI_ODEME)).url()));
		}

		if (willDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.SENET_MUSTERI_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SENET_MUSTERI_ACILIS_ISLEMI.key), 
				controllers.chqbll.routes.PayrollsForBill.list(new RightBind(Right.SENET_MUSTERI_ACILIS_ISLEMI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.SENET_FIRMA_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SENET_FIRMA_ACILIS_ISLEMI.key), 
				controllers.chqbll.routes.PayrollsForBill.list(new RightBind(Right.SENET_FIRMA_ACILIS_ISLEMI)).url()));
		}

		subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.SENET_TURLERI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SENET_TURLERI.key),
				controllers.chqbll.routes.TypesForBill.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.SENET_BORDRO_KAYNAKLARI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.SENET_BORDRO_KAYNAKLARI.key),
				controllers.chqbll.routes.PayrollSourcesForBill.index().url()));
		}

		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
