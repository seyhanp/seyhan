/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import enums.ChqbllSort;

@Entity
/**
 * @author mdpinar
*/
public class ChqbllDetailPartial extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Boolean isCustomer = Boolean.TRUE;
	public ChqbllSort sort = ChqbllSort.Cheque;

	@ManyToOne
	public ChqbllPayrollDetail detail;

	@ManyToOne
	public Safe safe;

	@ManyToOne(cascade = CascadeType.ALL)
	public SafeTrans trans;

	@Constraints.Required
	@DateTime(pattern = "dd/MM/yyyy")
	public Date transDate = new Date();

	@Constraints.Required
	public Double amount;

	public String excCode;
	public Double excRate = 0d;
	public Double excEquivalent;

	@Constraints.MaxLength(100)
	public String description;

	public String insertBy;
	public Date insertAt;

	/*------------------------------------------------------------------------------------*/

	private static Model.Finder<Integer, ChqbllDetailPartial> find = new Model.Finder<Integer, ChqbllDetailPartial>(Integer.class, ChqbllDetailPartial.class);

	public static List<ChqbllDetailPartial> findList(ChqbllPayrollDetail det) {
		return find.where().eq("detail", det).findList();
	}

	public static void delById(Integer id) {
		ChqbllDetailPartial part = find.byId(id);
		if (part != null) part.delete();
	}

}
