/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.ebean.Model;
import play.i18n.Messages;
import utils.CacheUtils;
import controllers.admin.Workspaces;
import enums.Right;
import enums.UserEditingLimit;
import enums.UserLogLevel;

/**
 * @author mdpinar
*/
@MappedSuperclass
public abstract class BaseModel extends Model {

	private final static Logger log = LoggerFactory.getLogger(BaseModel.class);
	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Integer workspace;

	public String insertBy;
	public String updateBy;
	public Date insertAt;
	public Date updateAt;

	@Version
	public Integer version;

	public abstract Right getAuditRight();
	public abstract String getAuditDescription();

	/**
	 * Bir trans kaydedilirken baska bir trans uretildiginde kullanilmak uzere
	 */
	public void singleSave() {
		try {
			this.insertBy = CacheUtils.getUser().username;
		} catch (Exception e) {
			if (! getClass().equals(GlobalCurrencyRate.class)) {
				log.error("ERROR", e);
				return;
			}
			this.insertBy = "super";
		}
		this.workspace = CacheUtils.getWorkspaceId();
		this.insertAt = new Date();

		super.save();
		CacheUtils.cleanAll(this.getClass(), getAuditRight());
	}

	/**
	 * Bir trans kaydedilirken baska bir trans uretildiginde kullanilmak uzere
	 */
	public void singleUpdate() {
		try {
			this.updateBy = CacheUtils.getUser().username;
		} catch (Exception e) {
			if (! getClass().equals(GlobalCurrencyRate.class)) {
				return;
			}
			this.updateBy = "super";
		}
		this.workspace = CacheUtils.getWorkspaceId();
		this.updateAt = new Date();

		if (insertBy == null) insertBy = updateBy;
		if (insertAt == null) insertAt = updateAt;

		super.update();
		CacheUtils.cleanAll(this.getClass(), getAuditRight());
	}

	public void singleDelete() {
		super.delete();
		CacheUtils.cleanAll(this.getClass(), getAuditRight());
	}

	@Override
	public void update() {
		singleUpdate();
		new AdminUserAudit(this.updateBy, this.updateAt, getAuditRight(), getAuditDescription(), UserLogLevel.Update).save();
	}

	@Override
	public void save() {
		singleSave();
		new AdminUserAudit(this.insertBy, this.insertAt, getAuditRight(), getAuditDescription(), UserLogLevel.Insert).save();
	}
	
	public void saveForOpening() {
		super.save();
	}

	@Override
	public void delete() {
		singleDelete();
		new AdminUserAudit(CacheUtils.getUser().username, new Date(), getAuditRight(), getAuditDescription(), UserLogLevel.Delete).save();
	}

	@Transient
	public String checkEditingConstraints() {
		if (CacheUtils.getWorkspaceName() == null || CacheUtils.getWorkspaceName().trim().isEmpty()) {
			return Messages.get("firstly.select", Messages.get("workspace"));
		} else {
			AdminWorkspace ws = Workspaces.isRightUserForWS(CacheUtils.getUser());
			if (ws == null) return Messages.get("any.not.found.workspace");
		}
		
		if (this.id == null) return null;
		if (CacheUtils.isSpecialUser()) return null;

		if (CacheUtils.getUser().userGroup.editingLimit.equals(UserEditingLimit.Free)) return null;
		if (insertBy != null && ! insertBy.equals(CacheUtils.getUser().username)) return Messages.get("you.can.edit.only.yours");

		return null;
	}

}
