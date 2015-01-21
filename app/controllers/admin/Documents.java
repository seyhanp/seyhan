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

import static play.data.Form.form;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.AdminDocument;
import models.AdminDocumentField;
import models.AdminDocumentTarget;
import models.temporal.DocumentMultiplier;
import models.temporal.PrintData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Configuration;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import utils.CacheUtils;
import utils.CloneUtils;
import utils.StringUtils;
import views.html.admins.document.form;
import views.html.admins.document.list;
import views.html.admins.document.multiplier;
import views.html.admins.document.print_form;

import com.google.common.reflect.TypeToken;
import com.seyhanproject.pserver.Document;
import com.seyhanproject.pserver.Printing;

import controllers.Application;
import documents.BankTransFields;
import documents.ChqbllPartialFields;
import documents.ChqbllPayrollFields;
import documents.ChqbllTransFields;
import documents.ContactTransFields;
import documents.Helper;
import documents.InvoiceTransFields;
import documents.OrderTransFields;
import documents.SafeTransFields;
import documents.StockTransFields;
import documents.WaybillTransFields;
import enums.ChqbllSort;
import enums.DocBand;
import enums.Module;
import enums.Right;

/**
 * Belge tasarimlarini yapar. seyhan projesinde belgeler 2 ture ayrilir:
 *    1- Tek banta sahip belgeler (cari, kasa ve banka hareketleri icin tasarlanabilen belgeler)
 *    2- Cok banta sahip belgeler (stok, sipariş, fatura... hareketleri icin tasarlanabilen belgeler)
 * 
 * Bantlar:
 *    1- ReportTitle : Bir dokumde sadece ilk sayfada en basta dokumu yapilan banttir.
 *    2- PageTitle   : Her sayfada en basta (ilk sayfa icin ReportTitle dan hemen sonra) dokumu yapilan banttir.
 *    3- ** Detail **: Diger tum bantlardan geriye kalan alanlara dokumu yapilan tekrarli hareket bilgilerini iceren banttir.
 *    4- PageFooter  : Her sayfada en sonda dokumu yapilan banttir.
 *    5- ReportFooter: Bir dokumde sadece son sayfada en son (ve detay bandinin bittigi yerden baslayarak) dokumu yapilan banttir.
 * 
 * Detail Bandini aciklamak icin ornek degerler
 *    Sayfadaki satir sayisi : 30
 *    Rapor Basi : 0
 *    Sayfa Basi : 3
 *    Sayfa Sonu : 2
 *    Rapor Sonu : 0
 *    bu durumda detay kismi 25 satir olacak (30 - (ReportTitle+PageTitle+PageFooter+ReportFooter))
 * 
 * @author mdpinar
*/
public class Documents extends Controller {

	private final static Logger log = LoggerFactory.getLogger(Documents.class);

