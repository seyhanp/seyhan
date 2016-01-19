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
