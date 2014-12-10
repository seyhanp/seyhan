var exchange_rates;

$.getJSON('/as/get/exchange_rates', 
		function(data) {
			exchange_rates = data;
			$('#exchange_info').html(exchange_rates['info'].name);
		}
);

function findEquivalent() {
	var amount = parseFloat($(amountField).val());
	var rate = parseFloat($('#excRate').val());

	var equivalent = 0;
	if (rate == 1.0)
		equivalent = amount;
	else
		equivalent = rate * amount;

	if (isNumber(equivalent)) {
		$('#excEquivalent').val(formatMoney(equivalent.roundup(pennyDigits)));
	} else {
		$('#excEquivalent').val(formatMoney(0));
	}
	
	findRefEquivalent();
}

function setRate() {
	if (isBuying) {
		$('#excRate').val(exchange_rates[$('#excCode').val()].buying.roundup(5));
	} else {
		$('#excRate').val(exchange_rates[$('#excCode').val()].selling.roundup(5));
	}
	findEquivalent();
}

$('#excCode').keyup(function(event) {
	if (event.keyCode > 32 && event.keyCode < 41)
		setRate();
});

$('#excRate').keyup(function(event) {
	findEquivalent();
});

$(amountField).keyup(function(event) {
	findEquivalent();
});


function findRefEquivalent() {
	var amount = parseFloat($(amountField).val());
	var rate = parseFloat($('#excRate').val());
	var excEquivalent = parseFloat($('#excEquivalent').val());
	var refRate = parseFloat($('#refExcRate').val());

	if (isNaN(rate)) rate = 1;
	
	var equivalent = (rate/refRate) * amount;
	
	if (isNumber(equivalent)) {
		$('#refExcEquivalent').val(formatMoney(equivalent.roundup(pennyDigits)));
	} else {
		$('#refExcEquivalent').val(formatMoney(0));
	}
}

function setRefRate() {
	if(isBuying) {
		$('#refExcRate').val(exchange_rates[$('#refExcCode').val()].buying.roundup(5));
	} else {
		$('#refExcRate').val(exchange_rates[$('#refExcCode').val()].selling.roundup(5));
	}
	findRefEquivalent();
}

$('#excEquivalent').change(function(){
	findRefEquivalent();
});

$('#refExcCode').keyup(function(event){
	if (event.keyCode > 32 && event.keyCode < 41) setRefRate();
});

$('#refExcRate').keyup(function(event){
	findRefEquivalent();
});
