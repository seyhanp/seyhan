/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
/**
 * @author mdpinar
*/
public class GlobalCurrencyRateDetail extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(3)
	public String code;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(25)
	public String name;

	@Constraints.Required
	@Column(name = "_date")
	@DateTime(pattern = "dd/MM/yyyy")
	public Date date;

	public Double buying = 1d;
	public Double selling = 1d;

	@ManyToOne
	public GlobalCurrencyRate currencyRate;

	public GlobalCurrencyRateDetail() {
		;
	}

	public GlobalCurrencyRateDetail(GlobalCurrencyRateDetail other) {
		this.id = other.id;
		this.code = other.code;
		this.name = other.name;
		this.date = other.date;
		this.buying = other.buying;
		this.selling = other.selling;
	}

	public GlobalCurrencyRateDetail(String code, Double buying, Double selling) {
		this.code = code;
		this.buying = buying;
		this.selling = selling;
	}

	public GlobalCurrencyRateDetail(String code, String name) {
		super();
		this.code = code;
		this.name = name;
	}

}
