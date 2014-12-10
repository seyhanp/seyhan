/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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
