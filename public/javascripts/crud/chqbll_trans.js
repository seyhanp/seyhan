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


function fromStepOnChange() {
	toStepOnChange(true);
}

function toStepOnChange(isDependedCall) {
	var ra_contact = document.getElementById("ra_contact");
	var ra_safe = document.getElementById("ra_safe");
	var ra_bank = document.getElementById("ra_bank");
	var ra_exchange = document.getElementById("ra_exchange");

	var fromStep = $('#fromStep').val();
	var toStep = (orgToStep != '' ? orgToStep : $('#toStep').val());

	ra_contact.style.display = "none";
	ra_safe.style.display = "none";
	ra_bank.style.display = "none";
	
	$.getJSON('/as/chqbll/trans_steps?p='+fromStep+'&q='+(isDependedCall ? '' : toStep),
		function(data) {
			if (data.module === 'safe') {
				ra_safe.style.display = "block";
			}
			if (data.module === 'bank') {
				ra_bank.style.display = "block";
			}
			if (data.module === 'contact') {
				ra_contact.style.display = "block";
			}

			if (isDependedCall) {
				$("#toStep").empty();
				for(var key in data.steps) {
					var value = data.steps[key];
					$('#toStep').append($("<option/>", {value: key, text: value}));
				}
			}

			if (orgToStep != '') {
				$('#toStep').val(orgToStep);
				orgToStep = '';
				toStepOnChange(false);
			}
		}
	);
}

$(document).ready(function(){

	$("#chqbllTable").tableAutoAddRow({inputBoxAutoNumber: true, rowNumColumn: "rowNumber", autoAddRow: true}, function(row){
		findTotals();
	});

	$("#chqbllTable .delRow").btnDelRow(function(row){
		findTotals();

		var lastId = $('#virtuals\\['+(document.getElementById('chqbllTable').rows.length-1)+'\\]\\_id').val();
		if (lastId != '') $("#chqbllTable").btnManuelAddRow();
	});

	$("#chqbllTable").freezeTableColumns({
		width: 900,
		height: 410
	});
	
	$('form').bind('submit', function() {
        $('#fromStep').removeAttr('disabled');
        $('#toStep').removeAttr('disabled');
    });
	
	if ($('#id').val() != '') {
        $('#fromStep').attr('disabled', 'true');
        $('#toStep').attr('disabled', 'true');
	}

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

	fromStepOnChange();

	$("#fromStep").on({
		keyup: function(e){
			fromStepOnChange();
		}
	});

	$("#fromStep").on({
		change: function(e){
			fromStepOnChange();
		}
	});

	$("#toStep").on({
		keyup: function(e){
			toStepOnChange(false);
		}
	});

	$("#toStep").on({
		change: function(e){
			toStepOnChange(false);
		}
	});

	$("body").removeClass("loading");
});

/**************************************************************************/

function calculateAllFields() {
	var rowCount = document.getElementById('chqbllTable').rows.length;
	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		calculateFields(rowNo);
	}

	findTotals();
}

function switchStepCombos(status) {
    if (document.getElementById('chqbllTable').rows.length <= 1  && document.getElementById('id').value.length == 0) {
        $('#fromStep').removeAttr('disabled');
        $('#toStep').removeAttr('disabled');
    } else {
        $('#fromStep').prop('disabled', 'disabled');
        $('#toStep').prop('disabled', 'disabled');
    }
}

/**************************************************************************/

function calculateFields(rowNo) {
	var chqbllBaseId = '#virtuals\\['+rowNo+'\\]\\_';
	
	var row = {
		amount: parseDouble($(chqbllBaseId + 'amount').val(), 0),
		excCode: $(chqbllBaseId + 'excCode').val(),
		excRate: parseDouble($(chqbllBaseId + 'excRate').val(), 0),
		excEquivalent: 0
	};
	if (row.excCode == null) row.excCode = mainExcCode;
	
	if (row.excRate <= 0 || row.excRate > 5) {
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
	if (! isDateValid(baseDate)) baseDate = moment();
	
	var rowCount = document.getElementById('chqbllTable').rows.length;

	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		var chqbllBaseId = '#virtuals\\['+rowNo+'\\]\\_';
		
		if (isDate($(chqbllBaseId + 'dueDate').val())
		&&  $(chqbllBaseId + 'excEquivalent').val() > 0) {
			var dueDate = moment($(chqbllBaseId + 'dueDate').val(), 'DD/MM/YYYY');
			if (! isDateValid(dueDate)) {
				continue;
			}
			var days = dueDate.diff(baseDate, 'days');
			dayDistanceList[rowNo] = days;
			totalDays += days;
			total += parseFloat($(chqbllBaseId + 'excEquivalent').val());
		}
	}
	var factor = totalDays / total;

	var totalEffects = 0;
	for(var rowNo=0;rowNo<rowCount;rowNo++) {
		var chqbllBaseId = '#virtuals\\['+rowNo+'\\]\\_';
		if (isDate($(chqbllBaseId + 'dueDate').val())
		&&  $(chqbllBaseId + 'excEquivalent').val() > 0) {
			var amount = parseFloat($(chqbllBaseId + 'excEquivalent').val());
			var degree = factor * amount * dayDistanceList[rowNo];
			var effect = degree / totalDays;
			totalEffects += effect;
		}
	}			
	
	$('#rowCount').val(rowCount-1);
	$('#avarageDate').val(baseDate.add(totalEffects, 'days').format('DD/MM/YYYY'));
	$('#adat').val(totalEffects|0);
	$('#total').val(formatMoney(total));
	
	switchStepCombos();
}

