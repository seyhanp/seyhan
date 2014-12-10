/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import enums.Right;
import enums.TransType;

@MappedSuperclass
/**
 * @author mdpinar
*/
public abstract class AbstractStockTransDetail extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Integer workspace;

	@Constraints.Required
	public Integer receiptNo;

	@Column(name = "_right")
	@Constraints.Required
	public Right right;

	@ManyToOne
	public Stock stock;

	@Constraints.Required
	@DateTime(pattern = "dd/MM/yyyy")
	public Date transDate = new Date();

	@DateTime(pattern = "dd/MM/yyyy")
	public Date deliveryDate;

	public TransType transType = TransType.Input;

	@ManyToOne
	public StockDepot depot;

	@ManyToOne
	public Contact contact;

	@ManyToOne
	public GlobalTransPoint transPoint;

	@ManyToOne
	public GlobalPrivateCode privateCode;

	public Integer rowNo;

	@Constraints.MaxLength(100)
	public String name;

	@Constraints.Required
	public Double quantity;

	@Constraints.Required
	@Constraints.MaxLength(6)
	public String unit;
	public Double unitRatio;

	public Double basePrice;

	@Constraints.Required
	public Double price;

	@Constraints.Required
	public Double taxRate;

	public Double discountRate1 = 0d;
	public Double discountRate2 = 0d;
	public Double discountRate3 = 0d;

	@Constraints.Required
	public Double amount;

	public Double taxAmount = 0d;
	public Double discountAmount = 0d;

	@Constraints.Required
	public Double total;

	@Constraints.MaxLength(100)
	public String description;

	public Integer transYear; 
	public String transMonth;

	@Constraints.MaxLength(6) public String unit1;
	@Constraints.MaxLength(6) public String unit2;
	@Constraints.MaxLength(6) public String unit3;
	public Double unit2Ratio;
	public Double unit3Ratio;

	public Double input = 0d;
	public Double output = 0d;
	public Double inTotal = 0d;
	public Double outTotal = 0d;

	public Double netInput = 0d;
	public Double netOutput = 0d;
	public Double netInTotal = 0d;
	public Double netOutTotal = 0d;

	public String excCode;
	public Double excRate = 1d;
	public Double excEquivalent;

	@ManyToOne
	public SaleSeller seller;

	public Double plusFactorAmount = 0d;
	public Double minusFactorAmount = 0d;

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
