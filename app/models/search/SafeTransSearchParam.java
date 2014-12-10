/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.Safe;
import models.SafeTransSource;

/**
 * @author mdpinar
*/
public class SafeTransSearchParam extends AbstractSearchParam {

	public Integer receiptNo;
	public String transNo;

	public Safe safe;
	public SafeTransSource transSource;
	public GlobalTransPoint transPoint;
	public GlobalPrivateCode privateCode;

}
