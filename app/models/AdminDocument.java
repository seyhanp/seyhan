/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;
import utils.NumericUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;

import enums.CacheKeys;
import enums.ColumnTitleType;
import enums.Module;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class AdminDocument extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	public Module module;

	/**
	 * Ekstra parametre 
	 */
	public String header;
	
	@Column(name = "_right")
	@Constraints.Required
	public Right right;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(20)
	public String name;

	@Constraints.MaxLength(30)
	public String description;

	public Boolean isActive = Boolean.TRUE;

	/*
	 * Bir sayfada bulunacak satir sayisi
	 */
	public Integer pageRows = 66;
	public Integer reportTitleRows = 0;
	public Integer pageTitleRows = 3;
	public Integer detailRows = 1;
	public Integer pageFooterRows = 3;
	public Integer reportFooterRows = 0;

	public Boolean reportTitleLabels = Boolean.TRUE;
	public Boolean pageTitleLabels = Boolean.TRUE;
	public Boolean detailLabels = Boolean.TRUE;
	public Boolean pageFooterLabels = Boolean.TRUE;
	public Boolean reportFooterLabels = Boolean.TRUE;

	public Integer leftMargin = 0;
	public Integer topMargin = 0;
	public Integer bottomMargin = 0;

	/*
	 * Cari Borc dekontu Tek Sayfadir
	 * Stok Giris Fisi Cok Sayfadir
	 */
	public Boolean isSinglePage;
	
	public Boolean hasPaging = Boolean.TRUE;

	public ColumnTitleType columnTitleType = ColumnTitleType.NOTHING;

	/*
	 * Nakli yekun olacak mi? Olacaksa field in nickName'i olur.
	 */
	public String carryingOverName;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="reportTitleDoc", orphanRemoval = true)
	public List<AdminDocumentField> reportTitleFields;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="pageTitleDoc", orphanRemoval = true)
	public List<AdminDocumentField> pageTitleFields;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="detailDoc", orphanRemoval = true)
	public List<AdminDocumentField> detailFields;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="pageFooterDoc", orphanRemoval = true)
	public List<AdminDocumentField> pageFooterFields;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="reportFooterDoc", orphanRemoval = true)
	public List<AdminDocumentField> reportFooterFields;

	@Lob
	public String templateRows;

	@Version
	public Integer version;

	private static Model.Finder<Integer, AdminDocument> find = new Model.Finder<Integer, AdminDocument>(Integer.class, AdminDocument.class);
	
	public AdminDocument() {
		;
	}

	public AdminDocument(Module module, String header) {
		super();
		this.module = module;
		this.header = header;
		this.isSinglePage = (module.equals(Module.contact) || module.equals(Module.bank) || module.equals(Module.safe));
	}

	public static Map<String, String> options(Right right) {
		Map<String, String> result = CacheUtils.get(true, Right.BELGE_TASARIMI.name() + CacheKeys.OPTIONS.value + "." + right);
		
		if (result == null) {
			result = new LinkedHashMap<String, String>();
		}

		if (right != null) {
			List<AdminDocument> modelList = find.where()
													.eq("right", right)
													.eq("isActive", Boolean.TRUE)
												.orderBy("name")
											.findList();
			for (AdminDocument gd : modelList) {
				result.put(gd.id.toString(), gd.toString());
			}
			CacheUtils.set(true, Right.BELGE_TASARIMI.name() + CacheKeys.OPTIONS.value + "." + right, result);
		}

		return result;
	}

	public static List<AdminDocument> page() {
		Pair sortInfo = CookieUtils.getSortInfo(Right.BELGE_TASARIMI, "right");
		return find.where()
					.orderBy(sortInfo.key + " " + sortInfo.value)
				.findList();
	}

	public static List<String> getNames() {
		List<String> result = CacheUtils.get(true, Right.BELGE_TASARIMI.name() + CacheKeys.LIST_ALL.value);

		if (result == null) {
			List<AdminDocument> modelList = find.select("name")
												.where()
													.eq("isActive", Boolean.TRUE)
												.orderBy("name")
											.findList();
			result = new ArrayList<String>();
			for (AdminDocument gd : modelList) {
				result.add(gd.name);
			}
			CacheUtils.set(true, Right.BELGE_TASARIMI.name() + CacheKeys.LIST_ALL.value, result);
		}

		return result;
	}

	public static AdminDocument findById(Integer id) {
		return find.byId(id);
	}

	public static AdminDocument findForCloning(Integer id) {
		return find.fetch("reportTitleFields")
					.fetch("pageTitleFields")
					.fetch("detailFields")
					.fetch("pageFooterFields")
					.fetch("reportFooterFields")
				.where()
					.eq("id", id)
				.findUnique();
	}

	public static List<AdminDocument> listForExport() {
		return find.select("*")
					.fetch("reportTitleFields")
					.fetch("pageTitleFields")
					.fetch("detailFields")
					.fetch("pageFooterFields")
					.fetch("reportFooterFields")
				.where()
					.eq("isActive", Boolean.TRUE)
				.findList();
	}

	public static String findLastName(String name) {
		if (name != null && ! name.trim().isEmpty()) {
			SqlRow row = Ebean.createSqlQuery("select max(name) as mname from admin_document where name like '"+name+"%' group by name order by name desc").setMaxRows(1).findUnique();
			if (row != null && ! row.isEmpty()) {
				String base = row.getString("mname").replace(name, "");
				int no = NumericUtils.strToInteger(base, 0);
				return name+(no+1);
			}
		}
		return null;
	}

	public static AdminDocument findByName(String name) {
		AdminDocument result = CacheUtils.getByKeyValue(AdminDocument.class, "name", name);

		if (result == null) {
			result = find.where()
							.eq("isActive", Boolean.TRUE)
							.eq("name", name)
						.findUnique();
			if (result != null) CacheUtils.setByKeyValue(AdminDocument.class, "name", name, result);
		}

		return result;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<AdminDocument> el = find.where().eq(field, value);
		if (id != null)
			el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(AdminDocument.class, Right.BELGE_TASARIMI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(AdminDocument.class, Right.BELGE_TASARIMI);
		super.update();
	}

	@Override
	public void delete() {
		CacheUtils.cleanAll(AdminDocument.class, Right.BELGE_TASARIMI);
		super.delete();
	}

	@Override
	public String toString() {
		return name;
	}

}