	private final static Form<AdminDocument> dataForm = form(AdminDocument.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), "20%", true, null).sortable("name"));
		headerList.add(new GridHeader(Messages.get("type"), "25%", true, null).sortable("right"));
		headerList.add(new GridHeader(Messages.get("description")));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminDocument> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<AdminDocument> modelList = AdminDocument.page();
		if (modelList != null && modelList.size() > 0) {
			for (AdminDocument model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, Messages.get(model.right.key));
				dataMap.put(i++, model.description);
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminDocument>(getHeaderList(), dataList, null);
	}

	/**
	 * Liste formuna doner
	 */
	public static Result GO_HOME() {
		return redirect(
			controllers.admin.routes.Documents.list()
		);
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(list.render(buildPage()));
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<AdminDocument> filledForm = dataForm.bindFromRequest();
		Boolean isSinglePage = Boolean.parseBoolean(filledForm.data().get("isSinglePage"));

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, isSinglePage));
		} else {

			AdminDocument model = filledForm.get();

			List<AdminDocumentField> removeFieldList = new ArrayList<AdminDocumentField>();

			if (model.reportTitleFields != null && model.reportTitleFields.size() > 0) {
				for (AdminDocumentField field: model.reportTitleFields) {
					if (field.band == null || field.type == null || field.name == null || field.name.trim().isEmpty()) {
						removeFieldList.add(field);
						continue;
					}
				}
				model.reportTitleFields.removeAll(removeFieldList);
			}
			
			removeFieldList.clear();
			if (model.pageTitleFields != null && model.pageTitleFields.size() > 0) {
				for (AdminDocumentField field: model.pageTitleFields) {
					if (field.band == null || field.type == null || field.name == null || field.name.trim().isEmpty()) {
						removeFieldList.add(field);
						continue;
					}
				}
				model.pageTitleFields.removeAll(removeFieldList);
			}

			removeFieldList.clear();
			for (AdminDocumentField field: model.detailFields) {
				if (field.band == null || field.type == null || field.name == null || field.name.trim().isEmpty()) {
					removeFieldList.add(field);
					continue;
				}
			}
			model.detailFields.removeAll(removeFieldList);

			removeFieldList.clear();
			if (model.pageFooterFields != null && model.pageFooterFields.size() > 0) {
				for (AdminDocumentField field: model.pageFooterFields) {
					if (field.band == null || field.type == null || field.name == null || field.name.trim().isEmpty()) {
						removeFieldList.add(field);
						continue;
					}
				}
				model.pageFooterFields.removeAll(removeFieldList);
			}
			
			removeFieldList.clear();
			if (model.reportFooterFields != null && model.reportFooterFields.size() > 0) {
				for (AdminDocumentField field: model.reportFooterFields) {
					if (field.band == null || field.type == null || field.name == null || field.name.trim().isEmpty()) {
						removeFieldList.add(field);
						continue;
					}
				}
				model.reportFooterFields.removeAll(removeFieldList);
			}

			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm, model.isSinglePage));
			}

			if (model.isActive == null) model.isActive = Boolean.TRUE;
			if (model.isSinglePage == null) model.isSinglePage = Boolean.TRUE;
			if (model.pageRows == null) model.pageRows = 66;
			if (model.reportTitleRows == null) model.reportTitleRows = 0;
			if (model.pageTitleRows == null) model.pageTitleRows = 0;
			if (model.detailRows == null) model.detailRows = 0;
			if (model.pageFooterRows == null) model.pageFooterRows = 0;
			if (model.reportFooterRows == null) model.reportFooterRows = 0;

			if (model.reportTitleLabels == null) model.reportTitleLabels = Boolean.FALSE;
			if (model.pageTitleLabels == null) model.pageTitleLabels = Boolean.FALSE;
			if (model.detailLabels == null) model.detailLabels = Boolean.FALSE;
			if (model.pageFooterLabels == null) model.pageFooterLabels = Boolean.FALSE;
			if (model.reportFooterLabels == null) model.reportFooterLabels = Boolean.FALSE;

			if (model.leftMargin == null) model.leftMargin = 0;
			if (model.topMargin == null) model.topMargin = 0;
			if (model.bottomMargin == null) model.bottomMargin = 0;

			if (model.carryingOverName != null && model.carryingOverName.trim().isEmpty()) model.carryingOverName = null;

			try {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(filledForm, model.isSinglePage));
			}

			flash("success", Messages.get("saved", model.name));
			return GO_HOME();
		}

	}

	/**
	 * Yeni bir kayit formu olusturur
	 * 
	 * @param moduleName
	 */
	public static Result create(String moduleName) {
		return screate(moduleName, null);
	}

	/**
	 * Modul ve belge basligina gore yeni bir kayit formu olusturur
	 * 
	 * @param moduleName
	 * @param header
	 */
	public static Result screate(String moduleName, String header) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		AdminDocument document = new AdminDocument(Module.valueOf(moduleName), header);
		return ok(form.render(dataForm.fill(document), document.isSinglePage));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			AdminDocument model = AdminDocument.findById(id);
			
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("design")));
			} else {
				return ok(form.render(dataForm.fill(model), model.isSinglePage));
			}
		}
		return GO_HOME();
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			AdminDocument model = AdminDocument.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("design")));
			} else {
				try {
					model.delete();
					flash("success", Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					flash("error", Messages.get("delete.violation", model.name));
					return badRequest(form.render(dataForm.fill(model), model.isSinglePage));
				}
			}
		}
		return GO_HOME();
	}

	/**
	 * Secilen kaydin kopyasini olusturur
	 * 
	 * @param id
	 */
	public static Result createClone(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		AdminDocument source = AdminDocument.findById(id);

		DocumentMultiplier dm = new DocumentMultiplier();
		dm.id = id;
		dm.module =  source.module;
		dm.header =  source.header;
		dm.right =  source.right;
		dm.name =  source.name;
		dm.description =  source.description;

		Form<DocumentMultiplier> imDataForm = form(DocumentMultiplier.class);

		return ok(
			multiplier.render(imDataForm.fill(dm))
		);
	}

	/**
	 * Yeni kopyayi kaydeder
	 */
	public static Result saveClone() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<DocumentMultiplier> stmDataForm = form(DocumentMultiplier.class);
		Form<DocumentMultiplier> filledForm = stmDataForm.bindFromRequest();

		DocumentMultiplier dm = filledForm.get();

		checkCloneConstraints(filledForm);
		if (filledForm.hasErrors()) {
			return badRequest(multiplier.render(filledForm));
		}

		AdminDocument source = AdminDocument.findForCloning(dm.id);

		AdminDocument clone = CloneUtils.cloneModel(source);
		clone.id = null;
		clone.module = dm.module;
		clone.header = dm.header;
		clone.right = dm.right;
		clone.name = dm.name;
		clone.templateRows = source.templateRows;
		clone.description = dm.description;
		clone.isActive = Boolean.TRUE;

		for (AdminDocumentField fld : clone.reportTitleFields) {
			fld.id = null;
			fld.reportTitleDoc = clone;
			CloneUtils.resetModel(fld);
		}
		for (AdminDocumentField fld : clone.pageTitleFields) {
			fld.id = null;
			fld.pageTitleDoc = clone;
			CloneUtils.resetModel(fld);
		}
		for (AdminDocumentField fld : clone.detailFields) {
			fld.id = null;
			fld.detailDoc = clone;
			CloneUtils.resetModel(fld);
		}
		for (AdminDocumentField fld : clone.pageFooterFields) {
			fld.id = null;
			fld.pageFooterDoc = clone;
			CloneUtils.resetModel(fld);
		}
		for (AdminDocumentField fld : clone.reportFooterFields) {
			fld.id = null;
			fld.reportFooterDoc = clone;
			CloneUtils.resetModel(fld);
		}

		clone.save();

		return ok(Messages.get("saved", clone.name));
	}
	
	/**
	 * Secilen hareket kaydi icin uzerinde yazdirma secenekleri bulunan formu acar
	 * 
	 * @param modelId
	 * @param rightName
	 */
	public static Result showPrintForm(Integer modelId, String rightName) {
		Right right = Right.findRight(rightName);

		Map<String, String> options = AdminDocumentTarget.options();

		if (options == null || options.size() < 1) {
			return ok("<p><span class='label label-important'>"+Messages.get("alert")+"</span><br/>"+Messages.get("any.design.target.found.firstly.do")+"</p>");
		}

		if (right != null) {
			options = AdminDocument.options(right);
		}

		if (options != null && options.size() > 0) {
			PrintData pd = new PrintData();
			pd.right = right;
			pd.modelId = modelId;
	
			Form<PrintData> dataForm = form(PrintData.class);
			return ok(
				print_form.render(dataForm.fill(pd), right)
			);
		} else {
			return ok("<p><span class='label label-important'>"+Messages.get("alert")+"</span><br/>"+Messages.get("any.design.found.firstly.do")+"</p>");
		}
	}

	/**
	 * Secilen hareket kaydini secilen hedefe gonderir
	 */
	public static Result printDocument() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<PrintData> prnDataForm = form(PrintData.class);
		Form<PrintData> filledForm = prnDataForm.bindFromRequest();
		Right right = Right.findRight(dataForm.data().get("right"));

		String result = "error";

		if(filledForm.hasErrors()) {
			return badRequest(print_form.render(filledForm, right));
		} else {
			
			PrintData model = filledForm.get();
			if (model.document != null && model.document.id != null && model.target != null) {
				String[] targetInfo = model.target.split("\\|");
				
				AdminDocument document = AdminDocument.findById(model.document.id);
				AdminDocumentTarget target = AdminDocumentTarget.findById(new Integer(targetInfo[0]));
				
				if (model.document != null && model.document.id != null && model.modelId != null) {
					Document doc = new Document();
	
					doc.brokerIp = Configuration.root().getString("seyhan.mq.ip");
					doc.userIp = Http.Context.current().request().remoteAddress();
					doc.username = CacheUtils.getUser().username;
					doc.workspace = CacheUtils.getWorkspaceName();
					doc.right = model.right.name();
					doc.id = model.modelId;
	
					doc.isLocal = target.isLocal;
					doc.path = target.path;
					doc.targetName = target.name;
					doc.targetType = target.targetType.ordinal();
					doc.viewType = target.viewType.ordinal();
					doc.isCompressed = target.isCompressed;
					doc.pageRows = document.pageRows;
					doc.rows = Helper.buildPage(document, doc.id);
	
					Printing.send(doc);
					result = "success";
				}
			}
		}

		return ok(result);
	}

	/**
	 * Daha onceden disa aktarilmis olan belge tasarim(lar)ini iceriye aktarir
	 */
	@SuppressWarnings("serial")
	public static Result imbort() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		String ct = "file is null!";
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart file = body.getFile("file");
		if (file != null) {
			ct = "content format isn't json!";
			ct = file.getContentType();
			if (file.getFilename().endsWith(".json") || file.getContentType().contains("json")) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(file.getFile()));
					String line;
					StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
					   sb.append(line);
					}
					br.close();
					List<AdminDocument> docList = StringUtils.fromJson(sb.toString(), new TypeToken<List<AdminDocument>>(){});
					for (AdminDocument doc : docList) {
						String newName = AdminDocument.findLastName(doc.name);
						if (newName != null) doc.name = newName;
						doc.save();
						log.info(doc.right + " : " + doc.name + " has imported.");
					}
					flash("success", Messages.get("imported", file.getFilename()));
					ct = null;
				} catch (Exception e) {
					ct = e.getMessage();
					log.error("ERROR", e);
				}
			}
		}

		if (ct != null) flash("error", Messages.get("error.in.import", ct));
		return Application.getCurrentPageResult();
	}

	/**
	 * Tasarlanmis belge(ler)i disariya aktarir
	 */
	public static Result export() {
		if (! CacheUtils.isLoggedIn()) return Application.login();
		
		response().setHeader("Content-Type", "text/json");
		response().setHeader("Content-Disposition", "attachment; filename=documents.json");

		flash("success", Messages.get("exported", Messages.get("documents"), "documents.json"));
		return ok(
			new ByteArrayInputStream(StringUtils.toJson(AdminDocument.listForExport()).getBytes())
		);
	}
	
	/**
	 * Kopya kaydin kaydedilmeden once uygunlugunu denetler
	 * 
	 * @param filledForm
	 */
	private static void checkCloneConstraints(Form<DocumentMultiplier> filledForm) {
		DocumentMultiplier model = filledForm.get();

		if (model.id == null) {
			filledForm.reject("name", Messages.get("id.is.null"));
		}

		if (AdminDocument.isUsedForElse("name", model.name, null)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

	/**
	 * Belge bandlarinda bulunabilecek alanlari doner
	 * 
	 * @param bandName
	 * @param moduleName
	 * @param header
	 * 
	 * @return Map<String, String>
	 */
	public static Map<String, String> getBandFields(String bandName, String moduleName, String header) {
		Module module = Module.valueOf(moduleName);
		DocBand band = DocBand.valueOf(StringUtils.capitalize(bandName));
		boolean isDetail = DocBand.Detail.equals(band);
		boolean isReportFooter = DocBand.ReportFooter.equals(band);

		switch (module) {
			case contact: {
				if (isDetail) {
					return ContactTransFields.getOptions();
				}
				break;
			}
			case bank: {
				if (isDetail) {
					return BankTransFields.getOptions();
				}
				break;
			}
			case safe: {
				if (isDetail) {
					return SafeTransFields.getOptions();
				}
				break;
			}
			case stock: {
				if (isDetail) {
					return StockTransFields.getDetailOptions();
				} else {
					return StockTransFields.getMasterOptions(isReportFooter);
				}
			}
			case order: {
				if (isDetail) {
					return OrderTransFields.getDetailOptions();
				} else {
					return OrderTransFields.getMasterOptions(isReportFooter);
				}
			}
			case waybill: {
				if (isDetail) {
					return WaybillTransFields.getDetailOptions();
				} else {
					return WaybillTransFields.getMasterOptions(isReportFooter);
				}
			}
			case invoice: {
				if (isDetail) {
					return InvoiceTransFields.getDetailOptions();
				} else {
					return InvoiceTransFields.getMasterOptions(isReportFooter);
				}
			}
			case cheque: {
				if (isDetail) {
					if ("cheque_payroll".equals(header)) {
						return ChqbllPayrollFields.getDetailOptions(ChqbllSort.Cheque);
					} else if ("cheque_transaction".equals(header)) {
						return ChqbllTransFields.getDetailOptions(ChqbllSort.Cheque);
					} else if ("cheque_partial".equals(header)) {
						return ChqbllPartialFields.getDetailOptions();
					}
				} else {
					if ("cheque_payroll".equals(header)) {
						return ChqbllPayrollFields.getMasterOptions(isReportFooter);
					} else if ("cheque_transaction".equals(header)) {
						return ChqbllTransFields.getMasterOptions(isReportFooter);
					} else if ("cheque_partial".equals(header)) {
						return ChqbllPartialFields.getMasterOptions(ChqbllSort.Cheque, isReportFooter);
					}
				}
			}
			case bill: {
				if (isDetail) {
					if ("bill_payroll".equals(header)) {
						return ChqbllPayrollFields.getDetailOptions(ChqbllSort.Bill);
					} else if ("bill_transaction".equals(header)) {
						return ChqbllTransFields.getDetailOptions(ChqbllSort.Bill);
					} else if ("bill_partial".equals(header)) {
						return ChqbllPartialFields.getDetailOptions();
					}
				} else {
					if ("bill_payroll".equals(header)) {
						return ChqbllPayrollFields.getMasterOptions(isReportFooter);
					} else if ("bill_transaction".equals(header)) {
						return ChqbllTransFields.getMasterOptions(isReportFooter);
					} else if ("bill_partial".equals(header)) {
						return ChqbllPartialFields.getMasterOptions(ChqbllSort.Bill, isReportFooter);
					}
				}
			}

		}
		
		return null;
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<AdminDocument> filledForm) {
		AdminDocument model = filledForm.get();

		if (AdminDocument.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}

		List<ValidationError> detailErrors = lookForErrors(DocBand.Detail, model.detailRows, model.detailFields);
		if (model.detailRows == null || model.detailRows.intValue() < 1 || model.detailRows.intValue() > model.pageRows) {
			detailErrors.add(new ValidationError(DocBand.Detail.name(), Messages.get("error.numberRangeByName", Messages.get("row_count"), 1, model.pageRows)));
		}
		if (detailErrors.size() > 0) {
			filledForm.errors().put(DocBand.Detail.name(), detailErrors);
		}

	}

	/**
	 * Kayit islemi icin ekstra denetimler
	 * 
	 * @param band
	 * @param limit
	 * @param fields
	 * 
	 * @see #checkConstraints(Form)

	 * @return List<ValidationError>
	 */
	private static List<ValidationError> lookForErrors(DocBand band, int limit, List<AdminDocumentField> fields) {
		List<ValidationError> result = new ArrayList<ValidationError>();

		if (fields != null && fields.size() > 0) {
			for (int i = 1; i < fields.size() + 1; i++) {
				AdminDocumentField field = fields.get(i-1);

				switch (field.type) {
					case LINE:
					case STATIC_TEXT: {
						if (field.value == null || field.value.trim().isEmpty()) {
							result.add(new ValidationError(field.band.name(), Messages.get("is.not.null.for.table", i, Messages.get("value"))));
						}
						break;
					}
					
					default: {
						if (field.row == null || field.row <= 0 || field.column == null || field.column <= 0 || field.width == null || field.width <= 0) {
							result.add(new ValidationError(field.band.name(), Messages.get("triple.cannot.be.zero.table", i)));
						}
						if (field.row != null && field.row > limit) {
							result.add(new ValidationError(field.band.name(), Messages.get("too.high.for.table", i, Messages.get("row"), limit)));
						}
						if (field.column != null && field.column > 132) {
							result.add(new ValidationError(field.band.name(), Messages.get("too.high.for.table", i, Messages.get("column"), 132)));
						}
						if (field.width != null && field.width > 132) {
							result.add(new ValidationError(field.band.name(), Messages.get("too.high.for.table", i, Messages.get("width"), 132)));
						}
					}
				}
			}

		} else if (DocBand.Detail.equals(band)) { 
			result.add(new ValidationError(band.name(), Messages.get("table.min.row.alert")));
		}
		
		return result;
	}

}
