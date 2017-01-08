$("body").addClass("loading");

var currencies = [];
var sellers;
var exchange_rates;

$.getJSON('/as/get/simple_data',
	function(data) {
		exchange_rates = data.exchange_rates;
		for(var cur in exchange_rates) {
			if (cur != 'info') {
				currencies.push(cur);
			}
		}

		sellers = data.sellers;
	}
);

$(document).ready(function(){
	
	$("#stockTable").tableAutoAddRow({inputBoxAutoNumber: true, rowNumColumn: "rowNumber" });
	$("#stockTable .delRow").btnDelRow(function(row){
		calculateAllFieldsForStocks();
	});
	$("#stockTable").freezeTableColumns({
		width: 900,
		height: tableHeight
	});

	$("#stockTable").on({keyup: function(e){
		var rowNo = this.id.match(/\[(.*?)\]/)[1];
		var name  = this.id.substring(this.id.indexOf('\_')+1);
		
		calculateFieldsForStocks(rowNo, name);
		findTotalsForStocks();
	}}, "input.attention");

	$("#stockTable").on({change: function(e){
		var rowNo = this.id.match(/\[(.*?)\]/)[1];
		var name  = this.id.substring(this.id.indexOf('\_')+1);
		
		calculateFieldsForStocks(rowNo, name);
		findTotalsForStocks();
	}}, "select.attention");

	$("#stockTable").on({click: function(e){
		var id = $(this).nextAll('.impid:first').val();
		if (id != undefined && id.length > 0) {
			$.getJSON('/stocks/investigation/'+id,
				function(data) {
					$("#inv_title").html('<h4>'+data.title+'</h4>');
					$("#inv_body").html(data.body);
		
					$('#inv_modal\\-form').modal('show');
				}
			);
		}
	}}, ".invid");

	if (document.getElementById('factorTable') != undefined) {
		$("#factorTable").tableAutoAddRow({ inputBoxAutoNumber: true, rowNumColumn: "rowNumber", autoAddRow: true });
		$("#factorTable .delRow").btnDelRow(function(row){
			findTotalsForStocks();
		});
		$("#factorTable").on({change: function(e) {
			var rowNo = this.id.match(/\[(.*?)\]/)[1];
			var baseId = '#factors\\['+rowNo+'\\]\\_';
			var factorId = $(baseId + 'factor_id').val();
	
			if (factorId.length > 0) {
				$.getJSON('/as/find_cost_factor/' + factorId,
					function(data) {
						$(baseId + 'calcType').val(data.calcType);
						$(baseId + 'factorType').val(data.factorType);
						$(baseId + 'effectType').val(data.effectType);
						$(baseId + 'effect').val(data.effect);
						$(baseId + 'calcTypeOri').val(data.calcTypeOri);
						$(baseId + 'factorTypeOri').val(data.factorTypeOri);
						$(baseId + 'effectTypeOri').val(data.effectTypeOri);
		
						if (data.effectTypeOri === 'Amount') {
							$(baseId + 'amount').val(data.effect);
						} else {
							var subtotal = parseFloat($('#subtotal').val());
							$(baseId + 'amount').val(formatMoney((subtotal * data.effect) / 100));
						}
						findTotalsForStocks();
					}
				);
			} else {
				$(baseId + 'calcType').val('');
				$(baseId + 'factorType').val('');
				$(baseId + 'effectType').val('');
				$(baseId + 'effect').val('');
				$(baseId + 'amount').val('');
			}
	
		}}, ".factors.mainInput");
	}

	$("body").removeClass("loading");
});

$(document).on('selectContactEvent', function(event, data) {
	$('#contactName').val('');
	$('#contactTaxOffice').val('');
	$('#contactTaxNumber').val('');
	$('#contactAddress1').val('');
	$('#contactAddress2').val('');
	$('#contactStockPriceNo').val('1');
	if (data) {
		$('#contactName').val(data.name);
		$('#contactTaxOffice').val(data.taxOffice);
		$('#contactTaxNumber').val(data.taxNumber);
		$('#contactAddress1').val(data.address1);
		$('#contactAddress2').val(data.address2);
		$('#contactStockPriceNo').val(data.stockPriceNo);
	}
});

