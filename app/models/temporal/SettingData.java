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
