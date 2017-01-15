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

import models.Contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import utils.CacheUtils;
import views.html.admins.exim.import_contacts_form;

import com.avaje.ebean.Ebean;

import controllers.Application;
import enums.ContactStatus;

/**
 * @author mdpinar
*/
public class ImportContacts extends Controller {

	private final static Logger log = LoggerFactory.getLogger(ImportContacts.class);

	public static Result index() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(
			import_contacts_form.render()
		);
	}

	/**
	 * CSV formatiyla yuklenen cari hesap bilgilerini iceriye aktarir, format su sekilde olmali;
	 * 
	 * 	 ! kolon ayrimi virgul ile olmalidir !
	 * 
	 *   code         (cari kodu)      en fazla 30 karakter metin ve bos olamaz,
	 *   name         (adi)            en fazla 100 karakter metin ve bos olamaz,
	 *   tax_office   (vergi dairesi)  en fazla 20 karakter metin,
	 *   tax_number   (vergi numarasi) en fazla 15 karakter metin,
	 *   tc_kimlik    (tc kimlik no)   en fazla 11 karakter metin,
	 *   relevant     (ilgili)         en fazla 30 karakter metin,
	 *   phone        (telefon)        en fazla 15 karakter metin,
  	 *   fax          (fax)            en fazla 15 karakter metin,
  	 *   mobile_phone (cep telefonu)   en fazla 15 karakter metin,
  	 *   address1     (adres satiri 1) en fazla 100 karakter metin,
  	 *   address2     (adres satiri 2) en fazla 100 karakter metin,
  	 *   city         (sehir)          en fazla 20 karakter metin,
  	 *   country      (ulke)           en fazla 20 karakter metin,
  	 *   email        (e posta adresi) en fazla 100 karakter metin,
  	 *   website      (web sitesi)     en fazla 100 karakter metin,
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
						Contact contact = Contact.findByCode(fields[0]);
						if (contact == null) {
							contact = new Contact();
							exist = false;
						}
						contact.code = fields[0];
						contact.name = fields[1];
						contact.taxOffice = fields[2];
						contact.taxNumber = fields[3];
						contact.tcKimlik = fields[4];
						contact.relevant = fields[5];
						contact.phone = fields[6];
						contact.fax = fields[7];
						contact.mobilePhone = fields[8];
						contact.address1 = fields[9];
						contact.address2 = fields[10];
						contact.city = fields[11];
						contact.country = fields[12];
						contact.email = fields[13];
						contact.website = fields[14];
						contact.note = fields[15];
						contact.status = ContactStatus.Normal;
						
						if (exist) {
							contact.update();
							updated++;
						} else {
							contact.save();
							inserted++;
						}
					}
					br.close();
					flash("success", Messages.get("imported.with.report", file.getFilename(), inserted, updated));
					ct = null;

					Ebean.commitTransaction();
				} catch (ArrayIndexOutOfBoundsException aoe) {
					ct = "Alan sayısı hatası. Olması gereken alan sayısı 16 fakat dosyada bulunan (bir satırdaki virgüllerle ayrılmış) alanların sayısı : " + aoe.getMessage();
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
