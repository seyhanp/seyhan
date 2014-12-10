/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package documents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.GlobalCurrencyRateDetail;
import play.i18n.Messages;
import utils.Format;
import utils.StringUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import controllers.global.CurrencyRates;
import controllers.global.Profiles;
import enums.Module;

/**
 * @author mdpinar
*/
public class Tables {

	/**
	 * Doviz kuru tablosunu (model 1) String List olarak doner
	 * 
	 * @param label - tablonun hemen ustunde cikacak olan etiket
	 * @return List<String>
	 * 
	 * Döviz Kuru Tablosu
	 * -----------------------------
	 * |           Alış |    Satış |
	 * -----------------------------
	 * | EUR |   2.8919 |   2.8971 |
	 * | USD |   2.1234 |   2.1272 |
	 * -----------------------------
	 */
	public static List<String> getExchangeTable1(String label) {
		final int LENGTH = 29;

		List<String> result = null;
		Map<String, GlobalCurrencyRateDetail> rates = CurrencyRates.getActualExchangeRatesMap();

		StringBuilder line = new StringBuilder(LENGTH);
		String curFormat = Messages.get("formats.exchange");

		if (rates != null && rates.size() > 0) {
			result = new ArrayList<String>();

			result.add(label);
			result.add(StringUtils.fill("-", LENGTH));

			line.append("|");
			line.append(StringUtils.padLeft(Messages.get("buying"), 15));
			line.append(" | ");
			line.append(StringUtils.padLeft(Messages.get("selling"), 8));
			line.append(" |");
			result.add(line.toString());

			result.add(StringUtils.fill("-", LENGTH));
			for (Entry<String, GlobalCurrencyRateDetail> entry : rates.entrySet()) {
				if (entry.getKey().equals("info") || entry.getKey().equals(Profiles.chosen().gnel_excCode)) continue;

				line.setLength(0);
				line.append("| ");
				line.append(StringUtils.padRight(entry.getKey(), 3));
				line.append(" | ");
				line.append(Format.asDouble(entry.getValue().buying, curFormat, 8));
				line.append(" | ");
				line.append(Format.asDouble(entry.getValue().selling, curFormat, 8));
				line.append(" |");
				result.add(line.toString());
			}
			result.add(StringUtils.fill("-", LENGTH));
		}
		return result;
	}

	/**
	 * KDV dagilim tablosunu (model 1) String List olarak doner
	 * 
	 * @param label - tablonun hemen ustunde cikacak olan etiket
	 * @return List<String>
	 * 
	 * KDV Dagilim Tablosu
	 * --------------------------------------
	 * | Oran |       Matrah |        Tutar |
	 * --------------------------------------
	 * | % 18 |       935.75 |       168.66 |
	 * --------------------------------------
	 */
	public static List<String> getTaxTable1(String label, Module module, Integer id) {
		final int LENGTH = 38;

		List<String> result = null;

		StringBuilder line = new StringBuilder(LENGTH);
		String rateFormat = Messages.get("formats.rate");
		String curFormat = Messages.get("formats.currency");

		String query = "select tax_rate, basis, amount from " + module.name() + "_trans_tax where trans_id = " + id + " order by tax_rate";
		List<SqlRow> rows = Ebean.createSqlQuery(query).findList();
		if (rows != null && rows.size() > 0) {
			result = new ArrayList<String>();

			result.add(label);
			result.add(StringUtils.fill("-", LENGTH));

			line.append("|");
			line.append(StringUtils.padLeft(Messages.get("rate"), 5));
			line.append(" | ");
			line.append(StringUtils.padLeft(Messages.get("basis"), 12));
			line.append(" | ");
			line.append(StringUtils.padLeft(Messages.get("amount"), 12));
			line.append(" |");
			result.add(line.toString());
			
			result.add(StringUtils.fill("-", LENGTH));
			for(SqlRow row: rows) {
				line.setLength(0);
				line.append("| ");
				line.append("% " + Format.asDouble(row.getDouble("tax_rate"), rateFormat, 2));
				line.append(" | ");
				line.append(Format.asDouble(row.getDouble("basis"), curFormat, 12));
				line.append(" | ");
				line.append(Format.asDouble(row.getDouble("amount"), curFormat, 12));
				line.append(" |");
				result.add(line.toString());
			}
			result.add(StringUtils.fill("-", LENGTH));
		}
		return result;
	}

