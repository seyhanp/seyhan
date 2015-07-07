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

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints;
import utils.ModelHelper;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;

import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class StockExtraFields extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.STOK_EKSTRA_ALANLAR;

	@ManyToOne
	public AdminExtraFields extraFields;

	@Constraints.Required
	@Constraints.MinLength(2)
	@Constraints.MaxLength(30)
	public String name;

	public Boolean isActive = Boolean.TRUE;

	public static int getRowCount() {
		return ModelHelper.getRowCount(RIGHT);
	}

	public static Map<String, String> options(Integer extraFieldsId) {
		return ModelHelper.expOptions(RIGHT, Expr.eq("extraFields.id", extraFieldsId), "name");
	}

	public static List<StockExtraFields> page(AdminExtraFields extraFields) {
		return ModelHelper.page(RIGHT, "name", Expr.eq("extraFields", extraFields));
	}

	public static StockExtraFields findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id, AdminExtraFields extraFields) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id, Expr.eq("extraFields", extraFields));
	}

	public static void deleteAll(AdminExtraFields extraFields) {
		Ebean.createSqlUpdate("delete from contact_extra_fields where extra_fields_id = " + extraFields.id).execute();
	}

	@Override
	public Right getAuditRight() {
		return RIGHT;
	}

	@Override
	public String getAuditDescription() {
		return this.extraFields.name + " : " + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
