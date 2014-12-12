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
package utils;

import models.AbstractStockTrans;
import models.AbstractStockTransDetail;
import models.GlobalCurrencyRateDetail;
import models.Stock;
import controllers.global.CurrencyRates;
import controllers.global.Profiles;

/**
 * @author mdpinar
*/
public class CurrencyUtils {

	public static void setDetailExchange(AbstractStockTransDetail detail, Stock stock, AbstractStockTrans master) {
		if (detail == null || stock == null) return;

		if (detail.excCode == null || detail.excCode.trim().isEmpty()) detail.excCode = stock.excCode;
		if ((detail.excCode == null || detail.excCode.trim().isEmpty()) && master != null) detail.excCode = master.excCode;
		if (detail.excCode == null || detail.excCode.trim().isEmpty()) detail.excCode = Profiles.chosen().gnel_excCode;
		detail.excRate = CurrencyRates.getExchangeRate(detail.excCode, detail.right.transType);
		detail.excEquivalent = NumericUtils.round(detail.total * detail.excRate);
	}

	public static double findTodayRate(String currency, boolean isBuying) {
		double result = 1;

		if (currency != null) {
			GlobalCurrencyRateDetail gcrd =  CurrencyRates.getActualExchangeRatesMap().get(currency);
			if (gcrd != null) {
				if (isBuying)
					result = gcrd.buying;
				else
					result = gcrd.selling;
			}
		}
		
		return result;
	}

	public static double findTodayValue(String currency, double amount, boolean isBuying) {
		double result = amount;

		if (currency != null && amount > 0) {
			result = amount * findTodayRate(currency, isBuying);
		}
		
		return result;
	}

}
