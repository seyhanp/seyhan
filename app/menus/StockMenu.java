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
import models.AdminExtraFields;
import play.i18n.Messages;
import utils.AuthManager;
import enums.MenuItemType;
import enums.Module;
import enums.Right;
import enums.RightLevel;
/**
 * @author mdpinar
*/
class StockMenu extends AbstractMenu {

	public List<MenuItem> getMenu() {
		/*
		 * RAPORLAR
		 */
		List<MenuItem> subReportMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.STOK_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_LISTESI.key),
				controllers.stock.reports.routes.StockList.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_FIS_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_FIS_LISTESI.key),
					controllers.stock.reports.routes.ReceiptList.index().url()));
		}

		if (subReportMenu.size() > 0) subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.STOK_TOPN_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_TOPN_RAPORU.key),
				controllers.stock.reports.routes.TopNReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_ICMAL_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_ICMAL_RAPORU.key),
				controllers.stock.reports.routes.CumulativeReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_HAREKET_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_HAREKET_RAPORU.key),
				controllers.stock.reports.routes.TransReport.index().url()));
		}

		if (subReportMenu.size() > 0) subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.STOK_DURUM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_DURUM_RAPORU.key),
				controllers.stock.reports.routes.StockStatusReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_ENVANTER_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_ENVANTER_RAPORU.key),
				controllers.stock.reports.routes.InventoryReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_KAR_ZARAR_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_KAR_ZARAR_RAPORU.key),
				controllers.stock.reports.routes.ProfitLossReport.index().url()));
		}

		if (subReportMenu.size() > 0) subReportMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.STOK_SON_ISLEM_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_SON_ISLEM_RAPORU.key), 
				controllers.stock.reports.routes.LastTransReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_BEKLEYEN_STOKLAR_RAPORU, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_BEKLEYEN_STOKLAR_RAPORU.key),
				controllers.stock.reports.routes.WaitingStocksReport.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_HAREKETSIZ_STOKLAR_LISTESI, RightLevel.Enable)) {
			subReportMenu.add(new MenuItem(Messages.get(Right.STOK_HAREKETSIZ_STOKLAR_LISTESI.key),
				controllers.stock.reports.routes.InactiveStocksList.index().url()));
		}

		/*
		 * BASIT TANIMLAR
		 */
		List<MenuItem> subInfoMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.STOK_DEPO_TANITIMI, RightLevel.Enable)) {
			subInfoMenu.add(new MenuItem(Messages.get(Right.STOK_DEPO_TANITIMI.key), 
					controllers.stock.routes.Depots.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_BIRIM_TANITIMI, RightLevel.Enable)) {
			subInfoMenu.add(new MenuItem(Messages.get(Right.STOK_BIRIM_TANITIMI.key), 
					controllers.stock.routes.Units.index().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_KATEGORI_TANITIMI, RightLevel.Enable)) {
			subInfoMenu.add(new MenuItem(Messages.get(Right.STOK_KATEGORI_TANITIMI.key), 
				controllers.stock.routes.Categories.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.STOK_FIS_KAYNAKLARI, RightLevel.Enable)) {
			subInfoMenu.add(new MenuItem(Messages.get(Right.STOK_FIS_KAYNAKLARI.key), 
				controllers.stock.routes.TransSources.index().url()));
		}

		if (AuthManager.hasPrivilege(Right.STOK_EKSTRA_ALANLAR, RightLevel.Enable)) {
			List<AdminExtraFields> extraFieldList = AdminExtraFields.listAll(Module.stock.name());
			if (extraFieldList != null && extraFieldList.size() > 0) {
				isDividerAdded = false;

				for (AdminExtraFields ef : extraFieldList) {
					subInfoMenu.add(new MenuItem(Messages.get("definition.of", ef.name), controllers.stock.routes.ExtraFields.index(ef.idno).url()));
				}

			}
		}

		/*
		 * DIGER
		 */
		List<MenuItem> subOtherMenu = new ArrayList<MenuItem>();

		if (AuthManager.hasPrivilege(Right.STOK_MALIYET_HESAPLAMALARI, RightLevel.Enable)) {
			subOtherMenu.add(new MenuItem(Messages.get(Right.STOK_MALIYET_HESAPLAMALARI.key), 
				controllers.stock.routes.Costings.list().url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_MALIYET_FAKTORLERI, RightLevel.Enable)) {
			subOtherMenu.add(new MenuItem(Messages.get(Right.STOK_MALIYET_FAKTORLERI.key), 
				controllers.stock.routes.CostFactors.index().url()));
		}

		subOtherMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.STOK_FIYAT_GUNCELLEME, RightLevel.Enable)) {
			subOtherMenu.add(new MenuItem(Messages.get(Right.STOK_FIYAT_GUNCELLEME.key), 
				controllers.stock.routes.PriceUpdates.list().url()));
		}
		
		if (AuthManager.hasPrivilege(Right.STOK_FIYAT_LISTESI, RightLevel.Enable)) {
			subOtherMenu.add(new MenuItem(Messages.get(Right.STOK_FIYAT_LISTESI.key), 
					controllers.stock.routes.PriceLists.list().url()));
		}

		/*
		 * TANITIMLAR
		 */
		List<MenuItem> subMenu = new ArrayList<MenuItem>();

		subMenu.add(new MenuItem(Messages.get(Right.STOK_TANITIMI.key), controllers.stock.routes.Stocks.list().url()));

		isDividerAdded = false;
		if (AuthManager.hasPrivilege(Right.STOK_GIRIS_FISI, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.STOK_GIRIS_FISI.key), 
				controllers.stock.routes.Transes.list(new RightBind(Right.STOK_GIRIS_FISI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_CIKIS_FISI, RightLevel.Enable)) {
			isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.STOK_CIKIS_FISI.key), 
				controllers.stock.routes.Transes.list(new RightBind(Right.STOK_CIKIS_FISI)).url()));
		}
		if (isDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.STOK_GIRIS_IADE_FISI, RightLevel.Enable)) {
			if (! isDividerAdded) isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.STOK_GIRIS_IADE_FISI.key), 
				controllers.stock.routes.Transes.list(new RightBind(Right.STOK_GIRIS_IADE_FISI)).url()));
		}
		if (AuthManager.hasPrivilege(Right.STOK_CIKIS_IADE_FISI, RightLevel.Enable)) {
			if (! isDividerAdded) isDividerAdded = addDivider(subMenu, isDividerAdded);

			subMenu.add(new MenuItem(Messages.get(Right.STOK_CIKIS_IADE_FISI.key), 
				controllers.stock.routes.Transes.list(new RightBind(Right.STOK_CIKIS_IADE_FISI)).url()));
		}
		if (isDividerAdded) subMenu.add(new MenuItem(MenuItemType.Divider));

		if (AuthManager.hasPrivilege(Right.STOK_ACILIS_ISLEMI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.STOK_ACILIS_ISLEMI.key), 
				controllers.stock.routes.Transes.list(new RightBind(Right.STOK_ACILIS_ISLEMI)).url()));
		}

		if (AuthManager.hasPrivilege(Right.STOK_TRANSFER_FISI, RightLevel.Enable)) {
			subMenu.add(new MenuItem(Messages.get(Right.STOK_TRANSFER_FISI.key), 
				controllers.stock.routes.Transes.list(new RightBind(Right.STOK_TRANSFER_FISI)).url()));

			subMenu.add(new MenuItem(MenuItemType.Divider));
		}

		if (subInfoMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("defines"), "fa fa-pencil-square-o", subInfoMenu));
		if (subOtherMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("other_ops"), "fa fa-tasks", subOtherMenu));
		if (subReportMenu.size() > 0) subMenu.add(new MenuItem(Messages.get("reports"), "fa fa-file-text-o", subReportMenu));

		return subMenu;
	}

}
