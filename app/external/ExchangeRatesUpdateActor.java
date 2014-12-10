/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package external;
import java.util.Date;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtils;
import controllers.admin.Settings;
import controllers.global.CurrencyRates;
import enums.ExchangeRatePeriod;
import enums.ExchangeRateSource;

/**
 * @author mdpinar
*/
public class ExchangeRatesUpdateActor {

	private final static Logger log = LoggerFactory.getLogger(ExchangeRatesUpdateActor.class);

	private static Boolean isSuccess = null;
	
	static void doIt() {
		DateTime dt = new DateTime();

		if (isSuccess != null && (dt.getHourOfDay() < 7 || dt.getHourOfDay() > 20 || Settings.getGlobal().exchangePeriod.equals(ExchangeRatePeriod.Manual))) {
			log.debug("There is no exchange support!");
			return;
		}

		ExchangeRateSource exchangeSource = Settings.getGlobal().exchangeSource;
		ExchangeRatePeriod exchangePeriod = Settings.getGlobal().exchangePeriod;
		log.info("Exchange rate source and refresh period -> [" + exchangeSource + ", " + exchangePeriod + "]");

		boolean isSuitable = false;
		switch (exchangePeriod) {
			case Hourly: {
				isSuitable = true;
				break;
			}
			case Every_3_Hours: {
				isSuitable = (dt.getHourOfDay() % 3 == 0 || isSuccess == null);
				break;
			}
			case Once_A_Day: {
				isSuitable = (dt.getHourOfDay() == 16 || isSuccess == null);
				break;
			}
		}

		if (isSuccess != null && ! isSuccess) isSuitable = true;

		if (isSuitable) {
			log.info("Exchange refresher was triggered : " + DateUtils.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss"));
			isSuccess = false;
			try {
				switch (exchangeSource) {
					case TCMB_Exchange: {
						CurrencyRates.pullTCMBExcange();
						isSuccess = true;
						break;
					}
					case TCMB_Effective: {
						CurrencyRates.pullTCMBEffective();
						isSuccess = true;
						break;
					}
				}
			} catch (Exception e) {
				log.error("ERROR", e);
			}
		}

	}


}
