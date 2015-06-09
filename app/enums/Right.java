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
package enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.avaje.ebean.annotation.EnumValue;

public enum Right {
	
	/*
	 * CARI MENUSU
	 */
	@EnumValue("CARI")
	CARI(Module.contact, true, false, false),

	@EnumValue("CARI_TANITIMI")
	CARI_TANITIMI(Module.contact, false, true, false),

	@EnumValue("CARI_KATEGORI_TANITIMI")
	CARI_KATEGORI_TANITIMI(Module.contact, false, true, false),

	@EnumValue("CARI_EKSTRA_ALANLAR")
	CARI_EKSTRA_ALANLAR(Module.contact, false, true, false),

	@EnumValue("CARI_ISLEM_KAYNAKLARI")
	CARI_ISLEM_KAYNAKLARI(Module.contact, false, true, false),
	// -----------------------------------------------------------------

	@EnumValue("CARI_ACILIS_ISLEMI")
	CARI_ACILIS_ISLEMI(Module.contact, false, true, false, TransType.Debt, 2),

	@EnumValue("CARI_BORC_DEKONTU")
	CARI_BORC_DEKONTU(Module.contact, false, true, false, TransType.Debt),

	@EnumValue("CARI_ALACAK_DEKONTU")
	CARI_ALACAK_DEKONTU(Module.contact, false, true, false, TransType.Credit),
	// -----------------------------------------------------------------

	@EnumValue("CARI_HESAP_LISTESI")
	CARI_HESAP_LISTESI(Module.contact, false, false, false),

	@EnumValue("CARI_ISLEM_LISTESI")
	CARI_ISLEM_LISTESI(Module.contact, false, false, false),

	@EnumValue("CARI_DURUM_RAPORU")
	CARI_DURUM_RAPORU(Module.contact, false, false, false),
	
	@EnumValue("CARI_ANALIZ_RAPORU")
	CARI_ANALIZ_RAPORU(Module.contact, false, false, false),

	@EnumValue("CARI_YASLANDIRMA_RAPORU")
	CARI_YASLANDIRMA_RAPORU(Module.contact, false, false, false),

	@EnumValue("CARI_HAREKET_RAPORU")
	CARI_HAREKET_RAPORU(Module.contact, false, false, false),

	@EnumValue("CARI_SON_ISLEM_RAPORU")
	CARI_SON_ISLEM_RAPORU(Module.contact, false, false, false),

	@EnumValue("CARI_HAREKETSIZ_CARILER_LISTESI")
	CARI_HAREKETSIZ_CARILER_LISTESI(Module.contact, false, false, false),

	/*
	 * CEK MENUSU
	 */
	@EnumValue("CEK")
	CEK(Module.cheque, true, false, false),

	@EnumValue("CEK_TURLERI")
	CEK_TURLERI(Module.cheque, false, true, false),

	@EnumValue("CEK_BORDRO_KAYNAKLARI")
	CEK_BORDRO_KAYNAKLARI(Module.cheque, false, true, false),
	// -----------------------------------------------------------------

	@EnumValue("CEK_MUSTERI_ACILIS_ISLEMI")
	CEK_MUSTERI_ACILIS_ISLEMI(Module.cheque, false, true, false, TransType.Debt, 2),

	@EnumValue("CEK_FIRMA_ACILIS_ISLEMI")
	CEK_FIRMA_ACILIS_ISLEMI(Module.cheque, false, true, false, TransType.Debt, 2),

	@EnumValue("CEK_GIRIS_BORDROSU")
	CEK_GIRIS_BORDROSU(Module.cheque, false, true, false, TransType.Debt),

	@EnumValue("CEK_MUSTERI_HAREKETLERI")
	CEK_MUSTERI_HAREKETLERI(Module.cheque, false, true, false, TransType.Debt),

	@EnumValue("CEK_PARCALI_TAHSILAT")
	CEK_PARCALI_TAHSILAT(Module.cheque, false, true, false, TransType.Debt),

	@EnumValue("CEK_CIKIS_BORDROSU")
	CEK_CIKIS_BORDROSU(Module.cheque, false, true, false, TransType.Credit),

