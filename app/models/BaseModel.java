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