/**************************************************************************
	Stok tablosu
**************************************************************************/

function calculateAllFieldsForStocks() {
	var stockRowCount = document.getElementById('stockTable').rows.length;
	for(var stockRowNo=0;stockRowNo<stockRowCount;stockRowNo++) {
		calculateFieldsForStocks(stockRowNo, 'tax_rate');
	}

	findTotalsForStocks();
}

/**************************************************************************/

function calculateFieldsForStocks(stockRowNo, datafield) {
	var stockBaseId = '#details\\['+stockRowNo+'\\]\\_';
	if ($(stockBaseId + 'stock_id').val().length <= 0) return;
	
	var row = {
		basePrice: parseDouble($(stockBaseId + 'basePrice').val(), 1),
		price: parseFloat($(stockBaseId + 'price').val()),
		amount: parseDouble($(stockBaseId + 'amount').val(), 0),
		quantity: parseDouble($(stockBaseId + 'quantity').val(), 1),
		unit : $(stockBaseId + 'unit').val(),
		unit1: $(stockBaseId + 'unit1').val(),
		unit2: $(stockBaseId + 'unit2').val(),
		unit3: $(stockBaseId + 'unit3').val(),
		unitRatio: parseDouble($(stockBaseId + 'unitRatio').val(), 1),
		unit2Ratio: parseDouble($(stockBaseId + 'unit2Ratio').val(), 0),
		unit3Ratio: parseDouble($(stockBaseId + 'unit3Ratio').val(), 0),
		discountRate1: parseDouble($(stockBaseId + 'discountRate1').val(), 0),
		discountRate2: parseDouble($(stockBaseId + 'discountRate2').val(), 0),
		discountRate3: parseDouble($(stockBaseId + 'discountRate3').val(), 0),
		discountAmount: parseDouble($(stockBaseId + 'discountAmount').val(), 0),
		taxRate: parseDouble($(stockBaseId + 'taxRate').val(), 0),
		taxRate2: parseDouble($(stockBaseId + 'taxRate2').val(), 0),
		taxRate3: parseDouble($(stockBaseId + 'taxRate3').val(), 0),
		plusFactorAmount: parseDouble($(stockBaseId + 'plusFactorAmount').val(), 0),
		minusFactorAmount: parseDouble($(stockBaseId + 'minusFactorAmount').val(), 0),
		excCode: $(stockBaseId + 'excCode').val(),
		excRate: parseDouble($(stockBaseId + 'excRate').val(), 0),
		excEquivalent: 0,
		taxAmount: 0,
		seller_id: null,
		total: 0
	};
	if (row.excCode == null) row.excCode = mainExcCode;
	
	row.unitRatio = 1;
	if (row.unit === row.unit2)
		row.unitRatio = row.unit2Ratio;
	else if (row.unit === row.unit3)
		row.unitRatio = row.unit3Ratio;

	if (datafield === 'price') {
		row.basePrice = (row.price / row.unitRatio).roundup(pennyDigits);
	}
	if (datafield === 'amount') {
		row.basePrice = (row.amount / (row.quantity*row.unitRatio)).roundup(pennyDigits);
	}
	
	row.price  = (row.basePrice * row.unitRatio).roundup(pennyDigits);
	row.amount = (row.quantity * row.price).roundup(pennyDigits);
	
	var factorEffect = (row.plusFactorAmount - row.minusFactorAmount);

	if (datafield === 'discountAmount') {
		$(stockBaseId + 'discountRate1').val(0);
		$(stockBaseId + 'discountRate2').val(0);
		$(stockBaseId + 'discountRate3').val(0);
	} else {
		var discount1 = (row.amount * row.discountRate1) / 100;
		var discount2 = ((row.amount - discount1) * row.discountRate2) / 100;
		var discount3 = ((row.amount - discount2) * row.discountRate3) / 100;
		row.discountAmount = (discount1 + discount2 + discount3).roundup(pennyDigits);
	}
	
	var basis = row.amount + factorEffect - row.discountAmount;
	var taxTot1 = (row.taxRate  > 0 ? (basis * row.taxRate) / 100 : 0);
	var taxTot2 = (row.taxRate2 > 0 ? (basis * row.taxRate2) / 100 : 0);
	var taxTot3 = (row.taxRate3 > 0 ? (basis * row.taxRate3) / 100 : 0);
	row.taxAmount = (taxTot1 + taxTot2 + taxTot3).roundup(pennyDigits);			

	if ($("#isTaxInclude").val() === 'true') {
		row.total = (basis + row.taxAmount).roundup(pennyDigits);
	} else {
		row.total = (basis).roundup(pennyDigits);
	}

	if (isNotOpeningTrans) {
		if (datafield === 'excCode' || row.excRate <= 0 || row.excRate > 5) {
			if (isBuying) {
				row.excRate = exchange_rates[row.excCode].buying;
			} else {
				row.excRate = exchange_rates[row.excCode].selling;
			}
		}
		row.excEquivalent = (row.total * row.excRate).roundup(pennyDigits);
	
		if (sellers[row.seller]) {
			row.seller_id = sellers[row.seller];
		} else {
			row.seller_id = null;
		}
	} else {
		row.excRate = 1.0;
		row.excEquivalent = row.total;
		row.seller_id = null;
	}
	
	if (datafield != 'price') $(stockBaseId + 'price').val(formatMoney(row.price));
	if (datafield != 'amount') $(stockBaseId + 'amount').val(formatMoney(row.amount));

	$(stockBaseId + 'taxAmount').val(formatMoney(row.taxAmount));
	$(stockBaseId + 'excRate').val(row.excRate);
	$(stockBaseId + 'excEquivalent').val(formatMoney(row.excEquivalent));
	$(stockBaseId + 'basePrice').val(row.basePrice);
	$(stockBaseId + 'unitRatio').val(formatMoney(row.unitRatio));
	$(stockBaseId + 'discountAmount').val(formatMoney(row.discountAmount));
	$(stockBaseId + 'seller').val(row.seller);
	$(stockBaseId + 'seller_id').val(row.seller_id);
	$(stockBaseId + 'total').val(formatMoney(row.total));
}

