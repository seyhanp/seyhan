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
public abstract class BaseContactExtraFieldsModel extends BaseModel {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne public ContactExtraFields extraField0;
	@ManyToOne public ContactExtraFields extraField1;
	@ManyToOne public ContactExtraFields extraField2;
	@ManyToOne public ContactExtraFields extraField3;
	@ManyToOne public ContactExtraFields extraField4;
	@ManyToOne public ContactExtraFields extraField5;
	@ManyToOne public ContactExtraFields extraField6;
	@ManyToOne public ContactExtraFields extraField7;
	@ManyToOne public ContactExtraFields extraField8;
	@ManyToOne public ContactExtraFields extraField9;

}
