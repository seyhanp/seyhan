
function simple_errorHandler(xhr, options, error) {
	if (xhr.status == 400) { 
	    $.pnotify({
	        title: globalTranslator['error'],
	        text: xhr.responseText,
	        type: 'error'
        });
	} else if (xhr.status == 403) { 
        window.location.href = '/forbidden'; 
    } else {		
    	alert(xhr.responseText);
    }
}

function simple_show(data, xhr) {
	if (data != null && data.indexOf('name="id"') > 0) {
		$("#simple\\-form_fields").html(data);
		$('#simple\\-modal\\-form').modal('show');
		$('#simple\\-modal\\-form').on('shown.bs.modal', function () {
			$('input:text:visible:first', this).focus();
			updateFormRegulators();
		});
	} else {
		xhr.status = 400;
		simple_errorHandler(xhr, null, null);
	}
}

function simple_reload_options() {
	var func = null;

	switch (simple_combo_type) {
		case 0: { //parametresiz clisanlar icin: BankExpense
			func = simple_controller.options();
			break;
		}
		case 1: { //hasBlankOption ile clisanlar icin: Bank
			func = simple_controller.options(has_simple_blank_option);
			break;
		}
		case 2: { //right ile clisanlar icin: Bank.TransSource
			var right = $('#right').val();
			if (right != undefined || right.length > 0) {
				func = simple_controller.options(right);
			}
			break;
		}
		case 3: { //belirsiz parametre ile clisanlar icin: Stock.Unit
			func = simple_controller.options(simple_addition_of_name);
			break;
		}
	}

	has_simple_blank_option = true;
	simple_addition_of_name = null;

	func.ajax({
		cache: false,
		success: function(data, status, xhr) {
			$(simple_selector).html(data);
		},
	    error: function(xhr, options, error) {
	    	simple_errorHandler(xhr, options, error);
		}
	});
}

function simple_create(extraParams) {
	if (extraParams == null) {
		simple_controller.create().ajax({
			cache: false,
			success: function(data, status, xhr) {
				simple_show(data, xhr);
			},
		    error: function(xhr, options, error) {
		    	simple_errorHandler(xhr, options, error);
			}
		});
	} else {
		simple_controller.create(extraParams).ajax({
			cache: false,
			success: function(data, status, xhr) {
				simple_show(data, xhr);
			},
		    error: function(xhr, options, error) {
		    	simple_errorHandler(xhr, options, error);
			}
		});
	}
}

function simple_save() {
	simple_controller.save().ajax({
		cache: false,
		data : $("#simple\\-ajax\\-form").serialize(),
		success: function(data, status, xhr) {
			$('#simple\\-modal\\-form').modal('hide');
			simple_reload_options();
		},
	    error: function(xhr, options, error) {
	    	$("#simple\\-form_fields").html(xhr.responseText);
	    	updateFormRegulators();
	    }
	});
}
