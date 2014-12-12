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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.search.NameOnlySearchParam;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.DateUtils;
import utils.ModelHelper;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.CostingType;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class StockCosting extends BaseStockExtraFieldsModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.STOK_MALIYET_HESAPLAMALARI;

	@Constraints.MaxLength(30)
	public String name;

	@Constraints.MaxLength(100)
	public String properties;

	@DateTime(pattern = "dd/MM/yyyy HH:mm")
	public Date execDate;

	@Constraints.Required
	@DateTime(pattern = "dd/MM/yyyy")
	public Date calcDate = new Date();

	public CostingType costingType = CostingType.Weighted;

	public String providerCode;

	@ManyToOne
	public Stock stock;

	@ManyToOne
	public GlobalTransPoint transPoint;

	@ManyToOne
	public StockCategory category;

	@ManyToOne
	public StockDepot depot;

	public Boolean isActive = Boolean.TRUE;

	public static Page<StockCosting> page(NameOnlySearchParam searchParam) {
		ExpressionList<StockCosting> expList = ModelHelper.getExpressionList(RIGHT);

		if (searchParam.name != null && ! searchParam.name.isEmpty()) {
			expList.like("name", searchParam.name + "%");
		}

		return ModelHelper.getPage(RIGHT, expList, "properties", searchParam, false);
	}

	public static Map<String, String> options() {
		return options(null);
	}

	public static Map<String, String> optionsForAVG() {
		List<CostingType> typeList = new ArrayList<CostingType>();
		typeList.add(CostingType.Simple);
		typeList.add(CostingType.Weighted);
		typeList.add(CostingType.Moving);

		return options(typeList);
	}

	public static Map<String, String> optionsForxFO() {
		List<CostingType> typeList = new ArrayList<CostingType>();
		typeList.add(CostingType.FIFO);
		typeList.add(CostingType.LIFO);

		return options(typeList);
	}

	private static Map<String, String> options(List<CostingType> typeList) {
		if (typeList == null) {
			return ModelHelper.expOptions(RIGHT, null);
		} else {
			return ModelHelper.expOptions(RIGHT, Expr.in("costingType", typeList));
		}
	}

	@Transient
	private void buildProperties() {
		StringBuilder sb = new StringBuilder();
		if (this.execDate != null) {
			sb.append(DateUtils.formatDate(this.execDate, "yyyy/MM/dd HH:mm"));
		}
		sb.append(" - [");
		sb.append(Messages.get(this.costingType.key));
		sb.append("] - ( ");
		sb.append(this.name );
		sb.append(" )");

		this.properties = sb.toString();
	}

	public static StockCosting findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id);
	}

	@Override
	public void save() {
		buildProperties();
		super.save();
	}

	@Override
	public void update() {
		buildProperties();
		super.update();
	}

	@Override
	public Right getAuditRight() {
		return RIGHT;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.name") + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
