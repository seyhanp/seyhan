function userInfoErrorHandler(xhr, options, error) {
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

function userInfoEdit() {
	jsRoutes.controllers.admin.Users.editRestricted().ajax({
		cache: false,
		success: function(data, status, xhr) {
			$("#userInfoFormFields").html(data);
			$('#userInfoForm').modal('show');
		},
	    error: function(xhr, options, error) {
	    	userInfoErrorHandler(xhr, options, error);
		}
	});
}

function userInfoSave() {
	jsRoutes.controllers.admin.Users.saveRestricted().ajax({
		cache: false,
		data : $("#userInfoItemForm").serialize(),
		success: function(data, status, xhr) {
			$('#userInfoForm').modal('hide');
		    $.pnotify({
		        title: globalTranslator['success'],
		        text: xhr.responseText,
		        type: 'success'
	        });
		},
	    error: function(xhr, options, error) {
	    	$("#userInfoFormFields").html(xhr.responseText);
	    }
	});
}
