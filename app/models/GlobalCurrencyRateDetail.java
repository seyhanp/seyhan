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
