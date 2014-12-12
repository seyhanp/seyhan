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

import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.ModelHelper;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class Bank extends BaseModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.BANK_HESAP_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(15)
	public String accountNo;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(50)
	public String name;

	@Constraints.MaxLength(30)
	public String branch;

	@Constraints.MaxLength(20)
	public String city;

	@Constraints.MaxLength(26)
	public String iban;

	@Constraints.MaxLength(3)
	public String excCode;

	public Boolean isActive = Boolean.TRUE;

	public static Map<String, String> options() {
		return ModelHelper.options(RIGHT);
	}

	public static List<Bank> page() {
		return ModelHelper.page(RIGHT, "name");
	}

	public static Bank findById(Integer id) {
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
		return Messages.get("audit.code") + this.accountNo + " - "  + Messages.get("audit.name") + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
