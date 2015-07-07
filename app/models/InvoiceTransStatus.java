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

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;

import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.ModelHelper;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class InvoiceTransStatus extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.FATR_FATURA_DURUMLARI;
	private static final String ORDERING = "ordering, parent, name";

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	@ManyToOne
	public InvoiceTransStatus parent;

	public Integer ordering = 0;

	public Boolean isActive = Boolean.TRUE;

	public static Map<String, String> options(Integer ownId) {
		Expression expr = null;
		if (ownId != null && ownId.intValue() > 0) {
			expr = Expr.or(Expr.isNull("parent.id"), Expr.and(Expr.ne("id", ownId), Expr.ne("parent.id", ownId)));
		}
		return ModelHelper.expOptions(RIGHT, expr, ORDERING);
	}

	public static Map<String, String> childOptions(Integer oldStatusId) {
		return ModelHelper.expOptions(RIGHT, Expr.eq("parent.id", (oldStatusId != null && oldStatusId.intValue() > 0 ? oldStatusId : null)), ORDERING);
	}

	public static Map<String, String> options(boolean isFirstStep) {
		return ModelHelper.expOptions(RIGHT, (isFirstStep ? Expr.isNull("parent") : null), ORDERING);
	}

	public static List<InvoiceTransStatus> page() {
		return ModelHelper.orderedPage(RIGHT, ORDERING);
	}

	public static InvoiceTransStatus findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id);
	}

	@Override
	public Right getAuditRight() {
		return RIGHT;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.name") + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
