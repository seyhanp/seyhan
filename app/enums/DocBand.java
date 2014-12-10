/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import com.avaje.ebean.annotation.EnumValue;

public enum DocBand {

	@EnumValue("ReportTitle")
	ReportTitle,

	@EnumValue("PageTitle")
	PageTitle,

	@EnumValue("Detail")
	Detail,

	@EnumValue("PageFooter")
	PageFooter,

	@EnumValue("ReportFooter")
	ReportFooter;

}
