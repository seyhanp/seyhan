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
public class NovaposhtaCargo extends BaseModel {

	private static final long serialVersionUID = 1L;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(100)
	public String name;

	@Constraints.MaxLength(30)
	public String responsible;

	@Constraints.MaxLength(15)
	public String phone1;

	@Constraints.MaxLength(15)
	public String phone2;

	@Constraints.MaxLength(150)
	public String address1;

	@Constraints.MaxLength(150)
	public String address2;
	
	public Boolean isActive = Boolean.TRUE;

	public static Map<String, String> options() {
		return ModelHelper.options(Right.NOVAPOSHTA_KARGO_TANITIMI);
	}

	public static List<NovaposhtaCargo> page() {
		return ModelHelper.page(Right.NOVAPOSHTA_KARGO_TANITIMI, "name");
	}

	public static NovaposhtaCargo findById(Integer id) {
		return ModelHelper.findById(Right.NOVAPOSHTA_KARGO_TANITIMI, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(Right.NOVAPOSHTA_KARGO_TANITIMI, field, value, id);
	}

	@Override
	public Right getAuditRight() {
		return Right.NOVAPOSHTA_KARGO_TANITIMI;
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
