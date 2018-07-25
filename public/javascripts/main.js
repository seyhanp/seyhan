
function isNumber(n) {
	return !isNaN(parseFloat(n)) && isFinite(n);
}

function updateFormRegulators(selector) {
	try {
		$((selector ? selector : "") + " .currency").autoNumeric({lZero: 'deny', aSep:'', mDec: pennyDigits});
		$((selector ? selector : "") + " .rate").autoNumeric({lZero: 'keep', aSep:'', vMin: '-99.99', vMax: '100', aPad: false});
		$((selector ? selector : "") + " .amount").autoNumeric({lZero: 'keep', aSep:'', vMin: '0', mDec: '5', aPad: false});
		$((selector ? selector : "") + " .number").autoNumeric({lZero: 'deny', aSep:'', mDec:'0'});

		$((selector ? selector : "") + " .month").inputmask("9999-99", {clearIncomplete: true, placeholder: ""});
		$((selector ? selector : "") + " .date").inputmask("dd/mm/yyyy", {clearIncomplete: true, placeholder: "", yearrange: {minyear: 2000, maxyear: 2099}});
		
		$((selector ? selector : "") + " .date").each(function(){
			$(this).datepicker({
				dateFormat: 'dd/mm/yyyy', 
				showOn: "button", 
				buttonImage: "/assets/img/calPickerIcon.png", 
				buttonImageOnly: true,
				changeMonth: true,
				changeYear: true
			});
		});
		
	    $((selector ? selector : "") + " input[type='text'], select").bind("keypress", function(e) {
	        if (e.keyCode == 13 && e.target.type != "submit" && ! $(e.target).hasClass('noenter')) {
	        	if (! $(e.target).hasClass('has_special_enter')) {
		        	var fields = $(this).closest("form").find('input,textarea,select').not(':hidden');
		        	var index = fields.index(e.target);
		        	if (index > -1 && (index + 1) < fields.length) {
		        		fields.eq(index + 1).focus();
		        	}
	        	}
	            return false;
	        }
		});
		
	} catch (e) {
		console.error(e);
	}
}

function prepareClickableRows() {
	$(".clickableRow").click(function(e) {
		var referance = $(this).data("ref")
		if (referance != undefined && $(e.target).is('td')) {
			if ($(this).hasClass('ajax')) {
				edit(referance);
			} else {
				window.document.location = referance;
			}
			e.stopPropagation();
		}
	});
}

function isDateValid(time) {
	if (time != null && time.isValid()) {
		if (time.year() > 1999 && time.year() < 2100) return true;
	}
	
	return false;
}

function formatMoney(value) {
	return value.toFixed(pennyDigits).replace(/(\d)(?=(\d{3})+\.)/g, "$1");
}

function parseDouble(val, defauld) {
	if (!isNaN(val) && val.length > 0) {
	  return parseFloat(val);
	} else {
		return defauld;
	}
}

function parseBoolean(str) {
	if (str == null) return false;
	return /^true$/i.test(str);
}

var dateRegEx = /^(0[1-9]|[12][0-9]|3[01]|[1-9])[- /.](0[1-9]|1[012]|[1-9])[- /.](19|20)\d\d$/;
function isDate(val) {
	if (val != null && val != undefined) {
		return (val.match(dateRegEx) !== null);
	}
	return false;
}

function getOrNull(val) {
	if (val != null && val.length > 0) {
	  return val;
	} else {
		return null;
	}
}

function incIdNumber(id, dir) {
	return id.replace(/\[(\d+)\]/g, function(match, number) {
	       return "\\[" + (parseInt(number) + (dir ? 1 : -1)) + "\\]";
		});
}

function goBack() {
	var window_path = $('#mainFieldsForm').attr('action');
	window.location.href = window_path;
}

Number.prototype.roundup= function(dec){
    var multiplier = Math.pow(10, dec);
    return Math.round(this * multiplier) / multiplier;
}

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

$(document).ready(function() {
	$.pnotify.defaults.delay = 2000;

	updateFormRegulators();

    $(this).ajaxStart(function () {
    	$('body').addClass('wait');
    	}).ajaxComplete(function () {
    		$('body').removeClass('wait');
	});    

	$("#accordian a").click(function(){
		var link = $(this);
		var closest_ul = link.closest("ul");
		var parallel_active_links = closest_ul.find(".active")
		var closest_li = link.closest("li");
		var link_status = closest_li.hasClass("active");
		var count = 0;

		closest_ul.find("ul").slideUp(function(){
			if(++count == closest_ul.find("ul").length)
				parallel_active_links.removeClass("active");
		});

		if (! link_status) {
			closest_li.children("ul").slideDown();
			closest_li.addClass("active");
		}

		var liid = closest_li.attr('id');
		if (! link_status) {
			if (liid.startsWith("menu-l1")) {
				$.cookie("menu.l1.selected.id", closest_li.attr('id'), { expires: 365, path: '/' });
			} else {
				$.cookie("menu.l2.selected.id", closest_li.attr('id'), { expires: 365, path: '/' });
			}
		} else {
			if (liid.startsWith("menu-l1")) {
				$.cookie("menu.l1.selected.id", null, { expires: 365, path: '/' });
			} else {
				$.cookie("menu.l2.selected.id", null, { expires: 365, path: '/' });
			}
		}

	});

	var lastLevel1Selection = $.cookie("menu.l1.selected.id");
	var lastLevel2Selection = $.cookie("menu.l2.selected.id");
	if (lastLevel1Selection != undefined) $("#"+lastLevel1Selection).addClass("active");
	if (lastLevel2Selection != undefined) $("#"+lastLevel2Selection).addClass("active");

	prepareClickableRows();

});
