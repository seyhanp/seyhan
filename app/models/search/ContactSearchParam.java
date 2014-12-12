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
package models.search;

import models.ContactCategory;
import models.ContactExtraFields;
import enums.ContactStatus;

/**
 * @author mdpinar
*/
public class ContactSearchParam extends AbstractSearchParam {

	public String code;
	public String name;

	public ContactCategory category;
	public ContactStatus status;

	public ContactExtraFields extraField0;
	public ContactExtraFields extraField1;
	public ContactExtraFields extraField2;
	public ContactExtraFields extraField3;
	public ContactExtraFields extraField4;
	public ContactExtraFields extraField5;
	public ContactExtraFields extraField6;
	public ContactExtraFields extraField7;
	public ContactExtraFields extraField8;
	public ContactExtraFields extraField9;

}
