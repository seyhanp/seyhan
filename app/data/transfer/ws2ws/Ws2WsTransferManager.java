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

import java.util.LinkedHashMap;
import java.util.Map;

import models.temporal.Ws2WsTransferModel;
import enums.Module;

/**
 * @author mdpinar
*/
public class Ws2WsTransferManager {

	private static Map<Module, ITransfer> transferMap;

	static {
		transferMap = new LinkedHashMap<Module, ITransfer>();

		/*
		 * Ordering is important!!!
		 */
		transferMap.put(Module.order, new OrderTransfer());
		transferMap.put(Module.waybill, new WaybillTransfer());
		transferMap.put(Module.invoice, new InvoiceTransfer());
		transferMap.put(Module.stock, new StockTransfer());
		transferMap.put(Module.cheque, new ChqbllTransfer());
		transferMap.put(Module.bank, new BankTransfer());
		transferMap.put(Module.safe, new SafeTransfer());
		transferMap.put(Module.contact, new ContactTransfer());
		transferMap.put(Module.global, new GlobalTransfer());
		transferMap.put(Module.sale, new SaleTransfer());
	}

	public static void transfer(Ws2WsTransferModel model) {
		transfer(model.saleInfo, false, Module.sale, model);
		transfer(model.globalInfo, false, Module.global, model);
		transfer(model.contactInfo, model.contactTrans, Module.contact, model);
		transfer(model.safeInfo, model.safeTrans, Module.safe, model);
		transfer(model.bankInfo, model.bankTrans, Module.bank, model);
		transfer(model.chqbllInfo, model.chqbllTrans, Module.cheque, model);
		transfer(model.stockInfo, model.stockTrans, Module.stock, model);
		transfer(model.orderInfo, model.orderTrans, Module.order, model);
		transfer(model.waybillInfo, model.waybillTrans, Module.waybill, model);
		transfer(model.invoiceInfo, false, Module.invoice, model);
	}

	public static void destroy(int targetWS, boolean willBeDestroyedAll) {
		for (ITransfer transfer : transferMap.values()) {
			transfer.destroyTransaction(targetWS, willBeDestroyedAll);
		}
		for (ITransfer transfer : transferMap.values()) {
			transfer.destroyInfo(targetWS);
		}
	}

	private static void transfer(boolean isInfoWanted, boolean isTransWanted, Module module, Ws2WsTransferModel model) {
		ITransfer transfer = transferMap.get(module);
		if (isInfoWanted) transfer.transferInfo(model.sourceWS.id, model.targetWS.id);
		if (isTransWanted) transfer.transferTransaction(model.transDate, model.description, model.sourceWS.id, model.targetWS.id);
	}

}
