/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author mdpinar
*/
public class JSRouter extends Controller {

	public static Result jsRoutes() {
		response().setContentType("text/javascript");
		return ok(
			Routes.javascriptRouter("jsRoutes",

				controllers.stock.routes.javascript.Categories.paste(),
				controllers.stock.routes.javascript.Categories.save(),
				controllers.stock.routes.javascript.Categories.create(),
				controllers.stock.routes.javascript.Categories.edit(),
				controllers.stock.routes.javascript.Categories.remove(),

				controllers.stock.routes.javascript.Depots.list(),
				controllers.stock.routes.javascript.Depots.save(),
				controllers.stock.routes.javascript.Depots.create(),
				controllers.stock.routes.javascript.Depots.edit(),
				controllers.stock.routes.javascript.Depots.remove(),
				controllers.stock.routes.javascript.Depots.options(),

				controllers.stock.routes.javascript.ExtraFields.list(),
				controllers.stock.routes.javascript.ExtraFields.save(),
				controllers.stock.routes.javascript.ExtraFields.create(),
				controllers.stock.routes.javascript.ExtraFields.edit(),
				controllers.stock.routes.javascript.ExtraFields.remove(),
				controllers.stock.routes.javascript.ExtraFields.options(),

				controllers.stock.routes.javascript.TransSources.list(),
				controllers.stock.routes.javascript.TransSources.save(),
				controllers.stock.routes.javascript.TransSources.create(),
				controllers.stock.routes.javascript.TransSources.edit(),
				controllers.stock.routes.javascript.TransSources.remove(),
				controllers.stock.routes.javascript.TransSources.options(),

				controllers.order.routes.javascript.TransSources.list(),
				controllers.order.routes.javascript.TransSources.save(),
				controllers.order.routes.javascript.TransSources.create(),
				controllers.order.routes.javascript.TransSources.edit(),
				controllers.order.routes.javascript.TransSources.remove(),
				controllers.order.routes.javascript.TransSources.options(),

				controllers.waybill.routes.javascript.TransSources.list(),
				controllers.waybill.routes.javascript.TransSources.save(),
				controllers.waybill.routes.javascript.TransSources.create(),
				controllers.waybill.routes.javascript.TransSources.edit(),
				controllers.waybill.routes.javascript.TransSources.remove(),
				controllers.waybill.routes.javascript.TransSources.options(),

				controllers.invoice.routes.javascript.TransSources.list(),
				controllers.invoice.routes.javascript.TransSources.save(),
				controllers.invoice.routes.javascript.TransSources.create(),
				controllers.invoice.routes.javascript.TransSources.edit(),
				controllers.invoice.routes.javascript.TransSources.remove(),
				controllers.invoice.routes.javascript.TransSources.options(),

				controllers.contact.routes.javascript.TransSources.list(),
				controllers.contact.routes.javascript.TransSources.save(),
				controllers.contact.routes.javascript.TransSources.create(),
				controllers.contact.routes.javascript.TransSources.edit(),
				controllers.contact.routes.javascript.TransSources.remove(),
				controllers.contact.routes.javascript.TransSources.options(),

				controllers.contact.routes.javascript.Categories.list(),
				controllers.contact.routes.javascript.Categories.save(),
				controllers.contact.routes.javascript.Categories.create(),
				controllers.contact.routes.javascript.Categories.edit(),
				controllers.contact.routes.javascript.Categories.remove(),
				controllers.contact.routes.javascript.Categories.options(),

				controllers.contact.routes.javascript.ExtraFields.list(),
				controllers.contact.routes.javascript.ExtraFields.save(),
				controllers.contact.routes.javascript.ExtraFields.create(),
				controllers.contact.routes.javascript.ExtraFields.edit(),
				controllers.contact.routes.javascript.ExtraFields.remove(),
				controllers.contact.routes.javascript.ExtraFields.options(),

				controllers.chqbll.routes.javascript.TypesForCheque.list(),
				controllers.chqbll.routes.javascript.TypesForCheque.save(),
				controllers.chqbll.routes.javascript.TypesForCheque.create(),
				controllers.chqbll.routes.javascript.TypesForCheque.edit(),
				controllers.chqbll.routes.javascript.TypesForCheque.remove(),
				controllers.chqbll.routes.javascript.TypesForCheque.options(),

				controllers.chqbll.routes.javascript.TypesForBill.list(),
				controllers.chqbll.routes.javascript.TypesForBill.save(),
				controllers.chqbll.routes.javascript.TypesForBill.create(),
				controllers.chqbll.routes.javascript.TypesForBill.edit(),
				controllers.chqbll.routes.javascript.TypesForBill.remove(),
				controllers.chqbll.routes.javascript.TypesForBill.options(),

				controllers.chqbll.routes.javascript.PayrollSourcesForCheque.list(),
				controllers.chqbll.routes.javascript.PayrollSourcesForCheque.save(),
				controllers.chqbll.routes.javascript.PayrollSourcesForCheque.create(),
				controllers.chqbll.routes.javascript.PayrollSourcesForCheque.edit(),
				controllers.chqbll.routes.javascript.PayrollSourcesForCheque.remove(),
				controllers.chqbll.routes.javascript.PayrollSourcesForCheque.options(),

				controllers.chqbll.routes.javascript.PayrollSourcesForBill.list(),
				controllers.chqbll.routes.javascript.PayrollSourcesForBill.save(),
				controllers.chqbll.routes.javascript.PayrollSourcesForBill.create(),
				controllers.chqbll.routes.javascript.PayrollSourcesForBill.edit(),
				controllers.chqbll.routes.javascript.PayrollSourcesForBill.remove(),
				controllers.chqbll.routes.javascript.PayrollSourcesForBill.options(),

				controllers.chqbll.routes.javascript.AjaxService.create(),
				controllers.chqbll.routes.javascript.AjaxService.search(),
				controllers.chqbll.routes.javascript.AjaxService.sequentialForm(),
				controllers.chqbll.routes.javascript.AjaxService.sequentialValidation(),

				controllers.bank.routes.javascript.Banks.list(),
				controllers.bank.routes.javascript.Banks.save(),
				controllers.bank.routes.javascript.Banks.create(),
				controllers.bank.routes.javascript.Banks.edit(),
				controllers.bank.routes.javascript.Banks.remove(),
				controllers.bank.routes.javascript.Banks.options(),
				
				controllers.bank.routes.javascript.Expenses.list(),
				controllers.bank.routes.javascript.Expenses.save(),
				controllers.bank.routes.javascript.Expenses.create(),
				controllers.bank.routes.javascript.Expenses.edit(),
				controllers.bank.routes.javascript.Expenses.remove(),
				controllers.bank.routes.javascript.Expenses.options(),

				controllers.bank.routes.javascript.TransSources.list(),
				controllers.bank.routes.javascript.TransSources.save(),
				controllers.bank.routes.javascript.TransSources.create(),
				controllers.bank.routes.javascript.TransSources.edit(),
				controllers.bank.routes.javascript.TransSources.remove(),
				controllers.bank.routes.javascript.TransSources.options(),

				controllers.safe.routes.javascript.Safes.list(),
				controllers.safe.routes.javascript.Safes.save(),
				controllers.safe.routes.javascript.Safes.create(),
				controllers.safe.routes.javascript.Safes.edit(),
				controllers.safe.routes.javascript.Safes.remove(),
				controllers.safe.routes.javascript.Safes.options(),

				controllers.safe.routes.javascript.TransSources.list(),
				controllers.safe.routes.javascript.TransSources.save(),
				controllers.safe.routes.javascript.TransSources.create(),
				controllers.safe.routes.javascript.TransSources.edit(),
				controllers.safe.routes.javascript.TransSources.remove(),
				controllers.safe.routes.javascript.TransSources.options(),

				controllers.safe.routes.javascript.Expenses.list(),
				controllers.safe.routes.javascript.Expenses.save(),
				controllers.safe.routes.javascript.Expenses.create(),
				controllers.safe.routes.javascript.Expenses.edit(),
				controllers.safe.routes.javascript.Expenses.remove(),
				controllers.safe.routes.javascript.Expenses.options(),

				controllers.sale.routes.javascript.Sellers.list(),
				controllers.sale.routes.javascript.Sellers.save(),
				controllers.sale.routes.javascript.Sellers.create(),
				controllers.sale.routes.javascript.Sellers.edit(),
				controllers.sale.routes.javascript.Sellers.remove(),
				controllers.sale.routes.javascript.Sellers.options(),

				controllers.stock.routes.javascript.Units.list(),
				controllers.stock.routes.javascript.Units.save(),
				controllers.stock.routes.javascript.Units.create(),
				controllers.stock.routes.javascript.Units.edit(),
				controllers.stock.routes.javascript.Units.remove(),
				controllers.stock.routes.javascript.Units.options(),

				controllers.stock.routes.javascript.CostFactors.list(),
				controllers.stock.routes.javascript.CostFactors.save(),
				controllers.stock.routes.javascript.CostFactors.create(),
				controllers.stock.routes.javascript.CostFactors.edit(),
				controllers.stock.routes.javascript.CostFactors.remove(),

				controllers.stock.routes.javascript.Stocks.createClone(),
				controllers.contact.routes.javascript.Contacts.createClone(),

				controllers.stock.routes.javascript.Transes.createClone(),
				controllers.order.routes.javascript.Transes.createClone(),
				controllers.contact.routes.javascript.Transes.createClone(),
				controllers.safe.routes.javascript.Transes.createClone(),
				controllers.bank.routes.javascript.Transes.createClone(),

				controllers.global.routes.javascript.TransPoints.paste(),
				controllers.global.routes.javascript.TransPoints.save(),
				controllers.global.routes.javascript.TransPoints.create(),
				controllers.global.routes.javascript.TransPoints.edit(),
				controllers.global.routes.javascript.TransPoints.remove(),

				controllers.global.routes.javascript.PrivateCodes.paste(),
				controllers.global.routes.javascript.PrivateCodes.save(),
				controllers.global.routes.javascript.PrivateCodes.create(),
				controllers.global.routes.javascript.PrivateCodes.edit(),
				controllers.global.routes.javascript.PrivateCodes.remove(),

				controllers.global.routes.javascript.Currencies.list(),
				controllers.global.routes.javascript.Currencies.save(),
				controllers.global.routes.javascript.Currencies.create(),
				controllers.global.routes.javascript.Currencies.edit(),
				controllers.global.routes.javascript.Currencies.remove(),

				controllers.admin.routes.javascript.Users.editRestricted(),
				controllers.admin.routes.javascript.Users.saveRestricted(),

				controllers.admin.routes.javascript.Documents.showPrintForm(),
				controllers.admin.routes.javascript.Documents.printDocument(),

				controllers.admin.routes.javascript.DocumentTargets.list(),
				controllers.admin.routes.javascript.DocumentTargets.save(),
				controllers.admin.routes.javascript.DocumentTargets.create(),
				controllers.admin.routes.javascript.DocumentTargets.edit(),
				controllers.admin.routes.javascript.DocumentTargets.remove(),

				controllers.admin.routes.javascript.ExtraFieldsForStocks.list(),
				controllers.admin.routes.javascript.ExtraFieldsForStocks.save(),
				controllers.admin.routes.javascript.ExtraFieldsForStocks.edit(),
				
				controllers.admin.routes.javascript.ExtraFieldsForContacts.list(),
				controllers.admin.routes.javascript.ExtraFieldsForContacts.save(),
				controllers.admin.routes.javascript.ExtraFieldsForContacts.edit(),

				controllers.stock.routes.javascript.Stocks.investigation(),
				controllers.contact.routes.javascript.Contacts.investigation(),
				controllers.safe.routes.javascript.Safes.investigation(),
				controllers.bank.routes.javascript.Banks.investigation()

			)
		);
	}

}
