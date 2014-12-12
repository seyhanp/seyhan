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
package external.model;

import java.util.Date;

/**
 * @author mdpinar
*/
public class TCMBRate {

	 private Date date;
	 private String code;
	 private String name;
	 private Double excBuying;
	 private Double excSelling;
	 private Double effBuying;
	 private Double effSelling;
	 
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getExcBuying() {
		return excBuying;
	}

	public void setExcBuying(Double excBuying) {
		this.excBuying = excBuying;
	}

	public Double getExcSelling() {
		return excSelling;
	}

	public void setExcSelling(Double excSelling) {
		this.excSelling = excSelling;
	}

	public Double getEffBuying() {
		return effBuying;
	}

	public void setEffBuying(Double effBuying) {
		this.effBuying = effBuying;
	}

	public Double getEffSelling() {
		return effSelling;
	}

	public void setEffSelling(Double effSelling) {
		this.effSelling = effSelling;
	}

}
