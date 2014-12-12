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
package reports;

import java.util.HashMap;
import java.util.Map;

import enums.ReportUnit;

/**
 * @author mdpinar
*/
public class ReportParams {

	public String modul;
	public String reportName;
	public String reportNameExtra;
	public ReportUnit reportUnit;
	public String query;
	public String orderBy;
	public String having;
	public Map<String, Object> paramMap = new HashMap<String, Object>();

}
