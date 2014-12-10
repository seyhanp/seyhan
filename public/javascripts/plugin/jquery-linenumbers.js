(function($){
	$.fn.linenumbers = function(in_opts){
		// Settings and Defaults
		var opt = $.extend({
			col_width: '25px',
			start: 1,
			digits: 4.
		},in_opts);
		return this.each(function(){
			// Get some numbers sorted out for the CSS changes
			var new_textarea_height = $(this)[0].style.height;
			// Create the div and the new textarea and style it
			$(this).before('<div style="width:6%;"><textarea style="width:34px;height:'+new_textarea_height+';resize:none;float:left;margin-right:-34px;font-family:Courier New;font-size:12px;white-space:pre;overflow:hidden;text-align:right" disabled="disabled"></textarea>');
			$(this).after('<div style="clear:both;"></div></div>');
			// Edit the existing textarea's styles
			$(this).css({'font-family':'Courier New','font-size':'12px','width':'95%','float':'right','resize':'none'});
			// Define a simple variable for the line-numbers box
			var lnbox = $(this).parent().find('textarea[disabled="disabled"]');
			// Bind some actions to all sorts of events that may change it's contents
			$(this).bind('blur focus change keyup keydown',function(){
				// Break apart and regex the lines, everything to spaces sans linebreaks
				var lines = "\n"+"\n"+$(this).val();
				lines = lines.match(/[^\n]*\n[^\n]*/gi);
				// declare output var
				var line_number_output='';
				// Loop through and process each line
				$.each(lines,function(k,v){
					// Add a line if not blank
					if(k!=0){
						line_number_output += k+":"+"\n";
					}
				});
				// Give the text area out modified content.
				$(lnbox).val(line_number_output);
				// Change scroll position as they type, makes sure they stay in sync
			    $(lnbox).scrollTop($(this).scrollTop());
			})
			// Lock scrolling together, for mouse-wheel scrolling 
			$(this).scroll(function(){
			    $(lnbox).scrollTop($(this).scrollTop());
			});
			// Fire it off once to get things started
			$(this).trigger('keyup');
		});
	};
})(jQuery);