/**************************************************************************/

$(document).on("stock_select", function(event, data) {
	if (data == undefined || data.id.length < 1) return false;

	$("#stockTable").btnManuelAddRow(
		function(rowIndex) {
			var row = {};
			var stockBaseId = '#details\\['+rowIndex+'\\]\\_';

			var lastStock = $(stockBaseId + 'stock_id').val();
			if (lastStock.length > 0) {
				stockBaseId = '#details\\['+(rowIndex+1)+'\\]\\_';
				$("#stockTable").btnManuelAddRow();
			}
			
			updateFormRegulators("#stockTable tbody tr");
			
			var row = {
				id:0, code: '', name: '',
				price: 0, amount: 0, quantity: 0,
				unit : '', unit1: '', unit2: '', unit3: '',
				unitRatio: 1, unit2Ratio: 1, unit3Ratio: 1,
				excCode: '', excRate: 0, excEquivalent: 0,
				taxRate: 0, taxRate2: 0, taxRate3: 0, taxAmount: 0,
				discountRate1: 0, discountRate2: 0, discountRate3: 0, discountAmount: 0,
				basePrice: 0, price: 0, amount: 0, seller_id: null,
			};

			row.id = data.id;
			row.code = data.code;
			row.name = (data.prefix != null ? data.prefix + ' ' : '') + data.name + (data.suffix != null ? ' ' + data.suffix : '');;
			row.quantity = data.number;

			if (data.excCode != null && data.excCode.length > 0)
				row.excCode = data.excCode;
			else
				row.excCode = defaultExcCode;

			if (isBuying) {
				row.basePrice = data.buyPrice;
				row.taxRate = data.buyTax;
				row.excRate = exchange_rates[row.excCode].buying;
			} else {
				row.basePrice = data.sellPrice;
				row.taxRate = data.sellTax;
				row.excRate = exchange_rates[row.excCode].selling;
			}
			row.taxRate2 = data.taxRate2;
			row.taxRate3 = data.taxRate3;

			row.unit = data.unit1;
			if (data.unitNo == 2) {
				row.unit = data.unit2;
				row.unitRatio = data.unit2ratio;
			}
			if (data.unitNo == 3) {
				row.unit = data.unit3;
				row.unitRatio = data.unit3ratio;
			}
			
			row.price = row.basePrice * row.unitRatio;
			row.unit1 = data.unit1;
			row.unit2 = data.unit2;
			row.unit3 = data.unit3;
			row.unit2Ratio = data.unit2ratio;
			row.unit3Ratio = data.unit3ratio;
			row.amount = (row.quantity * row.price).roundup(pennyDigits);
			row.excEquivalent = (row.amount * row.excRate).roundup(pennyDigits);

			if (isSelling) {
				row.discountRate1 = data.discountRate1;
				row.discountRate2 = data.discountRate2;
				row.discountRate3 = data.discountRate3;
				var discount1 = (row.discountRate1 > 0 ? (row.amount * row.discountRate1) / 100 : 0);
				var discount2 = (row.discountRate2 > 0 ? ((row.amount - discount1) * row.discountRate2) / 100 : 0);
				var discount3 = (row.discountRate3 > 0 ? ((row.amount - discount2) * row.discountRate3) / 100 : 0);
				row.discountAmount = (discount1 + discount2 + discount3).roundup(pennyDigits);
			}
			
			var taxTot1 = (row.taxRate  > 0 ? (row.amount * row.taxRate) / 100 : 0);
			var taxTot2 = (row.taxRate2 > 0 ? (row.amount * row.taxRate2) / 100 : 0);
			var taxTot3 = (row.taxRate3 > 0 ? (row.amount * row.taxRate3) / 100 : 0);
			row.taxAmount = (taxTot1 + taxTot2 + taxTot3).roundup(pennyDigits);			

			var seller_id = $('#seller_id').val();
			if (seller_id != undefined && seller_id.length > 0) {
				row.seller_id = seller_id;
				row.seller = sellers[seller_id];
			} else {
				row.seller_id = null;
				row.seller = '';
			}

			$(stockBaseId + 'stock_id').val(row.id);
			$(stockBaseId + 'code').val(row.code);
			$(stockBaseId + 'name').val(row.name);
			$(stockBaseId + 'basePrice').val(row.basePrice);
			$(stockBaseId + 'price').val(formatMoney(row.price));
			$(stockBaseId + 'quantity').val(row.quantity);
			$(stockBaseId + 'amount').val(formatMoney(row.amount));

			$(stockBaseId + 'unit').empty();
			$(stockBaseId + 'unit').append($('<option>', {value:row.unit1, text:row.unit1, selected:(row.unit == row.unit1)}));
			if (row.unit2 != null && row.unit2.length > 0) $(stockBaseId + 'unit').append($('<option>', {value:row.unit2, text:row.unit2, selected:(row.unit == row.unit2)}));
			if (row.unit3 != null && row.unit3.length > 0) $(stockBaseId + 'unit').append($('<option>', {value:row.unit3, text:row.unit3, selected:(row.unit == row.unit3)}));
			$(stockBaseId + 'unit1').val(row.unit1);
			$(stockBaseId + 'unit2').val(row.unit2);
			$(stockBaseId + 'unit3').val(row.unit3);
			
			$(stockBaseId + 'unitRatio').val(row.unitRatio);
			$(stockBaseId + 'unit2Ratio').val(row.unit2Ratio);
			$(stockBaseId + 'unit3Ratio').val(row.unit3Ratio);
			$(stockBaseId + 'discountRate1').val(row.discountRate1);
			$(stockBaseId + 'discountRate2').val(row.discountRate2);
			$(stockBaseId + 'discountRate3').val(row.discountRate3);
			$(stockBaseId + 'discountAmount').val(formatMoney(row.discountAmount));
			$(stockBaseId + 'taxRate').val(row.taxRate);
			$(stockBaseId + 'taxRate2').val(row.taxRate2);
			$(stockBaseId + 'taxRate3').val(row.taxRate3);
			$(stockBaseId + 'taxAmount').val(formatMoney(row.taxAmount));
			$(stockBaseId + 'excCode').val(row.excCode);
			$(stockBaseId + 'excRate').val(row.excRate);
			$(stockBaseId + 'excEquivalent').val(formatMoney(row.excEquivalent));
			$(stockBaseId + 'seller').val(row.seller);
			$(stockBaseId + 'seller_id').val(row.seller_id);

			findTotalsForStocks();
		}
	);
	
});

