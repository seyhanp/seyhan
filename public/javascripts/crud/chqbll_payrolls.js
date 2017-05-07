$("body").addClass("loading");

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

$(document).ready(function(){

	$("#chqbllTable").tableAutoAddRow({inputBoxAutoNumber: true, rowNumColumn: "rowNumber", autoAddRow: true}, function(row){
		updateFormRegulators("#chqbllTable tbody tr");
	});
	$("#chqbllTable .delRow").btnDelRow(function(row){
		$("#chqbllTable .date").datepicker("destroy");
		updateFormRegulators("#chqbllTable tbody tr");
		calculateAllFields();
	});

	$("#chqbllTable").freezeTableColumns({
		width: 900,
		height: (isOpening ? 320 : 410)
	});
	
	$("#chqbllTable").on({keyup: function(e){
		var rowNo = this.id.match(/\[(.*?)\]/)[1];
		var name  = this.id.substring(this.id.indexOf('\_')+1);
		
		calculateFields(rowNo, name);
		findTotals();
	}}, "input.attention");

	$("#chqbllTable").on({change: function(e){
		var rowNo = this.id.match(/\[(.*?)\]/)[1];
		var name  = this.id.substring(this.id.indexOf('\_')+1);
		
		calculateFields(rowNo, name);
		findTotals();
	}}, "select.attention");

	$("#chqbllTable").on({click: function(e){
		var id = $(this).nextAll('.impid:first').val();
		if (id != undefined && id.length > 0) {
			$.getJSON('/as/chqbll/investigation/'+id,
				function(data) {
					$("#inv_title").html('<h4>'+data.title+'</h4>');
					$("#inv_body").html(data.body);
		
					$('#inv_modal\\-form').modal('show');
				}
			);
		}
	}}, ".invid");

	updateFormRegulators("#chqbllTable tbody tr");

	if (isOpening) {
		$(document).on('selectContactEvent', function(event, data) {
			if (data == undefined || data.id.length < 1) return false;
		
			$("#chqbllTable").btnManuelAddRow(
				function(rowIndex) {
					var row = {};
					var detailId = '#details\\['+rowIndex+'\\]\\_';
		
					updateFormRegulators("#chqbllTable tbody tr");
		
					$(detailId + 'contact_id').val(data.id);
					$(detailId + 'lastContactName').val(data.name);
					$(detailId + 'portfolioNo').val(lastPortfolioNo+rowIndex);
				}
			);
		});
	}

	$("body").removeClass("loading");
});

/**************************************************************************/

function calculateAllFields() {
	var rowCount = document.getElementById('chqbllTable').rows.length;
	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		calculateFields(rowNo, 'due_date');
	}

	findTotals();
}

/**************************************************************************/

function calculateFields(rowNo, datafield) {
	var chqbllBaseId = '#details\\['+rowNo+'\\]\\_';
	
	var row = {
		amount: parseDouble($(chqbllBaseId + 'amount').val(), 0),
		excCode: $(chqbllBaseId + 'excCode').val(),
		excRate: parseDouble($(chqbllBaseId + 'excRate').val(), 0),
		excEquivalent: 0
	};
	if (row.excCode == null) row.excCode = mainExcCode;
	
	if (datafield === 'excCode' || row.excRate <= 0 || row.excRate > 5) {
		row.excRate = exchange_rates[row.excCode].buying;
	}
	row.excEquivalent = (row.amount * row.excRate).roundup(pennyDigits);

	$(chqbllBaseId + 'excRate').val(row.excRate);
	$(chqbllBaseId + 'excEquivalent').val(formatMoney(row.excEquivalent));
}

/**************************************************************************/

