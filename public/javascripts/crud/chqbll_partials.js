var currencies = [];
var exchange_rates;

$.getJSON('/as/get/simple_data',
	function(data) {
		exchange_rates = data.exchange_rates;
		for(var cur in exchange_rates) {
			if (cur != 'info') {
				currencies.push(cur);
			}
		}
	}
);

$("document").ready(function(){
	$("#partialTable").tableAutoAddRow({inputBoxAutoNumber: true, rowNumColumn: "rowNumber", autoAddRow: true}, function(row){
		updateFormRegulators("#partialTable tbody tr");
		calculateFields(0, null);
	});
	$("#partialTable .delRow").btnDelRow(function(row){
		$("#partialTable .date").datepicker("destroy");
		updateFormRegulators("#partialTable tbody tr");
		findTotals();
	});
	
	$("#partialTable").freezeTableColumns({
		width: 920,
		height: 355
	});
	
	$("#partialTable").on({keyup: function(e){
		var rowNo = this.id.match(/\d/);
		var name  = this.id.substring(this.id.indexOf('\_')+1);
		var transDateID = '#details\\['+rowNo+'\\]\\_transDate';

		calculateFields(rowNo, name);
		findTotals();
		if ($(transDateID).val() === '') $(transDateID).val(moment().format('DD/MM/YYYY'));
	}}, "input.attention");
	
});

function calculateFields(rowNo, datafield) {
	var chqbllBaseId = '#details\\['+rowNo+'\\]\\_';

	var row = {
		amount: parseDouble($(chqbllBaseId + 'amount').val(), 0),
		excCode: $('#excCode').val(),
		excEquivalent: 0
	};

	var excRate = 0;
	
	if (global_excCode == row.excCode) {
		excRate = 1;
	} else {
		excRate = parseDouble($(chqbllBaseId + 'excRate').val(), 0);
		if (excRate == 0 || excRate > 10) {
			excRate = (isCustomer ?  exchange_rates[row.excCode].selling : exchange_rates[row.excCode].buying);
		}
	}

	row.excEquivalent = (row.amount * excRate).roundup(pennyDigits);
	$(chqbllBaseId + 'excCode').val(row.excCode);
	$(chqbllBaseId + 'excRate').val(excRate);
	$(chqbllBaseId + 'excEquivalent').val(formatMoney(row.excEquivalent));
}

function findTotals() {
	var paid = 0;
	var remaining = 0;
	var rowCount = document.getElementById('partialTable').rows.length;

	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		var chqbllBaseId = '#details\\['+rowNo+'\\]\\_';
		
		if (isDate($(chqbllBaseId + 'transDate').val()) && $(chqbllBaseId + 'amount').val() > 0) {
			paid += parseFloat($(chqbllBaseId + 'amount').val());
		}
	}

	var amount = parseDouble($('#amount').val());
	$('#total').val(formatMoney(amount) + excCode);
	$('#paid').val(paid);
	$('#remaining').val(amount-paid);
	$('#payment').val(formatMoney(paid) + excCode);
	$('#remain').val(formatMoney(amount-paid) + excCode);
}

findTotals();
