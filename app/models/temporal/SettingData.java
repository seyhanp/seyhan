/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import play.data.validation.Constraints;
import enums.ExchangeRatePeriod;
import enums.ExchangeRateSource;

/**
 * @author mdpinar
*/
public class SettingData {

	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(10)
	public String code;

	@Constraints.MaxLength(30)
	public String description;

	/*************************/

	@Constraints.MaxLength(50)
	public String companyName = "";

	public ExchangeRatePeriod exchangePeriod = ExchangeRatePeriod.Once_A_Day;
	public ExchangeRateSource exchangeSource = ExchangeRateSource.TCMB_Exchange;
	public Double exchangeDiffRateForBuying = 0d;
	public Double exchangeDiffRateForSelling = 0d;

}
