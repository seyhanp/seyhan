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
package meta;

import java.util.Map;

import play.libs.F;
import play.libs.F.Option;
import play.mvc.QueryStringBindable;
import enums.Right;

/**
 * @author mdpinar
*/
public class RightBind implements QueryStringBindable<RightBind> {

	public Right value;

	public RightBind() {
		;
	}

	public RightBind(Right value) {
		this.value = value; 
	}

	@Override
	@SuppressWarnings("unchecked")
	public Option<RightBind> bind(String key, Map<String, String[]> data) {
		String[] vs = data.get(key);
		if (vs != null && vs.length > 0) {
			value = Right.valueOf(vs[0]);
			return F.Some(this);
		}
		return F.None();
	}

	@Override
	public String unbind(String key) {
		return key + "=" + value.name();

	}

	@Override
	public String javascriptUnbind() {
		return (value != null ? value.name() : "no");
	}

	@Override
	public String toString() {
		return value.name();
	}


}