/*************************************************************************
         SELECTION SECTION
***************************************************************************/

var selection_controller = jsRoutes.controllers.chqbll.AjaxService;

function selection_create() {
	var ids = [];
	$('.detail_id').each(function(index, entry) {
		if ($(this).val() != '') ids.push($(this).val());
	});		
	
	selection_controller.create(sort, right, $('#fromStep').val(), ids.toString()).ajax({
		cache: false,
		success: function(data, status, xhr) {
			$("#selection_form_fields").html(data);
			$('#modal\\-selection').modal('show');
			updateFormRegulators('#modal\\-selection');
		}
	});
}

function selection_search() {
	selection_controller.search().ajax({
		cache: false,
		data : $("#selectionForm").serialize(),
		success: function(data, status, xhr) {
			$("#selection_search_result").html(data);
		}
	});
}

function selection_clear() {
	$("#selection_search_result").html("");
}

function selection_transfer() {
	$('.case:checked').each(function(index, entry) {

		var isExist = false;
		var selectedId = parseInt($(this).attr('id').match(/[\d\.]+/));
		var selectedValue = $('#subDetails\\['+selectedId+'\\]\\_id').val();
		
		$('.impid').each(function(index, entry) {
			if ($(this).val() === selectedValue) {
				isExist = true;
				return false;
			}
		});

		if (! isExist) {
			$("#chqbllTable").btnManuelAddRow(
				function(rowIndex) {
					var virtualId = '#virtuals\\['+rowIndex+'\\]\\_';
					var subdetailId = '#subDetails\\['+selectedId+'\\]\\_';
	
					$(virtualId + 'id').val($(subdetailId + 'id').val());
					$(virtualId + 'lastStep').val($('#fromStep').val());
					$(virtualId + 'portfolioNo').val($(subdetailId + 'portfolioNo').val());
					$(virtualId + 'serialNo').val($(subdetailId + 'serialNo').val());
					$(virtualId + 'dueDate').val($(subdetailId + 'dueDate').val());
					$(virtualId + 'amount').val(formatMoney(parseFloat($(subdetailId + 'amount').val())));
					$(virtualId + 'owner').val($(subdetailId + 'owner').val());
					$(virtualId + 'paymentPlace').val($(subdetailId + 'paymentPlace').val());
					$(virtualId + 'excCode').val($(subdetailId + 'excCode').val());
					$(virtualId + 'excRate').val($(subdetailId + 'excRate').val());
					$(virtualId + 'excEquivalent').val($(subdetailId + 'excEquivalent').val());
					$(virtualId + 'bankName').val($(subdetailId + 'bankName').val());
					$(virtualId + 'bankBranch').val($(subdetailId + 'bankBranch').val());
					$(virtualId + 'bankAccountNo').val($(subdetailId + 'bankAccountNo').val());
					$(virtualId + 'correspondentBranch').val($(subdetailId + 'correspondentBranch').val());
					$(virtualId + 'surety').val($(subdetailId + 'surety').val());
					$(virtualId + 'suretyAddress').val($(subdetailId + 'suretyAddress').val());
					$(virtualId + 'suretyPhone1').val($(subdetailId + 'suretyPhone1').val());
					$(virtualId + 'suretyPhone2').val($(subdetailId + 'suretyPhone2').val());
					$(virtualId + 'cbtypeName').val($(subdetailId + 'cbtypeName').val());
					$(virtualId + 'description').val($(subdetailId + 'description').val());
				}
			);
		}
	});
	findTotals();
	
	$("#selection_search_result").html("");
}
