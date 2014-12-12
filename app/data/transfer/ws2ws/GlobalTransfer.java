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
package data.transfer.ws2ws;

import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import enums.Module;
/**
 * @author mdpinar
*/
class GlobalTransfer extends BaseTransfer {

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new GlobalPrivateCode(), sourceWS, targetWS, false);
		executeInsertQueryForInfoTables(new GlobalTransPoint(), sourceWS, targetWS, false);
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("global_private_code", targetWS);
		executeDeleteQueryForInfoTables("global_trans_point", targetWS);
	}

	@Override
	public Module getModule() {
		return Module.global;
	}

}