	/**
	 * Doviz dagilim tablosunu (model 1) String List olarak doner
	 * 
	 * @param label - tablonun hemen ustunde cikacak olan etiket
	 * @return List<String>
	 * 
	 * Doviz Dagilim Tablosu
	 * --------------------
	 * |     TUTARLAR     |
	 * --------------------
	 * |       935.75 TL  |
	 * --------------------
	 */
	public static List<String> getCurrencyTable1(String label, Module module, Integer id) {
		final int LENGTH = 20;
		
		List<String> result = null;
		
		StringBuilder line = new StringBuilder(LENGTH);
		String curFormat = Messages.get("formats.currency");
		
		String query = "select currency, amount from " + module.name() + "_trans_currency where trans_id = " + id + " order by currency";
		List<SqlRow> rows = Ebean.createSqlQuery(query).findList();
		if (rows != null && rows.size() > 0) {
			result = new ArrayList<String>();
			
			result.add(label);
			result.add(StringUtils.fill("-", LENGTH));
			
			line.append("| ");
			line.append(StringUtils.padCenter(Messages.get("amounts"), 16));
			line.append(" |");
			result.add(line.toString());
			
			result.add(StringUtils.fill("-", LENGTH));
			for(SqlRow row: rows) {
				line.setLength(0);
				line.append("| ");
				line.append(Format.asDouble(row.getDouble("amount"), curFormat, 12));
				line.append(" " + StringUtils.padRight(row.getString("currency"), 3));
				line.append(" |");
				result.add(line.toString());
			}
			result.add(StringUtils.fill("-", LENGTH));
		}
		return result;
	}
	
	/**
	 * Faktor dagilim tablosunu (model 1) String List olarak doner
	 * 
	 * @param label - tablonun hemen ustunde cikacak olan etiket
	 * @return List<String>
	 *
	 * Faktörler Tablosu
	 * ---------------------------------------------------------
	 * | Adı             | Etki Tipi |     Etki |        Tutar |
	 * ---------------------------------------------------------
	 * | HAMALİYE        | Tutar     |    60.00 |        60.00 |
	 * | KARGO           | Yüzde     |     3.00 |        28.07 |
	 * ---------------------------------------------------------
	 */
	public static List<String> getFactorTable1(String label, Module module, Integer id) {
		final int LENGTH = 58;
		
		List<String> result = null;
		
		StringBuilder line = new StringBuilder(LENGTH);
		String curFormat = Messages.get("formats.currency");

		String query = "select scf.name, scf.effect_type, f.effect, f.amount " +
						"from " + module.name() + "_trans_factor as f " +
						"inner join stock_cost_factor as scf on scf.id = f.factor_id " +
						"where trans_id = " + id;
		List<SqlRow> rows = Ebean.createSqlQuery(query).findList();
		if (rows != null && rows.size() > 0) {
			result = new ArrayList<String>();
			
			result.add(label);
			result.add(StringUtils.fill("-", LENGTH));
			
			line.append("| ");
			line.append(StringUtils.padRight(Messages.get("name"), 15));
			line.append(" | ");
			line.append(StringUtils.padRight(Messages.get("effect_type"), 9));
			line.append(" | ");
			line.append(StringUtils.padLeft(Messages.get("effect"), 9));
			line.append(" | ");
			line.append(StringUtils.padLeft(Messages.get("amount"), 12));
			line.append(" |");
			result.add(line.toString());
			
			result.add(StringUtils.fill("-", LENGTH));
			for(SqlRow row: rows) {
				line.setLength(0);
				line.append("| ");
				line.append(StringUtils.padRight(row.getString("name"), 15));
				line.append(" | ");
				line.append(StringUtils.padRight(Messages.get(row.getString("effect_type").toLowerCase()), 9));
				line.append(" | ");
				line.append(Format.asDouble(row.getDouble("effect"), curFormat, 9));
				line.append(" | ");
				line.append(Format.asDouble(row.getDouble("amount"), curFormat, 12));
				line.append(" |");
				result.add(line.toString());
			}
			result.add(StringUtils.fill("-", LENGTH));
		}
		return result;
	}
	
}