/**************************************************************************/

function findTotalsForStocks() {
	var plusFactor = 0;
	var minusFactor = 0;

	var subtotal = 0;
	var discountTotal = 0;
	var roundingDiscount = 0;
	var taxTotal = 0;
	var total = 0;

	var stockRowCount = document.getElementById('stockTable').rows.length;
	for(var stockRowNo=0;stockRowNo<stockRowCount;stockRowNo++) {
		var stockBaseId = '#details\\['+stockRowNo+'\\]\\_';
		if ($(stockBaseId + 'stock_id').val().length <= 0) break;

		subtotal += parseFloat($(stockBaseId + 'amount').val());
		discountTotal += parseFloat($(stockBaseId + 'discountAmount').val());
	}

	var totalDiscountAmount = 0;
	var totalDiscountRate = $('#totalDiscountRate').val();
	if (isNumber(totalDiscountRate) && totalDiscountRate > 0) {
		totalDiscountAmount = (subtotal * totalDiscountRate) / 100;
		if (discountTotal + totalDiscountAmount > subtotal) {
			discountTotal = subtotal;
		} else {
			discountTotal += totalDiscountAmount;
		}
	}
	
	for(var stockRowNo=0;stockRowNo<stockRowCount;stockRowNo++) {
		var stockBaseId = '#details\\['+stockRowNo+'\\]\\_';
		if ($(stockBaseId + 'stock_id').val().length <= 0) break;

		var factorEffect = 0;
		var plusFactorAmount = 0;
		var minusFactorAmount = 0;
		
		var row = {
			amount: parseDouble($(stockBaseId  + 'amount').val(), 0),
			taxRate: parseDouble($(stockBaseId + 'taxRate').val(), 0),
			taxRate2: parseDouble($(stockBaseId + 'taxRate2').val(), 0),
			taxRate3: parseDouble($(stockBaseId + 'taxRate3').val(), 0),
			discountAmount: parseDouble($(stockBaseId + 'discountAmount').val(), 0),
			excCode: $(stockBaseId + 'excCode').val(),
			excRate: parseDouble($(stockBaseId + 'excRate').val(), 0),
			excEquivalent: 0,
			total: 0,
			taxAmount: 0,
			plusFactorAmount: 0,
			minusFactorAmount: 0
		}
		if (row.excCode == null) row.excCode = mainExcCode;
		
		/**
		* Isi saglama almak icin her toplama isleminde faktor etkileri de hesaplanir
		*/
		if (document.getElementById('factorTable') != undefined) {
			var factorRowCount = document.getElementById('factorTable').rows.length;
			if (factorRowCount > 0) {
				for(var factorRowNo=0;factorRowNo<factorRowCount;factorRowNo++) {
					var factorBaseId = '#factors\\['+factorRowNo+'\\]\\_';
					var factorId = $(factorBaseId + 'factor_id').val();
					if (factorId.length <= 0) break;
					
					var factorInfo = {
						factorType: $(factorBaseId + 'factorTypeOri').val(),
						calcType: $(factorBaseId + 'calcTypeOri').val(),
						effectType: $(factorBaseId + 'effectTypeOri').val(),
						effect: parseDouble($(factorBaseId  + 'effect').val(), 0)
					}
		
					if (factorInfo.factorType === 'Discount') {
						if (factorInfo.effectType === 'Percent') {
							row.minusFactorAmount += (row.amount * factorInfo.effect) / 100;
						} else {
							row.minusFactorAmount += (row.amount / subtotal) * factorInfo.effect;
						}
					} else {
						if (factorInfo.effectType === 'Percent') {
							row.plusFactorAmount += (row.amount * factorInfo.effect) / 100;
						} else {
							row.plusFactorAmount += (row.amount / subtotal) * factorInfo.effect;
						}
					}
		
					if (factorInfo.calcType === 'Exclude') {
						factorEffect = (row.plusFactorAmount - row.minusFactorAmount).roundup(pennyDigits);
					}
					plusFactor += (row.plusFactorAmount).roundup(pennyDigits);
					minusFactor += (row.minusFactorAmount).roundup(pennyDigits);
				}
			}
		}

		var basis = row.amount + factorEffect - row.discountAmount;
		var rowTotalDiscountAmount = 0;
		if (isNumber(totalDiscountRate) && totalDiscountRate > 0) {
			var rowTotalDiscountAmount = (row.amount * totalDiscountRate) / 100;
			basis -= rowTotalDiscountAmount;
		} 

		var taxTot1 = (row.taxRate  > 0 ? (basis * row.taxRate) / 100 : 0);
		var taxTot2 = (row.taxRate2 > 0 ? (basis * row.taxRate2) / 100 : 0);
		var taxTot3 = (row.taxRate3 > 0 ? (basis * row.taxRate3) / 100 : 0);
		row.taxAmount = (taxTot1 + taxTot2 + taxTot3).roundup(pennyDigits);			
		
		row.amount -= rowTotalDiscountAmount;
		
		if ($("#isTaxInclude").val() === 'true') {
			row.total = ((row.amount + factorEffect) + row.taxAmount - row.discountAmount).roundup(pennyDigits);
		} else {
			row.total = ((row.amount + factorEffect) - row.discountAmount).roundup(pennyDigits);
		}
		row.excEquivalent = (row.total * row.excRate).roundup(pennyDigits);
		
		taxTotal += row.taxAmount;
		total += row.excEquivalent;
		
		$(stockBaseId + 'taxAmount').val(formatMoney(row.taxAmount));
		$(stockBaseId + 'excRate').val(row.excRate);
		$(stockBaseId + 'excEquivalent').val(formatMoney(row.excEquivalent));
		$(stockBaseId + 'plusFactorAmount').val(row.plusFactorAmount);
		$(stockBaseId + 'minusFactorAmount').val(row.minusFactorAmount);
		$(stockBaseId + 'total').val(formatMoney(row.total));
	}

	var roundingVal = $("#roundingDigits").val();
	if (roundingVal != undefined && roundingVal.length > 0) {
		var roundingDigits = parseInt(roundingVal);
		if (roundingDigits > 0 && roundingDigits < 4) {
			roundingDiscount = total - (Math.floor(total / Math.pow(10, roundingDigits)) * Math.pow(10, roundingDigits));
		} else {
			roundingDiscount = total - (total | 0);
		}
	}

	/**
	 * Finds totals for factor table
	 */
	/********************************************************************/
	var plusFactorTotal = 0;
	var minusFactorTotal = 0;
	if (document.getElementById('factorTable') != undefined) {
		var rowCount = document.getElementById('factorTable').rows.length;
		
		for(var rowNo=0;rowNo<rowCount;rowNo++) {
			var baseId = '#factors\\['+rowNo+'\\]\\_';
			var factorId = $(baseId + 'factor_id').val();
			if (factorId == undefined) break;
	
			if ($(baseId + 'calcTypeOri').val() === 'Discount') {
				minusFactorTotal += parseDouble($(baseId + 'amount').val(), 0);
			} else {
				plusFactorTotal += parseDouble($(baseId + 'amount').val(), 0);
			}
		}
		$('#minusFactorTotal').val(formatMoney(minusFactorTotal));
		$('#plusFactorTotal').val(formatMoney(plusFactorTotal));
	}
	/********************************************************************/
	var netTotal = subtotal - discountTotal;
	var total = total + plusFactorTotal - minusFactorTotal;

	var withholdingAmount = 0;
	if (typeof withholdingRate != 'undefined' && withholdingRate > 0) {
		$('#withholdingRate').val(withholdingRate);
		if (nettotal >= 1000) {
			withholdingAmount = taxTotal * withholdingRate;
			$('#withholdingAmount').val(formatMoney(withholdingAmount));
		}
	}
	
	$('#total').val(formatMoney(subtotal));
	$('#discountTotal').val(formatMoney(discountTotal));
	$('#subtotal').val(formatMoney(netTotal));
	$('#taxTotal').val(formatMoney(taxTotal));
	$('#roundingDiscount').val(formatMoney(roundingDiscount));

	$('#netTotal').val(formatMoney(total - withholdingAmount - roundingDiscount));
	$('#amount').val(formatMoney(total - withholdingAmount - roundingDiscount));

	try {
		findEquivalent();
	} catch (e) {}
}
