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

import models.AbstractBaseTrans;
import models.BaseModel;

import org.apache.commons.lang3.SerializationUtils;

import play.db.ebean.Model;
import controllers.global.Profiles;
import enums.DocNoIncType;

/**
 * @author mdpinar
*/
public class CloneUtils {

	public static void resetModel(Model model) {
		model._ebean_getIntercept().setLoaded();
		model._ebean_getIntercept().setReference();
		model._ebean_getIntercept().setIntercepting(false);
	}

	public static <T extends Model> T cloneModel(T source) {
		T model = SerializationUtils.clone(source);

		model._ebean_getIntercept().setLoaded();
		model._ebean_getIntercept().setReference();
		model._ebean_getIntercept().setIntercepting(false);

		return model;
	}

	public static <T extends BaseModel> T cloneBaseModel(T source) {
		T model = SerializationUtils.clone(source);

		model.workspace = CacheUtils.getWorkspaceId();
		model.id = null;

		model._ebean_getIntercept().setLoaded();
		model._ebean_getIntercept().setReference();
		model._ebean_getIntercept().setIntercepting(false);

		model.insertBy = null;
		model.insertAt = null;
		model.updateBy = null;
		model.updateAt = null;

		return model;
	}

	public static <T extends AbstractBaseTrans> T cloneTransaction(T source) {
		T model = SerializationUtils.clone(source);

		model.workspace = CacheUtils.getWorkspaceId();
		model.id = null;

		model._ebean_getIntercept().setLoaded();
		model._ebean_getIntercept().setReference();
		model._ebean_getIntercept().setIntercepting(false);

		model.insertBy = null;
		model.insertAt = null;
		model.updateBy = null;
		model.updateAt = null;

		if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) model.transNo = DocNoUtils.findLastTransNo(model.right);
		model.receiptNo = DocNoUtils.findLastReceiptNo(model.right);

		RefModuleUtil.setTransientFields(model);

		return model;
	}

}
