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

/**
 * @author mdpinar
*/
public class GridHeader {

	public String title;
	public String color;
	public String width;
	public String align;
	public boolean isBool;
	public boolean isLink;

	public String fieldName;
	public boolean isSortable;

	public GridHeader(String title) {
		this(title, null, null, null, false, false);
	}

	public GridHeader(String title, boolean isLink) {
		this(title, null, null, null, false, isLink);
	}

	public GridHeader(String title, String width, boolean isBool) {
		this(title, width, null, null, isBool, false);
	}

	public GridHeader(String title, String width, boolean isBool, boolean isLink) {
		this(title, width, null, null, isBool, isLink);
	}

	public GridHeader(String title, String width) {
		this(title, width, null, null, false, false);
	}

	public GridHeader(String title, String width, String align, String color) {
		this(title, width, align, color, false, false);
	}

	public GridHeader(String title, String width, boolean isLink, String color) {
		this(title, width, null, color, false, isLink);
	}

	public GridHeader(String title, String width, String align, String color, boolean isBool, boolean isLink) {
		super();
		this.title = title;
		this.width = width;
		this.align = (align != null ? align : "left");
		this.color = (color != null ? color : "black");
		this.isBool = isBool;
		this.isLink = isLink;
	}

	public GridHeader sortable(String fieldName) {
		this.isSortable = true;
		this.fieldName = fieldName;

		return this;
	}

}
