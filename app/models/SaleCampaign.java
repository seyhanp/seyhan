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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import models.search.NameOnlySearchParam;
import models.temporal.Pair;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;
import utils.CacheUtils;
import utils.CookieUtils;
import utils.DateUtils;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import controllers.global.Profiles;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class SaleCampaign extends BaseStockExtraFieldsModel {

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.SATS_KAMPANYA_TANITIMI;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date startDate;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date endDate;

	public Boolean isActive = Boolean.TRUE;

	public Integer priority = 1;

	public Double discountRate1 = 0d;
	public Double discountRate2 = 0d;
	public Double discountRate3 = 0d;

	@ManyToOne
	@JoinColumn(name="stock_category_id")
	public StockCategory category;

	private static Model.Finder<Integer, SaleCampaign> find = new Model.Finder<Integer, SaleCampaign>(Integer.class, SaleCampaign.class);

	public static Page<SaleCampaign> page(NameOnlySearchParam searchParam) {
		ExpressionList<SaleCampaign> expList = find.where()
														.eq("workspace", CacheUtils.getWorkspaceId());

		if (searchParam.name != null && ! searchParam.name.isEmpty()) {
			expList.like("name", searchParam.name + "%");
		}

		Pair sortInfo = CookieUtils.getSortInfo(RIGHT, "name");

		Page<SaleCampaign> page = expList.orderBy(sortInfo.key + " " + sortInfo.value)
										.findPagingList(Profiles.chosen().gnel_pageRowNumber)
										.setFetchAhead(false)
									.getPage(searchParam.pageIndex);

		return page;
	}

	public static SaleCampaign findById(Integer id) {
		return find.where()
				.eq("workspace", CacheUtils.getWorkspaceId())
				.eq("id", id)
			.findUnique();
	}
	
	public static List<SaleCampaign> findActiveCampaigns() {
		return find.order("priority desc, startDate").where()
				.eq("workspace", CacheUtils.getWorkspaceId())
				.eq("isActive", Boolean.TRUE)
				.raw("start_date >= " + DateUtils.formatDateForDB(new Date()))
			.findList();
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<SaleCampaign> el = find.where()
												.eq("workspace", CacheUtils.getWorkspaceId())
												.eq(field, value);
		if (id != null) el.ne("id", id);

		return el.findUnique() != null;
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