function findTotals() {
	var portInd = 0;
	var total = 0;
	var totalDays = 0;
	var dayDistanceList = new Array();

	var baseDate = moment($("#transDate").val(), 'DD/MM/YYYY');
	var rowCount = document.getElementById('chqbllTable').rows.length;

	//checks whether base date is before any due dates. if so, base date is changed with the earliest due date.
	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		var chqbllBaseId = '#details\\['+rowNo+'\\]\\_';
		if (isDate($(chqbllBaseId + 'dueDate').val())) {
			var dueDate = moment($(chqbllBaseId + 'dueDate').val(), 'DD/MM/YYYY');
			if (dueDate.isBefore(baseDate)) baseDate = dueDate;
		}
	}

	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		var chqbllBaseId = '#details\\['+rowNo+'\\]\\_';
		
		if ($(chqbllBaseId + 'id').val() === "") {
			$(chqbllBaseId + 'portfolioNo').val(lastPortfolioNo+portInd);
			portInd++;
		}
		
		if (isDate($(chqbllBaseId + 'dueDate').val())
		&&  $(chqbllBaseId + 'excEquivalent').val() > 0) {
			var dueDate = moment($(chqbllBaseId + 'dueDate').val(), 'DD/MM/YYYY');
			var days = dueDate.diff(baseDate, 'days');
			dayDistanceList[rowNo] = days;
			totalDays += days;
			total += parseFloat($(chqbllBaseId + 'excEquivalent').val());
		}
	}

	var factor = 0;
	if (totalDays * total > 0) {
		factor = totalDays / total;
	}

	var totalEffects = 0;
	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		var chqbllBaseId = '#details\\['+rowNo+'\\]\\_';
		if (isDate($(chqbllBaseId + 'dueDate').val())
		&&  $(chqbllBaseId + 'excEquivalent').val() > 0) {
			var amount = parseFloat($(chqbllBaseId + 'excEquivalent').val());
			var degree = factor * amount * dayDistanceList[rowNo];
			if (degree * totalDays > 0) {
				totalEffects += (degree / totalDays);
			}
		}
	}

	$('#rowCount').val(rowCount-1);
	$('#avarageDate').val(baseDate.add(totalEffects, 'days').format('DD/MM/YYYY'));
	$('#adat').val(totalEffects|0);
	$('#total').val(formatMoney(total));
	
	try {
		findEquivalent();
	} catch (e) {}

}

/*************************************************************************
         SEQUENTIAL SECTION
***************************************************************************/

var sequential_controller = jsRoutes.controllers.chqbll.AjaxService;

function sequential_form_open() {
	sequential_controller.sequentialForm(right).ajax({
		cache: false,
		success: function(data, status, xhr) {
			$("#sequential_form_fields").html(data);
			$('#modal\\-sequential').modal('show');
			updateFormRegulators('#modal\\-sequential');
		}
	});
}

function sequential_create() {
	sequential_controller.sequentialValidation(sort, isCustomer).ajax({
		cache: false,
		data : $("#sequantialForm").serialize(),
		success: function(data, status, xhr) {
			
			var count = data.portfolioNo;
			var baseDate = moment($('#dueDate').val(), 'DD/MM/YYYY');

			for (var i=0; i<count; i++) {
				$("#chqbllTable").btnManuelAddRow(
					function(rowIndex) {
						var detailId = '#details\\['+rowIndex+'\\]\\_';
						
						$(detailId + 'portfolioNo').val(lastPortfolioNo+rowIndex);
						$(detailId + 'serialNo').val(data.serialNo);
						if (i == 0) {
							$(detailId + 'dueDate').val(baseDate.format('DD/MM/YYYY'));
						} else {
							switch (data.dueYear) {
								case 0: {
									$(detailId + 'dueDate').val(baseDate.add("days", data.rowNo).format('DD/MM/YYYY'));
									break;
								}
								case 1: {
									$(detailId + 'dueDate').val(baseDate.add("months", data.rowNo).format('DD/MM/YYYY'));
									break;
								}
								case 2: {
									$(detailId + 'dueDate').val(baseDate.add("weeks", data.rowNo).format('DD/MM/YYYY'));
									break;
								}
							}
						}
						$(detailId + 'amount').val(formatMoney(data.amount));
						$(detailId + 'owner').val(data.owner);
						$(detailId + 'paymentPlace').val(data.paymentPlace);
						if (data.excCode == null) data.excCode = mainExcCode;
						$(detailId + 'excCode').val(data.excCode);
						if (isCustomer) {
							data.excRate = exchange_rates[data.excCode].selling;
						} else {
							data.excRate = exchange_rates[data.excCode].buying;
						}
						$(detailId + 'excRate').val(data.excRate);
						$(detailId + 'excEquivalent').val(formatMoney(data.amount*data.excRate));
						$(detailId + 'bankName').val(data.bankName);
						$(detailId + 'bankBranch').val(data.bankBranch);
						$(detailId + 'bankAccountNo').val(data.bankAccountNo);
						$(detailId + 'correspondentBranch').val(data.correspondentBranch);
						$(detailId + 'surety').val(data.surety);
						$(detailId + 'suretyAddress').val(data.suretyAddress);
						$(detailId + 'suretyPhone1').val(data.suretyPhone1);
						$(detailId + 'suretyPhone2').val(data.suretyPhone2);
						$(detailId + 'description').val(data.description);
						if (data.bank != null) $(detailId + 'bank_id').val(data.bank.id);
						if (data.cbtype != null) $(detailId + 'cbtype_id').val(data.cbtype.id);
					}
				);
			}
			findTotals();
			
			$('#modal\\-sequential').modal('hide');
			updateFormRegulators("#chqbllTable tbody tr");
		},
		error: function (xhr, status, error) {
			$("#sequential_form_fields").html(xhr.responseText);
			updateFormRegulators('#modal\\-sequential');
		}
	});
}
