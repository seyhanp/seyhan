/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

public enum ExchangeRateSource {

	TCMB_Exchange,
	TCMB_Effective;

	public static ExchangeRateSource find(String source) {
		ExchangeRateSource ers = TCMB_Exchange;
		try {
			ers = ExchangeRateSource.valueOf(source);
		} catch (Exception e) {
			;
		}

		return ers;
	}

}
