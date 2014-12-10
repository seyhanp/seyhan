/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author mdpinar
*/
@MappedSuperclass
public abstract class BaseStockExtraFieldsModel extends BaseModel {

	private static final long serialVersionUID = 1L;

	@ManyToOne public StockExtraFields extraField0;
	@ManyToOne public StockExtraFields extraField1;
	@ManyToOne public StockExtraFields extraField2;
	@ManyToOne public StockExtraFields extraField3;
	@ManyToOne public StockExtraFields extraField4;
	@ManyToOne public StockExtraFields extraField5;
	@ManyToOne public StockExtraFields extraField6;
	@ManyToOne public StockExtraFields extraField7;
	@ManyToOne public StockExtraFields extraField8;
	@ManyToOne public StockExtraFields extraField9;

}
