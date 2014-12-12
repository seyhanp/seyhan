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

import javax.persistence.MappedSuperclass;

import play.data.validation.Constraints.Required;
import enums.Right;

@MappedSuperclass
/**
 * @author mdpinar
*/
public abstract class AbstractDocTrans extends AbstractBaseTrans {

	private static final long serialVersionUID = 1L;

	@Required
	public Double amount = 0d;
	public Double debt = 0d;
	public Double credit = 0d;

	public AbstractDocTrans(Right right) {
		super(right);
	}

}