	@EnumValue("CEK_FIRMA_HAREKETLERI")
	CEK_FIRMA_HAREKETLERI(Module.cheque, false, true, false, TransType.Credit),

	@EnumValue("CEK_PARCALI_ODEME")
	CEK_PARCALI_ODEME(Module.cheque, false, true, false, TransType.Credit),
	// -----------------------------------------------------------------

	@EnumValue("CEK_LISTESI")
	CEK_LISTESI(Module.cheque, false, false, false),

	@EnumValue("CEK_PARCALI_LISTESI")
	CEK_PARCALI_LISTESI(Module.cheque, false, false, false),

	@EnumValue("CEK_ISLEM_BORDRO_LISTESI")
	CEK_ISLEM_BORDRO_LISTESI(Module.cheque, false, false, false),

	@EnumValue("CEK_HAREKET_BORDRO_LISTESI")
	CEK_HAREKET_BORDRO_LISTESI(Module.cheque, false, false, false),

	/*
	 * SENET MENUSU
	 */
	@EnumValue("SENET")
	SENET(Module.bill, true, false, false),

	@EnumValue("SENET_TURLERI")
	SENET_TURLERI(Module.bill, false, true, false),

	@EnumValue("SENET_BORDRO_KAYNAKLARI")
	SENET_BORDRO_KAYNAKLARI(Module.bill, false, true, false),
	// -----------------------------------------------------------------

	@EnumValue("SENET_MUSTERI_ACILIS_ISLEMI")
	SENET_MUSTERI_ACILIS_ISLEMI(Module.bill, false, true, false, TransType.Debt, 2),

	@EnumValue("SENET_FIRMA_ACILIS_ISLEMI")
	SENET_FIRMA_ACILIS_ISLEMI(Module.bill, false, true, false, TransType.Debt, 2),

	@EnumValue("SENET_GIRIS_BORDROSU")
	SENET_GIRIS_BORDROSU(Module.bill, false, true, false, TransType.Debt),

	@EnumValue("SENET_MUSTERI_HAREKETLERI")
	SENET_MUSTERI_HAREKETLERI(Module.bill, false, true, false, TransType.Debt),

	@EnumValue("SENET_PARCALI_TAHSILAT")
	SENET_PARCALI_TAHSILAT(Module.bill, false, true, false, TransType.Debt),

	@EnumValue("SENET_CIKIS_BORDROSU")
	SENET_CIKIS_BORDROSU(Module.bill, false, true, false, TransType.Credit),

	@EnumValue("SENET_FIRMA_HAREKETLERI")
	SENET_FIRMA_HAREKETLERI(Module.bill, false, true, false, TransType.Credit),

	@EnumValue("SENET_PARCALI_ODEME")
	SENET_PARCALI_ODEME(Module.bill, false, true, false, TransType.Credit),
	// -----------------------------------------------------------------

	@EnumValue("SENET_LISTESI")
	SENET_LISTESI(Module.bill, false, false, false),

	@EnumValue("SENET_PARCALI_LISTESI")
	SENET_PARCALI_LISTESI(Module.bill, false, false, false),

	@EnumValue("SENET_ISLEM_BORDRO_LISTESI")
	SENET_ISLEM_BORDRO_LISTESI(Module.bill, false, false, false),

	@EnumValue("SENET_HAREKET_BORDRO_LISTESI")
	SENET_HAREKET_BORDRO_LISTESI(Module.bill, false, false, false),

	/*
	 * STOK MENUSU
	 */
	@EnumValue("STOK")
	STOK(Module.stock, true, false, false),

	@EnumValue("STOK_TANITIMI")
	STOK_TANITIMI(Module.stock, false, true, false),

	@EnumValue("STOK_KATEGORI_TANITIMI")
	STOK_KATEGORI_TANITIMI(Module.stock, false, true, false),

	@EnumValue("STOK_DEPO_TANITIMI")
	STOK_DEPO_TANITIMI(Module.stock, false, true, false),

	@EnumValue("STOK_BIRIM_TANITIMI")
	STOK_BIRIM_TANITIMI(Module.stock, false, true, false),

	@EnumValue("STOK_EKSTRA_ALANLAR")
	STOK_EKSTRA_ALANLAR(Module.stock, false, true, false),

