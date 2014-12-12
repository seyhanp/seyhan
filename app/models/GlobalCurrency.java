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
package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.ExpressionList;

import controllers.global.Profiles;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class GlobalCurrency extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(3)
	public String code;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(25)
	public String name;

	public Boolean isActive = Boolean.TRUE;

	@Version
	public Integer version;

	private static Model.Finder<Integer, GlobalCurrency> find = new Model.Finder<Integer, GlobalCurrency>(Integer.class, GlobalCurrency.class);

	public static List<String> options() {
		List<String> options = CacheUtils.getListOptions(GlobalCurrency.class);
		if (options != null) return options;

		String baseCurrency = Profiles.chosen().gnel_excCode;

		List<GlobalCurrency> modelList = find.where()
												.ne("code", baseCurrency)
												.eq("isActive", Boolean.TRUE)
											.orderBy("code")
										.findList();

		options = new ArrayList<String>();
		options.add(baseCurrency);
		for(GlobalCurrency model: modelList) {
			options.add(model.code);
		}

		if (options.size() > 0) CacheUtils.setListOptions(GlobalCurrency.class, options);

		return options;
	}

	public static List<GlobalCurrency> page() {
		Pair sortInfo = CookieUtils.getSortInfo(Right.GNEL_DOVIZ_BIRIMLERI, "isActive", "desc");
		return find.orderBy(sortInfo.key + " " + sortInfo.value).findList();
	}

	public static List<GlobalCurrency> getAll() {
		List<GlobalCurrency> result = CacheUtils.getListAll(GlobalCurrency.class);
		if (result != null) return result;

		result = find
					.where()
						.eq("isActive", Boolean.TRUE)
					.orderBy("code")
				.findList();

		if (result != null && result.size() > 0) CacheUtils.setListAll(GlobalCurrency.class, result);

		return result;
	}

	public static GlobalCurrency findById(Integer id) {
		GlobalCurrency result = CacheUtils.getById(GlobalCurrency.class, id);

		if (result == null) {
			result = find.byId(id);
			if (result != null) CacheUtils.setById(GlobalCurrency.class, id, result);
		}

		return result;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<GlobalCurrency> el = find.where().eq(field, value);
		if (id != null) el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public String toString() {
		return code;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(GlobalCurrency.class, Right.GNEL_DOVIZ_BIRIMLERI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(GlobalCurrency.class, Right.GNEL_DOVIZ_BIRIMLERI);
		super.update();
	}
	
	@Override
	public void delete() {
		CacheUtils.cleanAll(GlobalCurrency.class, Right.GNEL_DOVIZ_BIRIMLERI);
		super.delete();
	}

}
