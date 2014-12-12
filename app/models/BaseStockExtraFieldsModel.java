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

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author mdpinar
*/
@MappedSuperclass
public abstract class BaseStockExtraFieldsModel extends BaseModel {

	private static final long serialVersionUID = 1L;

	@ManyToOne public StockExtraFields extraField0;
	@ManyToOne public StockExtraFields extraField1;
	@ManyToOne public StockExtraFields extraField2;
	@ManyToOne public StockExtraFields extraField3;
	@ManyToOne public StockExtraFields extraField4;
	@ManyToOne public StockExtraFields extraField5;
	@ManyToOne public StockExtraFields extraField6;
	@ManyToOne public StockExtraFields extraField7;
	@ManyToOne public StockExtraFields extraField8;
	@ManyToOne public StockExtraFields extraField9;

}