	@EnumValue("STOK_FIS_KAYNAKLARI")
	STOK_FIS_KAYNAKLARI(Module.stock, false, true, false),
	
	// -----------------------------------------------------------------

	@EnumValue("STOK_ACILIS_ISLEMI")
	STOK_ACILIS_ISLEMI(Module.stock, false, true, false, TransType.Input, 2),

	@EnumValue("STOK_GIRIS_FISI")
	STOK_GIRIS_FISI(Module.stock, false, true, false, TransType.Input),

	@EnumValue("STOK_CIKIS_FISI")
	STOK_CIKIS_FISI(Module.stock, false, true, false, TransType.Output),

	@EnumValue("STOK_CIKIS_IADE_FISI")
	STOK_CIKIS_IADE_FISI(Module.stock, false, true, false, TransType.Input, 1),

	@EnumValue("STOK_GIRIS_IADE_FISI")
	STOK_GIRIS_IADE_FISI(Module.stock, false, true, false, TransType.Output, 1),
	// -----------------------------------------------------------------
	@EnumValue("STOK_TRANSFER_FISI")
	STOK_TRANSFER_FISI(Module.stock, false, true, false, TransType.Output),

	@EnumValue("STOK_TRANSFER_YANSI")
	STOK_TRANSFER_YANSI(Module.stock, true),
	// -----------------------------------------------------------------

	@EnumValue("STOK_FIYAT_GUNCELLEME")
	STOK_FIYAT_GUNCELLEME(Module.stock, false, true, false),
	
	@EnumValue("STOK_FIYAT_LISTESI")
	STOK_FIYAT_LISTESI(Module.stock, false, true, false),

	@EnumValue("STOK_MALIYET_HESAPLAMALARI")
	STOK_MALIYET_HESAPLAMALARI(Module.stock, false, true, false),

	@EnumValue("STOK_MALIYET_FAKTORLERI")
	STOK_MALIYET_FAKTORLERI(Module.stock, false, true, false),

	// -----------------------------------------------------------------

	@EnumValue("STOK_LISTESI")
	STOK_LISTESI(Module.stock, false, false, false),
	
	@EnumValue("STOK_FIYATLI_LISTE")
	STOK_FIYATLI_LISTE(Module.stock, false, false, false),

	@EnumValue("STOK_FIS_LISTESI")
	STOK_FIS_LISTESI(Module.stock, false, false, false),

	@EnumValue("STOK_HAREKET_RAPORU")
	STOK_HAREKET_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_ICMAL_RAPORU")
	STOK_ICMAL_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_TOPN_RAPORU")
	STOK_TOPN_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_DURUM_RAPORU")
	STOK_DURUM_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_ENVANTER_RAPORU")
	STOK_ENVANTER_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_KAR_ZARAR_RAPORU")
	STOK_KAR_ZARAR_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_SON_ISLEM_RAPORU")
	STOK_SON_ISLEM_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_BEKLEYEN_STOKLAR_RAPORU")
	STOK_BEKLEYEN_STOKLAR_RAPORU(Module.stock, false, false, false),

	@EnumValue("STOK_HAREKETSIZ_STOKLAR_LISTESI")
	STOK_HAREKETSIZ_STOKLAR_LISTESI(Module.stock, false, false, false),

	/*
	 * SIPARIS MENUSU
	 */
	@EnumValue("SIPARIS")
	SPRS(Module.order, true, false, false),

	@EnumValue("SPRS_FIS_KAYNAKLARI")
	SPRS_FIS_KAYNAKLARI(Module.order, false, true, false),

	@EnumValue("SPRS_ONAYLAMA_ADIMLARI")
	SPRS_ONAYLAMA_ADIMLARI(Module.order, false, false, false),

	// -----------------------------------------------------------------

	@EnumValue("SPRS_ALINAN_SIPARIS_FISI")
	SPRS_ALINAN_SIPARIS_FISI(Module.order, false, true, false, TransType.Output),

	@EnumValue("SPRS_VERILEN_SIPARIS_FISI")
	SPRS_VERILEN_SIPARIS_FISI(Module.order, false, true, false, TransType.Input),

