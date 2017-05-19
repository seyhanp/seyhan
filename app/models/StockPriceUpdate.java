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

import models.search.NameOnlySearchParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.i18n.Messages;
import utils.CacheUtils;
import utils.GlobalCons;
import utils.ModelHelper;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.avaje.ebean.SqlRow;

import controllers.global.Profiles;
import enums.EffectDirection;
import enums.EffectType;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class StockPriceUpdate extends BaseStockExtraFieldsModel {

	private final static Logger log = LoggerFactory.getLogger(StockPriceUpdate.class);

	private static final long serialVersionUID = 1L;
	private static final Right RIGHT = Right.STOK_FIYAT_GUNCELLEME;

	@DateTime(pattern = "dd/MM/yyyy HH:mm")
	public Date execDate = new Date();

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public StockCategory category;
	public String providerCode;

	public Boolean buyPrice = Boolean.TRUE;
	public Boolean sellPrice = Boolean.TRUE;

	public EffectType effectType = EffectType.Percent;
	public EffectDirection effectDirection = EffectDirection.Increase;

	@Constraints.Required
	public Double effect;

	@Constraints.MaxLength(50)
	public String description;

	public static Page<StockPriceUpdate> page(NameOnlySearchParam searchParam) {
		ExpressionList<StockPriceUpdate> expList = ModelHelper.getExpressionList(RIGHT);

		if (searchParam.name != null && ! searchParam.name.isEmpty()) {
			expList.like("name", searchParam.name + "%");
		}

		return ModelHelper.getPage(RIGHT, expList, "id", searchParam, false);
	}

	public static StockPriceUpdate findById(Integer id) {
		return ModelHelper.findById(RIGHT, id);
	}

	public static StockPriceUpdate findLastOne() {
		Integer id = null;
		SqlRow idRow = Ebean.createSqlQuery("select max(id) as maxId from stock_price_update where workspace = " + CacheUtils.getWorkspaceId()).findUnique();
		if (idRow != null) {
			id = idRow.getInteger("maxId");
		}

		if (id != null) {
			return findById(id);
		}

		return null;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		return ModelHelper.isUsedForElse(RIGHT, field, value, id);
	}

	@Override
	public void delete() {
		Ebean.beginTransaction();
		try {
			/**
			 * Yedeklerden eski fiyatlar donulur
			 */
			if (GlobalCons.dbVendor.equals("h2")) {
				/*
				 * H2 gibi coklu tablo update destegi olmayan veritabanlari icin bu kisim kullanilir
				 */
				List<SqlRow> pudList = Ebean.createSqlQuery("select * from stock_price_update_detail where price_update_id = " + this.id).findList();
				if (pudList != null && pudList.size() > 0) {
					String query = "update stock " +
									"set buy_price = :buy_price, sell_price1 = :sell_price " +
									"where id = :id ";
					for (SqlRow pud : pudList) {
						Ebean.createSqlUpdate(query)
							.setParameter("buy_price", pud.getDouble("buy_price"))
							.setParameter("sell_price", pud.getDouble("sell_price"))
							.setParameter("id", pud.getInteger("stock_id")
						).execute();
					}
				}
			} else if (GlobalCons.dbVendor.equals("mysql")) {
				Ebean.createSqlUpdate(
					"update stock as s, stock_price_update_detail as pud " +
					"set s.buy_price = pud.buy_price, s.sell_price = pud.sell_price " +
					"where s.workspace = " + CacheUtils.getWorkspaceId() +
					"  and pud.price_update_id = " + this.id +
					"  and s.id = pud.stock_id "
				).execute();
			} else { //postgresql, mssql...
				Ebean.createSqlUpdate(
					"update stock " +
					"set buy_price = pud.buy_price, sell_price = pud.sell_price " +
					"from stock_price_update_detail as pud " +
					"where stock.workspace = " + CacheUtils.getWorkspaceId() +
					"  and pud.price_update_id = " + this.id +
					"  and stock.id = pud.stock_id "
				).execute();
			}

			/**
			 * Donulen yedekler silinir
			 */
			Ebean.createSqlUpdate("delete from stock_price_update_detail where price_update_id = :price_update_id")
					.setParameter("price_update_id", this.id)
				.execute();

			/**
			 * Baslik kaydi silinir
			 */
			super.delete();
			Ebean.commitTransaction();

		} catch (Exception e) {
			Ebean.rollbackTransaction();
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void save() {
		Ebean.beginTransaction();
		try {
			super.save();

			/**
			 * Stok fiyatlarinin islemden once yedekleri alinir (geri al islemini desteklemek icin)
			 */
			StringBuilder querySB = new StringBuilder();
			querySB.append("insert into stock_price_update_detail (price_update_id, stock_id, buy_price, sell_price) ");
			querySB.append("select " + this.id + ", id, buy_price, sell_price ");
			querySB.append("from stock as s where s.is_active = :active");

			if (this.providerCode != null && ! this.providerCode.isEmpty()) querySB.append(" and provider_code = '" + this.providerCode + "'");

			if (this.providerCode != null && ! this.providerCode.isEmpty()) querySB.append(" and provider_code = '" + this.providerCode + "'");
			if (this.extraField0 != null && this.extraField0.id != null) querySB.append(" and extra_field0_id = " + this.extraField0.id);
			if (this.extraField1 != null && this.extraField1.id != null) querySB.append(" and extra_field1_id = " + this.extraField1.id);
			if (this.extraField2 != null && this.extraField2.id != null) querySB.append(" and extra_field2_id = " + this.extraField2.id);
			if (this.extraField3 != null && this.extraField3.id != null) querySB.append(" and extra_field3_id = " + this.extraField3.id);
			if (this.extraField4 != null && this.extraField4.id != null) querySB.append(" and extra_field4_id = " + this.extraField4.id);
			if (this.extraField5 != null && this.extraField5.id != null) querySB.append(" and extra_field5_id = " + this.extraField5.id);
			if (this.extraField6 != null && this.extraField6.id != null) querySB.append(" and extra_field6_id = " + this.extraField6.id);
			if (this.extraField7 != null && this.extraField7.id != null) querySB.append(" and extra_field7_id = " + this.extraField7.id);
			if (this.extraField8 != null && this.extraField8.id != null) querySB.append(" and extra_field8_id = " + this.extraField8.id);
			if (this.extraField9 != null && this.extraField9.id != null) querySB.append(" and extra_field9_id = " + this.extraField9.id);

			Ebean.createSqlUpdate(querySB.toString()).setParameter("active", Boolean.TRUE).execute();

			/**
			 * Fiyat degisikligi yapilir
			 */

			querySB.setLength(0);
			querySB.append("update stock set is_active = is_active ");

			if (this.buyPrice != null && this.buyPrice) querySB.append(getUpdateString("buy_price"));
			if (this.sellPrice != null && this.sellPrice) querySB.append(getUpdateString("sell_price"));

			querySB.append(" where workspace = " + CacheUtils.getWorkspaceId() + " and is_active = :active ");

			if (this.providerCode != null && ! this.providerCode.isEmpty()) querySB.append(" and provider_code = '" + this.providerCode + "'");

			if (this.providerCode != null && ! this.providerCode.isEmpty()) querySB.append(" and provider_code = '" + this.providerCode + "'");
			if (this.extraField0 != null && this.extraField0.id != null) querySB.append(" and extra_field0_id = " + this.extraField0.id);
			if (this.extraField1 != null && this.extraField1.id != null) querySB.append(" and extra_field1_id = " + this.extraField1.id);
			if (this.extraField2 != null && this.extraField2.id != null) querySB.append(" and extra_field2_id = " + this.extraField2.id);
			if (this.extraField3 != null && this.extraField3.id != null) querySB.append(" and extra_field3_id = " + this.extraField3.id);
			if (this.extraField4 != null && this.extraField4.id != null) querySB.append(" and extra_field4_id = " + this.extraField4.id);
			if (this.extraField5 != null && this.extraField5.id != null) querySB.append(" and extra_field5_id = " + this.extraField5.id);
			if (this.extraField6 != null && this.extraField6.id != null) querySB.append(" and extra_field6_id = " + this.extraField6.id);
			if (this.extraField7 != null && this.extraField7.id != null) querySB.append(" and extra_field7_id = " + this.extraField7.id);
			if (this.extraField8 != null && this.extraField8.id != null) querySB.append(" and extra_field8_id = " + this.extraField8.id);
			if (this.extraField9 != null && this.extraField9.id != null) querySB.append(" and extra_field9_id = " + this.extraField9.id);

			Ebean.createSqlUpdate(querySB.toString()).setParameter("active", Boolean.TRUE).execute();
			Ebean.commitTransaction();

		} catch (Exception e) {
			Ebean.rollbackTransaction();
			log.error(e.getMessage(), e);
		}
	}

	private String getUpdateString(String field) {
		StringBuilder subSB = new StringBuilder(", ");

		subSB.append(field);
		subSB.append(" = ");
		subSB.append("ROUND(");
		subSB.append(field);

		if (this.effectDirection.equals(EffectDirection.Increase)) {
			subSB.append(" + ");
		} else {
			subSB.append(" - ");
		}

		if (this.effectType.equals(EffectType.Amount)) {
			subSB.append(this.effect);
		} else {
			subSB.append("(");
			subSB.append(field);
			subSB.append(" * ");
			subSB.append(this.effect);
			subSB.append(") / 100");
		}
		subSB.append(", " + Profiles.chosen().gnel_pennyDigitNumber + ")");

		return subSB.toString();
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
