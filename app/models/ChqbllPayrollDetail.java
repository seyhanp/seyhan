/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.search.ChqbllPartialSearchParam;
import models.search.ChqbllSelectionModel;
import models.temporal.Pair;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class ChqbllPayrollDetail extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Integer workspace;

	public Boolean isCustomer = Boolean.TRUE;
	public ChqbllSort sort = ChqbllSort.Cheque;

	public Integer portfolioNo;
	public Integer rowNo;

	@Constraints.MaxLength(25)
	public String serialNo;

	@ManyToOne
	public ChqbllPayroll trans;

	@ManyToOne
	public ChqbllPayrollSource transSource;

	@ManyToOne
	public ChqbllType cbtype;

	public ChqbllStep lastStep;

	@Constraints.MaxLength(70)
	public String owner;

	@Constraints.MaxLength(30)
	public String paymentPlace;

	@ManyToOne
	public Bank bank;

	@Constraints.MaxLength(15)
	public String bankAccountNo;

	@Constraints.MaxLength(50)
	public String bankName;

	@Constraints.MaxLength(30)
	public String bankBranch;

	@Constraints.MaxLength(30)
	public String correspondentBranch;

	@Constraints.Required
	@DateTime(pattern = "dd/MM/yyyy")
	public Date dueDate;

	@Constraints.Required
	public Double amount;

	public String excCode;
	public Double excRate = 1d;
	public Double excEquivalent;

	@Constraints.MaxLength(100)
	public String description;

	@ManyToOne
	public Contact contact;

	@Constraints.MaxLength(100)
	public String contactName;

	@Constraints.MaxLength(100)
	public String lastContactName;

	public Integer dueYear;
	public String dueMonth;

	@ManyToOne
	public GlobalTransPoint transPoint;

	@ManyToOne
	public GlobalPrivateCode privateCode;

	/*
	 * For surety
	 */

	@Constraints.MaxLength(100)
	public String surety;

	@Constraints.MaxLength(100)
	public String suretyAddress;

	@Constraints.MaxLength(15)
	public String suretyPhone1;

	@Constraints.MaxLength(15)
	public String suretyPhone2;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="detail", orphanRemoval = true)
	public List<ChqbllDetailHistory> histories;

	/*
	 * For partial collections
	 */
	public Double totalPaid = 0d;

	/*
	 * Trans fisi detayi ile baglanti icin kullanilacak, 'trans detail id' bilgisi
	 */
	@Transient
	public Integer transDetailId;

	/*------------------------------------------------------------------------------------*/

	private static Model.Finder<Integer, ChqbllPayrollDetail> find = new Model.Finder<Integer, ChqbllPayrollDetail>(Integer.class, ChqbllPayrollDetail.class);

	public static ChqbllPayrollDetail findById(Integer id) {
		return find.where()
				.eq("workspace", CacheUtils.getWorkspaceId())
				.eq("id", id)
			.findUnique();
	}

	public static ChqbllPayrollDetail getDetailedById(Integer id) {
		ChqbllPayrollDetail result = findById(id);
		result.histories =  ChqbllDetailHistory.findHistoryList(result, 100);

		return result;
	}

	public static List<ChqbllPayrollDetail> getListBySearchModel(ChqbllSelectionModel searchParam) {
		ExpressionList<ChqbllPayrollDetail> elList = find.where();

		elList.eq("workspace", CacheUtils.getWorkspaceId());
		elList.eq("sort", searchParam.selSort);
		elList.eq("lastStep", searchParam.step);

		if (searchParam.refContact != null && searchParam.refContact.id != null) {
			elList.eq("contact", searchParam.refContact);
		}
		if (searchParam.selPortfolioNo != null && searchParam.selPortfolioNo > 0) {
			elList.eq("portfolioNo", searchParam.selPortfolioNo);
		}
		if (searchParam.selSerialNo != null && ! searchParam.selSerialNo.trim().isEmpty()) {
			elList.like("serialNo", searchParam.selSerialNo);
		}
		if (searchParam.startDate != null) {
			elList.ge("dueDate", searchParam.startDate);
		}
		if (searchParam.endDate != null) {
			elList.le("dueDate", searchParam.endDate);
		}
		if (searchParam.alreadySelected != null && ! searchParam.alreadySelected.trim().isEmpty()) {
			elList.not(Expr.in("id", searchParam.alreadySelected.split(",")));
		}

		return elList.findList();
	}

	public static Page<ChqbllPayrollDetail> page(ChqbllPartialSearchParam searchParam, Right right) {
		ExpressionList<ChqbllPayrollDetail> expList = find.where();

		expList.eq("workspace", CacheUtils.getWorkspaceId());
		expList.eq("sort", searchParam.sort);
		expList.eq("isCustomer", ChqbllStep.isCustomer(right));

		if (searchParam.fullText != null && ! searchParam.fullText.isEmpty()) {
			expList.or(
				Expr.like("contactName", "%" + searchParam.fullText + "%"),
				Expr.or(
						Expr.like("serialNo", "%" + searchParam.fullText + "%"),
						Expr.like("description", "%" + searchParam.fullText + "%")
					)
			);
		} else {
			if (searchParam.step != null) {
				expList.eq("lastStep", searchParam.step);
			} else {
				List<ChqbllStep> stepList = new ArrayList<ChqbllStep>();
				if (ChqbllStep.isCustomer(right)) {
					stepList.add(ChqbllStep.PartCollection);
					stepList.add(ChqbllStep.InPortfolio);
				} else {
					stepList.add(ChqbllStep.PartPayment);
					stepList.add(ChqbllStep.Issued);
				}
				expList.in("lastStep", stepList);
			}
			if (searchParam.balanceOpts != null) {
				if (searchParam.balanceOpts) {
					expList.raw("amount > totalPaid");
				} else {
					expList.raw("amount = totalPaid");
				}
			}

			if (searchParam.cbtype != null && searchParam.cbtype.id != null) {
				expList.ge("cbtype", searchParam.cbtype);
			}
			if (searchParam.startDate != null) {
				expList.ge("dueDate", searchParam.startDate);
			}
			if (searchParam.endDate != null) {
				expList.le("dueDate", searchParam.endDate);
			}

			if (searchParam.serialNo != null) {
				expList.like("serialNo", searchParam.serialNo + "%");
			}
			if (searchParam.portfolioNo != null) {
				expList.eq("portfolioNo", searchParam.portfolioNo);
			}
		}

		Pair sortInfo = CookieUtils.getSortInfo(right, "portfolioNo", "desc");

		Page<ChqbllPayrollDetail> page = expList.orderBy(sortInfo.key + " " + sortInfo.value)
										.findPagingList(Profiles.chosen().gnel_pageRowNumber)
										.setFetchAhead(false)
									.getPage(searchParam.pageIndex);

		return page;
	}

	@Override
	public void save() {
		this.workspace = CacheUtils.getWorkspaceId();
		super.save();
	}

	@Override
	public void update() {
		this.workspace = CacheUtils.getWorkspaceId();
		super.update();
	}

}
