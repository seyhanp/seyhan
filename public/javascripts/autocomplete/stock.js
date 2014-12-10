var stock_val = $('#stock_id');

if (stock_val != undefined) {
	if (stock_val.val() != undefined && stock_val.val().length < 1) {
		$('#stock_cancel').hide();
	}
}

$(document).ready(function() {
	$('#stock_name').keyup(function(event){
		if (event.keyCode == 8 || event.keyCode == 46) $('#stock_id').val("");
		if (event.keyCode == 13) {
			
			var choice = getStockSearchChoice();
			if (choice != 'barcode' || $(this).val().length < 1) return;

			var contactId = "";
			try {
				contactId = $('#contact_id').val();
			} catch (e) { }
			if (contactId.length < 1) contactId = -1;

			$.get( "/as/find_stock/" + $(this).val() + "/" + contactId, function(item) {
		        if (! isContinuouslyAddedForStock) {
		        	$('#stock_name').val(item.name);
		        	$('#stock_id').val(item.id);
		        } else {
		    		$('#stock_id').val("");
		    		$('#stock_name').val("");
		        }
				$('#stock_cancel').hide();
				$(document).trigger('stock_select', item);
			});
		}
	});

	$('#stock_name').dblclick(function(event){
		var baseID = findBaseID(this.id);
		var id = $(baseID + '_id').val();
		if (id.length > 0) {
			window.open('/stocks/stock/'+id, 'stock');
		} else {
			window.open('/stocks/stock/new', 'stock');
		}
	});

	$('.stock').dblclick(function(event){
		var baseID = this.id.replace('name', 'stock_id').replace('[', '\\[').replace(']', '\\]');
		var id = $('#'+baseID).val();
		if (id.length > 0) {
			window.open('/stocks/stock/'+id, 'stock');
		} else {
			window.open('/stocks/stock/new', 'stock');
		}
	});
	
	$('#stock_name').typeahead({
		source: function(q, process) {

			var choice = getStockSearchChoice();
			if (choice === 'barcode') return;

			var contactId = "";
			try {
				contactId = $('#contact_id').val();
			} catch (e) { }
			if (contactId === undefined || contactId.length < 1) contactId = -1;

	        return $.post('/as/autocomplete/stock?q='+q + '&d='+choice + '&c='+contactId, function(response) {
	        	var result = [];
	        	for(i = 0; i < response.length; i++) {
	        	    result.push(JSON.stringify(response[i]));
	        	}

	            return process(result);
	        });
	    },
	    highlighter: function(item) {
	    	var row = JSON.parse(item);
	        return (row.code + " - " +  row.name).substring(0,38);
	    },
	    updater: function(item) {
	    	var row = JSON.parse(item);
	    	
	        if (! isContinuouslyAddedForStock) {
	        	$('#stock_id').val(row.id);
	        } else {
	    		$('#stock_id').val("");
	    		$('#stock_name').val("");
	        }
	        $('#stock_cancel').show();
	        $(document).trigger('stock_select', row);
	        
	        if (isContinuouslyAddedForStock)
	        	return '';
	        else
	        	return row.name;
	    },
	    items:12
	});
	
	$('#stock_cancel').click(function() {
		$('#stock_id').val("");
		$('#stock_name').val("");
		$('#stock_name').focus();
        $('#stock_cancel').hide();
        $(document).trigger('stock_select');
	});
	
	$('#stock_label').unbind('click').bind('click', function (e) {
		var oldValue = getStockSearchChoice();
		
		var newValue;
		
		if (oldValue === 'code') newValue = 'name';
		if (oldValue === 'name') newValue = 'barcode';
		if (oldValue === 'barcode') newValue = 'code';
		
		changeStockPerspective(newValue);
			
	});
	
	changeStockPerspective(getStockSearchChoice());
	
	function changeStockPerspective(choice) {
		if (choice === 'name' || choice === 'code') {
			$('#stock_name').addClass('typeahead');
		} else {
			$('#stock_name').removeClass('typeahead');
		}
		
		$('#stock_label').html(stockLabels[choice] + ' <b class="caret"></b>');
		$.cookie("stock.search.choice", choice, { expires: 365, path: '/' });
	}
	
	function getStockSearchChoice() {
		var choice = $.cookie("stock.search.choice");
		
		if (choice == null) 
			return 'name';
		else
			return choice;
	}
});
