	
function onFieldChange(name, field) {
	var baseId = $('#'+name+'_id').val();
	$(baseId+field).val($('#'+name+'_'+field).val());
}

function changeFieldLabelVisibility(band) {
	$('#'+band+'_labelGroup').css({display: ($('#'+band+"Labels")[0].selectedIndex == 0 ? "block" : "none")});
}

$("document").ready(function(){
	
	var bands = ["#reportTitleTable", "#pageTitleTable", "#detailTable", "#pageFooterTable", "#reportFooterTable"];

	bands.forEach(function(band) {
		$(band).tableAutoAddRow({inputBoxAutoNumber: true, rowNumColumn: "rowNumber", autoAddRow: true}, function(row){
			updateFormRegulators(band+" tbody tr");
		});
		$(band+" .delRow").btnDelRow(function(row){
			updateFormRegulators(band+" tbody tr");
		});

		$(band).freezeTableColumns({
			width: 290,
			height: 300
		});

		$(band.replace('Table', 'Fields')).keypress(function(e){
			if (e.charCode == 32) {
				e.preventDefault();
				addElementToBand(band.replace('Table', '').replace('#', ''));
			}
		});
		
		$(band.replace('Table', 'Fields')).dblclick(function(e){
			addElementToBand(band.replace('Table', '').replace('#', ''));
		});
		
	});

	$(".autoTable").on("focus", ".mainInput", function() {
		var raw1 = $(this).attr('id');
		var raw2 = '#'+raw1.substr(0, raw1.lastIndexOf('_') + 1);
		var raw3 = raw2.replace("[", "\\[");
		var raw4 = raw3.replace("]", "\\]");
		var raw5 = raw4.replace("_", "\\_");
		var fieldBaseId = raw5;
		var sectionId = '#'+raw1.substr(0, raw1.lastIndexOf('Fields'))+'_';

		$(sectionId+'id').val(fieldBaseId);
		var title = $(fieldBaseId + 'originalLabel').val();
		if (title.length < 1) {
			title = propsMsg;
		}
		$(sectionId+'label').html(title);

		$(sectionId+'row').val($(fieldBaseId + 'row').val());
		$(sectionId+'column').val($(fieldBaseId + 'column').val());
		$(sectionId+'width').val($(fieldBaseId + 'width').val());
		$(sectionId+'value').val($(fieldBaseId + 'value').val());
		$(sectionId+'format').val($(fieldBaseId + 'format').val());
		$(sectionId+'prefix').val($(fieldBaseId + 'prefix').val());
		$(sectionId+'suffix').val($(fieldBaseId + 'suffix').val());
		$(sectionId+'labelAlign').val($(fieldBaseId + 'labelAlign').val());
		$(sectionId+'labelWidth').val($(fieldBaseId + 'labelWidth').val());
		
		var typ = $(fieldBaseId + 'type').val();
		$(sectionId+'valueGroup').css({display: (typ == 'LINE' || typ == 'STATIC_TEXT' ? "block" : "none")});
		
		$('.mainInput').css({backgroundColor: 'white'});
		$(this).css({backgroundColor: 'lightblue'});
	});
	
	function addElementToBand(band) {
		$("#"+band+"Table").btnManuelAddRow(
			function(rowIndex) {
				var row = {};
				var fieldBaseId = '#'+band+'Fields\\['+rowIndex+'\\]\\_';
				var seleted = JSON.parse($("#"+band+"Fields").val());

				$(fieldBaseId + 'module').val(seleted.module);
				$(fieldBaseId + 'band').val(band.capitalize());
				$(fieldBaseId + 'type').val(seleted.type);
				$(fieldBaseId + 'name').val(seleted.name);
				$(fieldBaseId + 'nickName').val(seleted.nickName);
				$(fieldBaseId + 'hiddenField').val(seleted.hiddenField);

				var lbl = $("#"+band+"Fields :selected").text().toUpperCase();
				$(fieldBaseId + 'label').val(lbl + (isSinglePage || band != 'detail' ? ' : ' : ''));
				$(fieldBaseId + 'originalLabel').val(lbl);
				
				$(fieldBaseId + 'value').val(seleted.value);
				$(fieldBaseId + 'defauld').val(seleted.defauld);
				$(fieldBaseId + 'format').val(seleted.format);
				$(fieldBaseId + 'prefix').val(seleted.prefix);
				$(fieldBaseId + 'suffix').val(seleted.suffix);
				$(fieldBaseId + 'msgPrefix').val(seleted.msgPrefix);
				$(fieldBaseId + 'isDbField').val(seleted.isDbField);
				$(fieldBaseId + 'tableType').val(seleted.tableType);

				$(fieldBaseId + 'row').val(1);
				$(fieldBaseId + 'column').val(1);
				$(fieldBaseId + 'width').val(seleted.width);
				$(fieldBaseId + 'labelAlign').val('Right');
				$(fieldBaseId + 'labelWidth').val('15');
			}
		);
	}

});