	// -----------------------------------------------------------------

	@EnumValue("SPRS_FIS_LISTESI")
	SPRS_FIS_LISTESI(Module.order, false, false, false),

	@EnumValue("SPRS_DAGILIM_RAPORU")
	SPRS_DAGILIM_RAPORU(Module.order, false, false, false),

	/*
	 * IRSALIYE MENUSU
	 */
	@EnumValue("IRSALIYE")
	IRSL(Module.waybill, true, false, false),

	@EnumValue("IRSL_IRSALIYE_KAYNAKLARI")
	IRSL_IRSALIYE_KAYNAKLARI(Module.waybill, false, true, false),

	@EnumValue("IRSL_ONAYLAMA_ADIMLARI")
	IRSL_ONAYLAMA_ADIMLARI(Module.waybill, false, false, false),

	// -----------------------------------------------------------------
	
	@EnumValue("IRSL_ALIS_IRSALIYESI")
	IRSL_ALIS_IRSALIYESI(Module.waybill, false, true, false, TransType.Input),

	@EnumValue("IRSL_SATIS_IRSALIYESI")
	IRSL_SATIS_IRSALIYESI(Module.waybill, false, true, false, TransType.Output),

	// -----------------------------------------------------------------

	@EnumValue("IRSL_FIS_LISTESI")
	IRSL_FIS_LISTESI(Module.waybill, false, false, false),

	@EnumValue("IRSL_DAGILIM_RAPORU")
	IRSL_DAGILIM_RAPORU(Module.waybill, false, false, false),

	/*
	 * FATURA MENUSU
	 */
	@EnumValue("FATURA")
	FATR(Module.invoice, true, false, false),

	@EnumValue("FATR_FATURA_KAYNAKLARI")
	FATR_FATURA_KAYNAKLARI(Module.invoice, false, true, false),

	@EnumValue("FATR_KAPAMA_ISLEMI")
	FATR_KAPAMA_ISLEMI(Module.invoice, false, false, false),
	// -----------------------------------------------------------------

	@EnumValue("FATR_ALIS_FATURASI")
	FATR_ALIS_FATURASI(Module.invoice, false, true, false, TransType.Input),

	@EnumValue("FATR_SATIS_FATURASI")
	FATR_SATIS_FATURASI(Module.invoice, false, true, false, TransType.Output),

	@EnumValue("FATR_ALIS_IADE_FATURASI")
	FATR_ALIS_IADE_FATURASI(Module.invoice, false, true, false, TransType.Output, 1),

	@EnumValue("FATR_SATIS_IADE_FATURASI")
	FATR_SATIS_IADE_FATURASI(Module.invoice, false, true, false, TransType.Input, 1),

	// -----------------------------------------------------------------

	@EnumValue("FATR_FATURA_LISTESI")
	FATR_FATURA_LISTESI(Module.invoice, false, false, false),

	@EnumValue("FATR_DAGILIM_RAPORU")
	FATR_DAGILIM_RAPORU(Module.invoice, false, false, false),

	// -----------------------------------------------------------------

	/*
	 * KASA MENUSU
	 */
	@EnumValue("KASA")
	KASA(Module.safe, true, false, false),

	@EnumValue("KASA_TANITIMI")
	KASA_TANITIMI(Module.safe, false, true, false),

	@EnumValue("KASA_ISLEM_KAYNAKLARI")
	KASA_ISLEM_KAYNAKLARI(Module.safe, false, true, false),

	@EnumValue("KASA_GIDER_TANITIMI")
	KASA_GIDER_TANITIMI(Module.safe, false, true, false),

	// -----------------------------------------------------------------

	@EnumValue("KASA_ACILIS_ISLEMI")
	KASA_ACILIS_ISLEMI(Module.safe, false, true, false, TransType.Debt, 2),

	@EnumValue("KASA_TAHSIL_FISI")
	KASA_TAHSIL_FISI(Module.safe, false, true, false, TransType.Debt),

	@EnumValue("KASA_TEDIYE_FISI")
	KASA_TEDIYE_FISI(Module.safe, false, true, false, TransType.Credit),

	@EnumValue("KASA_MAHSUP_FISI")
	KASA_MAHSUP_FISI(Module.safe, false, true, false, TransType.Debt),
	// -----------------------------------------------------------------

