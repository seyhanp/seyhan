var ccp = $('#ccp');
var ccp_op = $('#ccp_op');
var ccp_msg = $('#ccp_msg');
var ccp_selected = $('#ccp_selected_id');

var selected;
var selected_parent;

$(document).ready(function() {
	$("#tree").dynatree({
		clickFolderMode: 0,
	    autoCollapse: true,
		fx: { height: "toggle", duration: 100 },
		initAjax: {url: '/as/' + methodName},
	    onDblClick: function(node, event) {
	        edit();
	    },
	    onActivate: function(node) {
	        selected = node.data.key;
	        if (node.getParent()) {
	        	selected_parent = node.getParent().data.key;
	        }
	    },
	    onPostInit: function (isReloading, isError) {
	        if (selected) {
	        	var tree = $("#tree").dynatree("getTree");
	    		var node = tree.getNodeByKey(selected);
	    		if (node == undefined) node = tree.getNodeByKey(selected_parent);
	    		if (node != undefined) {
		    		node.activate();
		    		node.expand(true);
	    		}
	        }
	    }
	});
});

function errorHandler(xhr, options, error) {
	if (xhr.status == 400) {
		showError(xhr.responseText);
	} else if (xhr.status == 403) { 
        window.location.href = '/forbidden'; 
    } else {
    	showError(disconnectMsg);
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

function create(isRoot) {
	controller.create((! isRoot && selected != undefined && selected.length > 0 ? selected : -1)).ajax({
		cache: false,
		success: function(data, status, xhr) {
			show(data, xhr);
		},
	    error: function(xhr, options, error) {
	    	errorHandler(xhr, options, error);
		}
	});
}

function edit() {
	var tree = $("#tree").dynatree("getTree");
	var node = tree.getActiveNode();

	if (node == undefined) {
		showError(catNotFound);
		return false;
	}

	controller.edit(node.data.key).ajax({
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
			$('#modal\\-form').modal('hide');
			refresh();
		},
	    error: function(xhr, options, error) {
	    	errorHandler(xhr, options, error);
	    }
	});
}

function remowe() {
	var tree = $("#tree").dynatree("getTree");
	var node = tree.getActiveNode();

	if (node == undefined) {
		showError(catNotFound);
		return false;
	}
	
    bootbox.confirm(node.data.title + ' ' + deleteQuestion, function(result) {
        if (result) {
    		controller.remove(node.data.key).ajax({
    			cache: false,
    			success: function(data, status, xhr) {
    				$("#success_pane").html(data);
    				tree.reload();
    			},
    		    error: function(xhr, options, error) {
    		    	errorHandler(xhr, options, error);
    			}
    		});
        }
    });
}

function showError(message) {
	$.pnotify({title: errorTitle, text: message, type: "error", animation: 'show'});
}

function refresh() {
	var tree = $("#tree").dynatree("getTree");
	tree.reload();
}

function kopy(op) {
	ccp.show();
	ccp_op.val(op);
	
	var tree = $("#tree").dynatree("getTree");
	var node = tree.getNodeByKey(selected);
	ccp_msg.html(node.data.title + " " + editCopied);
	ccp_selected.val(node.data.key);
}

function cut() {
	ccp.show();
	ccp_op.val(9);

	var tree = $("#tree").dynatree("getTree");
	var node = tree.getNodeByKey(selected);
	ccp_msg.html(node.data.title + " " + editKut);
	ccp_selected.val(node.data.key);
}

function paste() {
	if (selected == undefined) {
		showError(catNotFound);
		return false;
	}
	controller.paste(ccp_selected.val(), selected, ccp_op.val()).ajax({
		cache: false,
		success: function(data, status, xhr) {
			clearCCPBlock();
			refresh();
		},
	    error: function(xhr, options, error) {
	    	errorHandler(xhr, options, error);
		}
	});
}

clearCCPBlock();

function clearCCPBlock() {
	ccp.hide();
	ccp_op.val('');
	ccp_msg.html('');
	ccp_selected.val('');
}

$('#modal\\-form').on('shown', function () {
	$('input:text:visible:first', this).focus();
});

$('#modal\\-form').on({keypress: function(e){
	if (e.keyCode == 10 || e.keyCode == 13) { 
        e.preventDefault();
    }
}}, "input");
