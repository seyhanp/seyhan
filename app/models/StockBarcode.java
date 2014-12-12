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

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;

import com.avaje.ebean.ExpressionList;

@Entity
/**
 * @author mdpinar
*/
public class StockBarcode extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Integer workspace;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(50)
	public String barcode;

	@Constraints.MaxLength(30)
	public String prefix;

	@Constraints.MaxLength(30)
	public String suffix;

	public Integer unitNo = 1;

	public boolean isPrimary = false;

	@ManyToOne
	public Stock stock;

	private static Model.Finder<Integer, StockBarcode> find = new Model.Finder<Integer, StockBarcode>(Integer.class, StockBarcode.class);

	public StockBarcode(StockBarcode other) {
		this(other.barcode, other.prefix, other.suffix);
		this.id = other.id;
	}

	public StockBarcode(String primaryBarcode) {
		this(primaryBarcode, null, null);
		this.isPrimary = true;
	}

	public StockBarcode(String barcode, String prefix, String suffix) {
		this.barcode = barcode;
		this.prefix = prefix;
		this.suffix = suffix;
		this.unitNo = 1;
		this.workspace = CacheUtils.getWorkspaceId();
	}

	public static boolean areUsedForElse(Set<String> value, Integer id) {
		ExpressionList<StockBarcode> el = find.where()
												.eq("workspace", CacheUtils.getWorkspaceId())
												.in("barcode", value);
		if (id != null) el.ne("stock.id", id);

		return el.findUnique() != null;
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

	@Override
	public String toString() {
		return barcode;
	}

}
