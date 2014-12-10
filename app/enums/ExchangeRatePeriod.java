/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

public enum ExchangeRatePeriod {

	Manual,
	Once_A_Day,
	Hourly,
	Every_3_Hours;

	public static ExchangeRatePeriod find(String period) {
		ExchangeRatePeriod erp = Manual;
		try {
			erp = ExchangeRatePeriod.valueOf(period);
		} catch (Exception e) {
			;
		}

		return erp;
	}

}
