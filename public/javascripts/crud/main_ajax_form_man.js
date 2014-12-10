
function errorHandler(xhr, options, error) {
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

function show(data, xhr) {
	if (data != null && data.indexOf('name="id"') > 0) {
		$("#form_fields").html(data);
		$('#modal\\-form').modal('show');
		if (hasInsertRight) {
			$('#saveButton').show();
		} else {
			$('#saveButton').hide();
		}

		var recordid = $('#id').val();
		var insertByText = $('#insertBy').val();

		if (userid > 1 && editingLimit != 'Free' && recordid.length > 0 && username != insertByText) {
			$('#elseResLabel').show();
			$('#submitSaveBtn').hide();
		} else {
			$('#elseResLabel').hide();
			$('#submitSaveBtn').show();
		}
		
	} else {
		xhr.status = 400;
		errorHandler(xhr, null, null);
	}
}

function investigation(id) {
	if (hasInvestigation) {
		controller.investigation(id).ajax({
			cache: false,
			success: function(data, status, xhr) {
				$("#inv_title").html('<h4>'+data.title+'</h4>');
				$("#inv_body").html(data.body);
				$('#inv_modal\\-form').modal('show');
			},
		    error: function(xhr, options, error) {
		    	errorHandler(xhr, options, error);
			}
		});
	}
}

function create() {
	if (typeof extraFields_id != 'undefined') {
		controller.create(extraFields_id).ajax({
			cache: false,
			success: function(data, status, xhr) {
				show(data, xhr);
			},
		    error: function(xhr, options, error) {
		    	errorHandler(xhr, options, error);
			}
		});
	} else {
		controller.create().ajax({
			cache: false,
			success: function(data, status, xhr) {
				show(data, xhr);
			},
		    error: function(xhr, options, error) {
		    	errorHandler(xhr, options, error);
			}
		});
	}
}

function edit(id) {
	controller.edit(id).ajax({
		cache: false,
		success: function(data, status, xhr) {
			show(data, xhr);
		},
	    error: function(xhr, options, error) {
	    	errorHandler(xhr, options, error);
		}
	});
}

function save() {
	controller.save().ajax({
		cache: false,
		data : $("#itemForm").serialize(),
		success: function(data, status, xhr) {
			if (! conRecording) {
				$('#modal\\-form').modal('hide');
				list();
			} else {
				show(data, xhr);
				$('input:text:visible:first', $('#modal\\-form')).focus();
				$('#sub_successPanel').show();
				$('#sub_successPanel').delay(2000).fadeOut();
			}
		},
	    error: function(xhr, options, error) {
	    	$("#form_fields").html(xhr.responseText);
	    	updateFormRegulators('#modal\\-form');
	    }
	});
}

function remowe(id, label) {
    bootbox.confirm(label + ' ' + deleteQuestion, function(result) {
        if (result) {
			controller.remove(id).ajax({
				cache: false,
				success: function(data, status, xhr) {
					list();
				},
			    error: function(xhr, options, error) {
			    	errorHandler(xhr, options, error);
				}
			});
        }
	});
}

function list() {
	if (typeof extraFields_id != 'undefined') {
		controller.list(extraFields_id).ajax({
			cache: false,
			success: function(data, status, xhr) {
	            $("#list").html(data);
	            prepareClickableRows();
			},
		    error: function(xhr, options, error) {
		    	errorHandler(xhr, options, error);
			}
		});
	} else {
		controller.list().ajax({
			cache: false,
			success: function(data, status, xhr) {
	            $("#list").html(data);
	            prepareClickableRows();
			},
		    error: function(xhr, options, error) {
		    	errorHandler(xhr, options, error);
			}
		});
	}
}

$(window).keydown(function(event){
	if(event.keyCode == 13) {
    	event.preventDefault();
    	return false;
    }
});

$('#modal\\-form').on('shown', function () {
	$('input:text:visible:first', this).focus();
	updateFormRegulators('#modal\\-form');
});
