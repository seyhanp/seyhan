$(function() {
	
	function checkMainBox() {
		$("#selectall").prop("checked", ($(".case").length == $(".case:checked").length));
	}
	
	checkMainBox();

	$("#selectall").click(function() {
		$('.case').prop('checked', this.checked);
		$(document).trigger('selectAllEvent', $('.case:checked').length);
	});

	$(".case").click(function() {
		checkMainBox();
		$(document).trigger('selectAllEvent', $('.case:checked').length);
	});
	
});