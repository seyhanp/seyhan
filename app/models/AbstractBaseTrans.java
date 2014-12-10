/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.joda.time.Days;

import play.data.format.Formats.DateTime;
import play.i18n.Messages;
import utils.CacheUtils;
import utils.DocNoUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import controllers.global.Profiles;
import enums.DocNoIncType;
import enums.Module;
import enums.Right;
import enums.TransType;

@MappedSuperclass
/**
 * @author mdpinar
*/
public abstract class AbstractBaseTrans extends BaseModel {

	private static final long serialVersionUID = 1L;

	public Integer receiptNo;

	@Column(name = "_right")
	public Right right;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date transDate = new Date();

	public String transNo;

	public TransType transType = TransType.Debt;

	public String excCode;
	public Double excRate = 1d;
	public Double excEquivalent;

	@ManyToOne
	public GlobalTransPoint transPoint;

	@ManyToOne
	public GlobalPrivateCode privateCode;

	public Integer transYear;
	public String transMonth;

	public String description;

	/**
	 * Reflection to another module
	 */
	public Module refModule;
	public Integer refId;

	@Transient public Module refOldModule;
	@Transient public Contact refContact;
	@Transient public Safe refSafe;
	@Transient public Bank refBank;

	@Transient public ContactTransSource refContactTransSource;
	@Transient public SafeTransSource refSafeTransSource;
	@Transient public BankTransSource refBankTransSource;

	@Transient public String refExcCode;
	@Transient public Double refExcRate = 1d;
	@Transient public Double refExcEquivalent;

	public AbstractBaseTrans() {
		this.transPoint  = Profiles.chosen().gnel_transPoint;
		this.excCode = Profiles.chosen().gnel_excCode;
	}

	public AbstractBaseTrans(Right right) {
		this();
		this.right = right;

		if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) transNo = DocNoUtils.findLastTransNo(right);
		receiptNo = DocNoUtils.findLastReceiptNo(right);
	}

	@Transient
	public abstract String getTableName();

	@Override
	public Right getAuditRight() {
		return this.right;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.right") + Messages.get(right.key) + " - " +
				Messages.get("audit.receipt_no") + this.receiptNo;
	}

	@Override
	@Transient
	public String checkEditingConstraints() {
		if (! CacheUtils.isSpecialUser()) {
			AdminUserGroup group = CacheUtils.getUser().userGroup;
			if (id != null && group.editingTimeout > 0) {
				org.joda.time.DateTime today =  new org.joda.time.DateTime(new Date());
				org.joda.time.DateTime insertAtDT = new org.joda.time.DateTime(insertAt);
				Days days = Days.daysBetween(insertAtDT, today);

				if (days.getDays() > group.editingTimeout) {
					return Messages.get("editing_timeout.alert", group.editingTimeout);
				}
			}

			org.joda.time.DateTime today =  new org.joda.time.DateTime(new Date());
			org.joda.time.DateTime transDateDT = new org.joda.time.DateTime(transDate);
			Days days = Days.daysBetween(transDateDT, today);
			if (days.getDays() != 0 && ! group.hasEditDifDate) {
				return Messages.get("editing_difdate.alert");
			}
		}

		if (isUsedForElse("trans_no", transNo)) {
			return Messages.get("trans.no") + " " + Messages.get("not.unique", transNo);
		}

		return super.checkEditingConstraints();
	}

	private boolean isUsedForElse(String field, String value) {
		if (value == null || transNo.isEmpty()) return false; 

		String queryStr = "SELECT id FROM " + getTableName() + " WHERE workspace = :workspace AND _right = :right AND " + field + " = :value ";
		if (id != null) queryStr += "AND id <> :id"; 

		SqlQuery query = Ebean.createSqlQuery(queryStr);
		query.setParameter("workspace", CacheUtils.getWorkspaceId());
		query.setParameter("right", right);
		query.setParameter("value", value);
		if (id != null) query.setParameter("id", id);

		SqlRow checkRow = query.findUnique();
		return  (checkRow != null && checkRow.getInteger("id") != null);
	}

}