	@EnumValue("KASA_ISLEM_LISTESI")
	KASA_ISLEM_LISTESI(Module.safe, false, false, false),

	@EnumValue("KASA_KASA_DEFTERI")
	KASA_KASA_DEFTERI(Module.safe, false, false, false),

	@EnumValue("KASA_GUNLUK_RAPOR")
	KASA_GUNLUK_RAPOR(Module.safe, false, false, false),

	@EnumValue("KASA_DURUM_RAPORU")
	KASA_DURUM_RAPORU(Module.safe, false, false, false),

	@EnumValue("KASA_HAREKET_RAPORU")
	KASA_HAREKET_RAPORU(Module.safe, false, false, false),

	/*
	 * BANKA MENUSU
	 */
	@EnumValue("BANK")
	BANK(Module.bank, true, false, false),

	@EnumValue("BANK_HESAP_TANITIMI")
	BANK_HESAP_TANITIMI(Module.bank, false, true, false),

	@EnumValue("BANK_ISLEM_KAYNAKLARI")
	BANK_ISLEM_KAYNAKLARI(Module.bank, false, true, false),

	@EnumValue("BANK_MASRAF_TANITIMI")
	BANK_MASRAF_TANITIMI(Module.bank, false, true, false),

	// -----------------------------------------------------------------

	@EnumValue("BANK_ACILIS_ISLEMI")
	BANK_ACILIS_ISLEMI(Module.bank, false, true, false, TransType.Debt, 2),

	@EnumValue("BANK_HESABA_PARA_GIRISI")
	BANK_HESABA_PARA_GIRISI(Module.bank, false, true, false, TransType.Debt),

	@EnumValue("BANK_HESAPTAN_PARA_CIKISI")
	BANK_HESAPTAN_PARA_CIKISI(Module.bank, false, true, false, TransType.Credit),
	// -----------------------------------------------------------------

	@EnumValue("BANK_ISLEM_LISTESI")
	BANK_ISLEM_LISTESI(Module.bank, false, false, false),

	@EnumValue("BANK_GUNLUK_RAPOR")
	BANK_GUNLUK_RAPOR(Module.bank, false, false, false),

	@EnumValue("BANK_DURUM_RAPORU")
	BANK_DURUM_RAPORU(Module.bank, false, false, false),

	@EnumValue("BANK_HAREKET_RAPORU")
	BANK_HAREKET_RAPORU(Module.bank, false, false, false),

	/*
	 * SATIS MENUSU
	 */
	@EnumValue("SATS")
	SATS(Module.sale, true, false, false),

	@EnumValue("SATS_SATICI_TANITIMI")
	SATS_SATICI_TANITIMI(Module.sale, false, true, false),

	@EnumValue("SATS_KAMPANYA_TANITIMI")
	SATS_KAMPANYA_TANITIMI(Module.sale, false, true, false),

	@EnumValue("SATS_SATIS_RAPORU")
	SATS_SATIS_RAPORU(Module.sale, false, false, false),

	/*
	 * GENEL MENUSU
	 */
	@EnumValue("GNEL")
	GNEL(Module.global, true, false, false),

	@EnumValue("GNEL_PROFIL_TANITIMI")
	GNEL_PROFIL_TANITIMI(Module.global, false, true, true),

	@EnumValue("GNEL_ISLEM_NOKTALARI")
	GNEL_ISLEM_NOKTALARI(Module.global, false, true, false),

	@EnumValue("GNEL_OZEL_KODLAR")
	GNEL_OZEL_KODLAR(Module.global, false, true, false),

	@EnumValue("GNEL_DOVIZ_BIRIMLERI")
	GNEL_DOVIZ_BIRIMLERI(Module.global, false, true, false),

	@EnumValue("GNEL_DOVIZ_KURLARI")
	GNEL_DOVIZ_KURLARI(Module.global, false, true, false),
	// -----------------------------------------------------------------

	/*
	 * ADMIN MENUSU
	 */
	@EnumValue("KULLANICI_TANITIMI")
	KULLANICI_TANITIMI(Module.admin, false, true, true),

