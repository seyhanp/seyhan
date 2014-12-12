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
package utils;

import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.StockCategory;

/**
 * @author mdpinar
*/
public class InstantSQL {

	public static String buildTransPointSQL(Integer pointId) {
		GlobalTransPoint model = GlobalTransPoint.findById(pointId);

		if (model != null) {

			String level = null;
			if (model.par1Id == null) {
				level = "par1id";
			} else if (model.par2Id == null) {
				level = "par2id";
			} else if (model.par3Id == null) {
				level = "par3id";
			} else if (model.par4Id == null) {
				level = "par4id";
			} else if (model.par5Id == null) {
				level = "par5id";
			}

			return " inner join global_trans_point tp on tp.id = t.trans_point_id "
					+ " and (t.trans_point_id = " + model.id
					+ (level != null ? " or tp." + level + " = " + model.id : "")
					+ ")";
		}

		return null;
	}

	public static String buildPrivateCodeSQL(Integer privateId) {
		GlobalPrivateCode model = GlobalPrivateCode.findById(privateId);

		if (model != null) {

			String level = null;
			if (model.par1Id == null) {
				level = "par1id";
			} else if (model.par2Id == null) {
				level = "par2id";
			} else if (model.par3Id == null) {
				level = "par3id";
			} else if (model.par4Id == null) {
				level = "par4id";
			} else if (model.par5Id == null) {
				level = "par5id";
			}

			return " inner join global_private_code pc on pc.id = t.private_code_id "
					+ " and (t.private_code_id = " + model.id
					+ (level != null ? " or pc." + level + " = " + model.id : "")
					+ ")";
		}

		return null;
	}

	public static String buildCategorySQL(Integer catId) {
		if (catId == null) return "";

		StockCategory model = StockCategory.findById(catId);

		if (model != null) {

			String level = null;
			if (model.par1Id == null) {
				level = "par1id";
			} else if (model.par2Id == null) {
				level = "par2id";
			} else if (model.par3Id == null) {
				level = "par3id";
			} else if (model.par4Id == null) {
				level = "par4id";
			} else if (model.par5Id == null) {
				level = "par5id";
			}

			if (level == null) {
				return " and s.category_id = " + catId;
			} else {
				return " and s.category_id in (select id from stock_category sc where sc.workspace = " + CacheUtils.getWorkspaceId() + " and sc.id = " + catId + " or sc." + level + " = " + model.id + ")";
			}
		}

		return null;
	}

}
