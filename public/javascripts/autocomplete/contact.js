
var contact_val = $('#contact_id');
var refContact_val = $('#refContact_id');

if (contact_val != undefined) {
	if (contact_val.val() != undefined && contact_val.val().length < 1) {
		$('#contact_inv').hide();
	}
}
if (refContact_val != undefined) {
	if (refContact_val.val() != undefined && refContact_val.val().length < 1) $('#refContact_inv').hide();
}

function investigation(baseID) {
	var id = $('#'+baseID).val();

	if (id != undefined && id.length > 0) {
		$.getJSON('/contacts/investigation/'+id,
			function(data) {
				$("#inv_title").html('<h4>'+data.title+'</h4>');
				$("#inv_body").html(data.body);
	
				$('#inv_modal\\-form').modal('show');
			}
		);
	}
}

function findBaseID(name) {
	return '#' + name.split('\_')[0];
}

$(document).ready(function() {
	
	$('#contact_name,#refContact_name').on('dblclick', function(event){ 
		var baseID = findBaseID(this.id);
		var id = $(baseID + '_id').val();
		
		var win = null;
		if (id.length > 0) {
			win = window.open('/contacts/contact/'+id, 'contact');
		} else {
			win = window.open('/contacts/contact/new', 'contact');
		}
		win.focus();
	});

	$('#contact_name,#refContact_name').keyup(function(event){
		var baseID = findBaseID(this.id);
		if (event.keyCode == 8 || event.keyCode == 46 || (event.keyCode >= 48 && event.keyCode <= 90)) {
			$(baseID + '_id').val("");
			$(baseID + '_data').val("");
			$(baseID + '_inv').hide();
			$(document).trigger('selectContactEvent');
		}
	});
	
	$('#contact_name,#refContact_name').typeahead({
		source: function(q, process){
			var choice = getContactSearchChoice();
	        return $.post('/as/autocomplete/contact?q='+q + '&d='+$('#direction').val() + '&choice='+choice, function(response) {
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
	    	
	    	var baseID = findBaseID(this.$element[0].id);
	    	
	        $(baseID + '_id').val(row.id);
	        $(baseID + '_data').val(item);
	        $(baseID + '_inv').show();
	        $(document).trigger('selectContactEvent', row);

	        if (typeof setRate == 'function') {
	        	var tip = $('#right').val();
		        if (row.excCode.trim() != '' && tip.substring(0,4) != 'FATR') $('#excCode').val(row.excCode);
		        setRate();
	        }
	        
	        if (isContinuouslyAddedForContact)
	        	return '';
	        else
	        	return row.name;
	    },
	    items:12
	});

	$('#contact_label,#refContact_label').unbind('click').bind('click', function (e) {
		var oldValue = getContactSearchChoice();
		
		var newValue;
		
		if (oldValue === 'code') newValue = 'name';
		if (oldValue === 'name') newValue = 'code';
		
		changeContactPerspective(newValue);
			
	});
	
	changeContactPerspective(getContactSearchChoice());
	
	function changeContactPerspective(choice) {
		if (choice === 'name' || choice === 'code') {
			$('#contact_name').addClass('typeahead');
			$('#refContact_name').addClass('typeahead');
		} else {
			$('#contact_name').removeClass('typeahead');
			$('#refContact_name').removeClass('typeahead');
		}
		
		$('#contact_label,#refContact_label').html(contactLabels[choice] + ' <b class="caret"></b>');
		$.cookie("contact.search.choice", choice, { expires: 365, path: '/' });
	}
	
	function getContactSearchChoice() {
		var choice = $.cookie("contact.search.choice");
		
		if (choice == null) 
			return 'name';
		else
			return choice;
	}
});