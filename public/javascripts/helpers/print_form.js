function printErrorHandler(xhr, options, error) {
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

function showPrintForm(id, right) {
	jsRoutes.controllers.admin.Documents.showPrintForm(id, right).ajax({
		cache: false,
		success: function(data, status, xhr) {
			$("#printFormFields").html(data);
			$('#printForm').modal('show');
			setPrintFormField();
		},
	    error: function(xhr, options, error) {
	    	printErrorHandler(xhr, options, error);
		}
	});
}

function printDocument() {
	jsRoutes.controllers.admin.Documents.printDocument().ajax({
		cache: false,
		data : $("#printItemForm").serialize(),
		success: function(data, status, xhr) {
			$('#printForm').modal('hide');
		    $.pnotify({
		        title: globalTranslator[xhr.responseText],
		        text: translator[xhr.responseText],
		        type: xhr.responseText
	        });
			$.cookie("printing.@(right).doc", $("#document_id").val(), { expires: 365, path: '/' });
			$.cookie("printing.@(right).target", $("#target_id").val(), { expires: 365, path: '/' });
		},
	    error: function(xhr, options, error) {
	    	printErrorHandler(xhr, options, error);
	    }
	});
}

function setPrintFormField() {
	var doc = $.cookie("printing.@(right).doc");
	var target = $.cookie("printing.@(right).target");

	if (doc != null) $("#document_id").val(doc);
	if (target != null) $("#target_id").val(target);
}
