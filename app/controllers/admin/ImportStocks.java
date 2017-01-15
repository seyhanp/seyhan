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
package controllers.admin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import models.Stock;
import models.StockBarcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import utils.CacheUtils;
import views.html.admins.exim.import_stocks_form;

import com.avaje.ebean.Ebean;

import controllers.Application;

/**
 * @author mdpinar
*/
public class ImportStocks extends Controller {

	private final static Logger log = LoggerFactory.getLogger(ImportStocks.class);

	public static Result index() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(
			import_stocks_form.render()
		);
	}

	/**
	 * CSV formatiyla yuklenen stok bilgilerini iceriye aktarir, format su sekilde olmali;
	 * 
	 * 	 ! kolon ayrimi virgul ile olmalidir !
	 * 
	 *   code         (cari kodu)      en fazla 30 karakter metin ve bos olamaz,
	 *   name         (adi)            en fazla 100 karakter metin ve bos olamaz,
	 *   barcode      (barkodu)        en fazla 50 karakter metin, bos olabilir,
	 *   buy_price    (alis fiyati)    ondalikli sayi
	 *   sell_price   (satis fiyati)   ondalikli sayi
	 *   buy_tax      (alis kdv)       ondalikli sayi
	 *   sell_tax     (satis kdv)      ondalikli sayi
	 *   unit_1       (birim 1)        en fazla 6 karakter metin,
	 *   unit_2       (birim 2)        en fazla 6 karakter metin,
	 *   unit_3       (birim 3)        en fazla 6 karakter metin,
	 *   unit2ratio   (2. birim icin katsayi - 1. birime gore-) Orn: 1 pakette 6 adet olur
	 *   unit3ratio   (3. birim icin katsayi - 1. birime gore-) Orn: 1 kolide 24 adet olur
	 *   prim_rate    (pirim orani)    ondalikli sayi
	 *   min_limit    (en az bulundurma limit sayisi)
	 *   max_limit    (en fazla bulundurma limit sayisi)
	 *   provider_code(saglayici kodu) en fazla 30 karakter metin,
  	 *   note         (ekstra bilgi)   uzun metin,
	 */
	public static Result imbort() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		String ct = "file is null!";
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart file = body.getFile("file");
		if (file != null) {
			ct = "content format isn't CSV!";
			ct = file.getContentType();
			if (file.getFilename().endsWith(".csv") || file.getContentType().contains("csv")) {
				Ebean.beginTransaction();
				try {
					BufferedReader br = new BufferedReader(new FileReader(file.getFile()));
					String line;
					int inserted = 0;
					int updated  = 0;
					while ((line = br.readLine()) != null) {
						String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					   
						boolean exist = true;
						Stock stock = Stock.findByCode(fields[0]);
						if (stock == null) {
							stock = new Stock();
							exist = false;
						}
						stock.code = fields[0];
						stock.name = fields[1];
						stock.buyPrice = Double.valueOf(fields[3]);
						stock.sellPrice = Double.valueOf(fields[4]);
						stock.buyTax = Double.valueOf(fields[5]);
						stock.sellTax = Double.valueOf(fields[6]);
						stock.unit1 = fields[7];
						stock.unit2 = fields[8];
						stock.unit3 = fields[9];
						stock.unit2Ratio = Double.valueOf(fields[10]);
						stock.unit3Ratio = Double.valueOf(fields[11]);
						stock.primRate = Double.valueOf(fields[12]);
						stock.minLimit = Double.valueOf(fields[13]);
						stock.maxLimit = Double.valueOf(fields[14]);
						stock.providerCode = fields[15];
						stock.note = fields[16];

						String barcode = fields[2];
						if (barcode != null && ! barcode.trim().isEmpty()) {
							StockBarcode sb = new StockBarcode(barcode);
							if (exist && stock.barcodes.size() > 0) {
								stock.barcodes.set(0, new StockBarcode(barcode));
							} else {
								stock.barcodes.add(sb);
							}
						} else if (exist) {
							Ebean.createSqlUpdate("DELETE FROM stock_barcode WHERE stock_id = " + stock.id).execute();
							stock.barcodes = new ArrayList<StockBarcode>();
						}
						
						if (exist) {
							stock.update();
							updated++;
						} else {
							stock.save();
							inserted++;
						}
					}
					br.close();
					flash("success", Messages.get("imported.with.report", file.getFilename(), inserted, updated));
					ct = null;

					Ebean.commitTransaction();
				} catch (ArrayIndexOutOfBoundsException aoe) {
					ct = "Alan sayısı hatası. Olması gereken alan sayısı 17 fakat dosyada bulunan (bir satırdaki virgüllerle ayrılmış) alanların sayısı : " + aoe.getMessage();
					log.error("ERROR", aoe);
				} catch (Exception e) {
					Ebean.rollbackTransaction();
					ct = e.getMessage();
					log.error("ERROR", e);
				}
			}
		}

		if (ct != null) flash("error", Messages.get("error.in.import", ct));
		return Application.getCurrentPageResult();
	}

}