	@EnumValue("KULLANICI_DEPARTMANLARI")
	KULLANICI_DEPARTMANLARI(Module.admin, false, true, true),

	@EnumValue("KULLANICI_GURUPLARI")
	KULLANICI_GURUPLARI(Module.admin, false, true, true),

	@EnumValue("KULLANICI_ROLLERI")
	KULLANICI_ROLLERI(Module.admin, false, true, true),

	@EnumValue("KULLANICI_HAREKETLERI")
	KULLANICI_HAREKETLERI(Module.admin, false, false, true),

	@EnumValue("EKSTRA_STOK_ALANLARI")
	EKSTRA_STOK_ALANLARI(Module.admin, false, true, true),
	
	@EnumValue("EKSTRA_CARI_ALANLARI")
	EKSTRA_CARI_ALANLARI(Module.admin, false, true, true),

	@EnumValue("CALISMA_ALANI")
	CALISMA_ALANI(Module.admin, false, true, true),

	@EnumValue("CALISMA_ALANI_TRANSFER")
	CALISMA_ALANI_TRANSFER(Module.admin, false, true, true),

	@EnumValue("GENEL_AYARLAR")
	GENEL_AYARLAR(Module.admin, false, true, true),

	@EnumValue("BELGE_TASARIMI")
	BELGE_TASARIMI(Module.admin, false, true, true),
	
	@EnumValue("BELGE_HEDEFLERI")
	BELGE_HEDEFLERI(Module.admin, false, true, true),

	@EnumValue("CARI_VIRMAN")
	CARI_VIRMAN(Module.contact, true),

	@EnumValue("KASA_VIRMAN")
	KASA_VIRMAN(Module.safe, true),

	@EnumValue("BANK_VIRMAN")
	BANK_VIRMAN(Module.bank, true),

	@EnumValue("ADMIN")
	ADMIN(Module.admin, true);

	public String key = "enum." + name();
	public TransType reverseTransType;

	public Module module;
	public boolean isShadow;
	public boolean isHeader;
	public boolean isCRUD;
	public boolean isReport;
	public boolean isAdminMenu;
	public boolean isReturn;
	public boolean isOpening;

	public TransType transType;

	private static Map<Module, Set<Right>> moduleRightMap = null;

	Right(Module module, boolean isShadow) {
		this.isShadow = isShadow;
		this.module = module;
	}

	Right(Module module, boolean isHeader, boolean isCRUD, boolean isAdminMenu) {
		this(module, isHeader, isCRUD, isAdminMenu, null, 0);
	}

	Right(Module module, boolean isHeader, boolean isCRUD, boolean isAdminMenu, TransType transType) {
		this(module, isHeader, isCRUD, isAdminMenu, transType, 0);
	}

	Right(Module module, boolean isHeader, boolean isCRUD, boolean isAdminMenu, TransType transType, int type) { //type -> 0:Normal, 1-Return, 2:Opening
		this.module = module;
		this.isHeader = isHeader;
		this.isCRUD = isCRUD;
		this.isAdminMenu = isAdminMenu;

		switch (type) {
			case 1: { //Return
				this.isReturn = true;
				break;
			}
			case 2: { //Opening
				this.isOpening = true;
				break;
			}
		}

		this.isReport = (! isHeader && ! isCRUD && ! isAdminMenu);

		this.transType = transType;
		if (transType != null) {
			this.reverseTransType = (transType.equals(TransType.Debt) ? TransType.Credit : TransType.Debt);
		}
	}

	public static Set<Right> getModuleRightSet(Module module) {
		if (moduleRightMap == null) buildModuleMap();
		return moduleRightMap.get(module);
	}

	public static Right findRight(String name) {
		try {
			return valueOf(name);
		} catch (Exception e) {
			return null;
		}
	}

	private static void buildModuleMap() {
		moduleRightMap = new HashMap<Module, Set<Right>>();

		for (Module module : Module.values()) {
			Set<Right> set = new TreeSet<Right>();
			for (Right right : Right.values()) {
				if (! right.isHeader && ! right.isShadow && module.equals(right.module)) {
					set.add(right);
				}
			}
			moduleRightMap.put(module, set);
		}
	}

}
