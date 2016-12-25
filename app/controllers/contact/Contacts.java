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
package controllers.contact;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.Contact;
import models.search.ContactSearchParam;
import models.temporal.InfoMultiplier;
import models.temporal.Pair;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CloneUtils;
import utils.QueryUtils;
import views.html.contacts.contact.form;
import views.html.contacts.contact.list;
import views.html.tools.components.info_multiplier;
import views.html.tools.templates.investigation_form;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.Module;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Contacts extends Controller {

	private final static Right RIGHT_SCOPE = Right.CARI_TANITIMI;

	private final static Logger log = LoggerFactory.getLogger(Contacts.class);
	private final static Form<Contact> dataForm = form(Contact.class);
	private final static Form<ContactSearchParam> paramForm = form(ContactSearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("code"), "12%").sortable("code"));
		headerList.add(new GridHeader(Messages.get("contact"), true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("phone"), "10%"));
		headerList.add(new GridHeader(Messages.get("mobile_phone"), "10%"));
		headerList.add(new GridHeader(Messages.get("category"), "10%"));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<Contact> buildPage(ContactSearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<Contact> page = Contact.page(searchParam);
		List<Contact> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (Contact model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.code);
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.phone);
				dataMap.put(i++, model.mobilePhone);
				dataMap.put(i++, (model.category != null ? model.category.name : ""));

				dataList.add(dataMap);
			}
		}

		return new PageExtend<Contact>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.contact.routes.Contacts.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<ContactSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(list.render(buildPage(filledParamForm.get()), filledParamForm));
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<Contact> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			Contact model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) {
				flash("error", editingConstraintError);
				return badRequest(form.render(dataForm.fill(model)));
			}

			checkConstraints(filledForm);
			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			try {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(dataForm.fill(model)));
			}
			
			flash("success", Messages.get("saved", model.name));
			if (Profiles.chosen().gnel_continuouslyRecording)
				return create();
			else
				return GO_HOME;
		}
	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		return ok(form.render(dataForm.fill(new Contact(""))));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			Contact model = Contact.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("contact")));
			} else {
				return ok(form.render(dataForm.fill(model)));
			}
		}
		return GO_HOME;
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Delete);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			Contact model = Contact.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("contact")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model)));
				}
				try {
					model.delete();
					flash("success", Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", model.name));
					return badRequest(form.render(dataForm.fill(model)));
				}
			}
		}
		return GO_HOME;
	}

	public static Result investigation(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Contact contact = Contact.findById(id);

		List<Pair> properties = new ArrayList<Pair>();
		properties.add(new Pair(Messages.get("contact.relevant"), contact.relevant));
		properties.add(new Pair(Messages.get("phone"), contact.phone));
		properties.add(new Pair("Fax", contact.fax));
		properties.add(new Pair(Messages.get("mobile_phone"), contact.mobilePhone));
		properties.add(new Pair("Email", contact.email));
		properties.add(new Pair(Messages.get("contact.website"), contact.website));
		properties.add(Pair.EMPTY);
		properties.add(new Pair(Messages.get("address") + " 1", contact.address1));
		properties.add(new Pair(Messages.get("address") + " 2", contact.address2));
		properties.add(new Pair(Messages.get("city"), contact.city));
		properties.add(new Pair(Messages.get("country"), contact.country));
		properties.add(Pair.EMPTY);
		properties.add(new Pair("TC Kimlik No", (contact.tcKimlik != null ? contact.tcKimlik.toString() : "")));
		properties.add(new Pair(Messages.get("contact.tax.no"), contact.taxNumber));
		properties.add(new Pair(Messages.get("contact.tax.office"), contact.taxOffice));

		ObjectNode result = Json.newObject();

		result.put("title", contact.name);
		result.put("body", investigation_form.render(
								QueryUtils.inspectXTrans(Module.contact, contact.id),
								QueryUtils.inspectXSummary(Module.contact, contact.id),
								properties, contact.note
							).body()
					);

		return ok(result);
	}

	/**
	 * Secilen kaydin kopyasini olusturur
	 * 
	 * @param id
	 */
	public static Result createClone(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		Contact source = Contact.findById(id);

		InfoMultiplier im = new InfoMultiplier();
		im.id = id;
		im.code =  source.code;
		im.name =  source.name;

		Form<InfoMultiplier> imDataForm = form(InfoMultiplier.class);

		return ok(
			info_multiplier.render(imDataForm.fill(im), controllers.contact.routes.Contacts.list().url(), Contacts.class.getSimpleName())
		);
	}

	/**
	 * Yeni kopyayi kaydeder
	 */
	public static Result saveClone() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<InfoMultiplier> stmDataForm = form(InfoMultiplier.class);
		Form<InfoMultiplier> filledForm = stmDataForm.bindFromRequest();

		InfoMultiplier im = filledForm.get();

		checkCloneConstraints(filledForm);
		if (filledForm.hasErrors()) {
			return badRequest(info_multiplier.render(filledForm, controllers.contact.routes.Contacts.list().url(), Contacts.class.getSimpleName()));
		}

		Contact clone = CloneUtils.cloneBaseModel(Contact.findById(im.id));
		clone.code = im.code;
		clone.name = im.name;
		clone.save();

		return ok(Messages.get("saved", clone.name));
	}

	private static void checkCloneConstraints(Form<InfoMultiplier> filledForm) {
		InfoMultiplier model = filledForm.get();

		if (model.id == null) {
			filledForm.reject("code", Messages.get("id.is.null"));
		}

		if (Contact.isUsedForElse("code", model.code, null)) {
			filledForm.reject("code", Messages.get("not.unique", model.code));
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<Contact> filledForm) {
		Contact model = filledForm.get();

		if (Contact.isUsedForElse("code", model.code, model.id)) {
			filledForm.reject("code", Messages.get("not.unique", model.code));
		}
		
		if (Contact.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}
