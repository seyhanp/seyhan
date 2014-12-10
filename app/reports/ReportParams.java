/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package reports;

import java.util.HashMap;
import java.util.Map;

import enums.ReportUnit;

/**
 * @author mdpinar
*/
public class ReportParams {

	public String modul;
	public String reportName;
	public String reportNameExtra;
	public ReportUnit reportUnit;
	public String query;
	public String orderBy;
	public String having;
	public Map<String, Object> paramMap = new HashMap<String, Object>();

}
