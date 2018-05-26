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
package external;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtils;
import utils.JsonReader;
import external.model.ExchangeRate;

/**
 * Narodowy Bank Polski (Central Bank of Poland) Exchange Rates
 * 
 * @author mdpinar
*/
public class NBPExchanges {

	private final static Logger log = LoggerFactory.getLogger(NBPExchanges.class);

	public static List<ExchangeRate> getRates() {
		List<ExchangeRate> result = new ArrayList<ExchangeRate>();

		try {
			log.info("Narodowy Bank Polski xml service connection is trying...");
			JSONArray jsonArray = JsonReader.readJsonArrayFromUrl("http://api.nbp.pl/api/exchangerates/tables/C/today");
			JSONObject entry = jsonArray.getJSONObject(0);
			
			Date firstDate = DateUtils.parse(entry.getString("effectiveDate"), "yyyy-MM-dd");

			JSONArray rateArray = entry.getJSONArray("rates");
			for (int i = 0; i < rateArray.length(); i++) {
				JSONObject element = rateArray.getJSONObject(i);

				ExchangeRate rate = new ExchangeRate();

				if (i == 0) rate.setDate(firstDate);

				rate.setCode(element.getString("code"));
				rate.setName(element.getString("currency"));
				rate.setExcBuying(element.getDouble("bid"));
				rate.setExcSelling(element.getDouble("ask"));

				result.add(rate);
			}
			log.info("xml service connection was successfuly done.");

		} catch (Exception e) {
			log.error("ERROR", e);
		}

		return result;
	}

}
