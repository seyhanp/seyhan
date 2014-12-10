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
public abstract class BaseContactExtraFieldsModel extends BaseModel {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne public ContactExtraFields extraField0;
	@ManyToOne public ContactExtraFields extraField1;
	@ManyToOne public ContactExtraFields extraField2;
	@ManyToOne public ContactExtraFields extraField3;
	@ManyToOne public ContactExtraFields extraField4;
	@ManyToOne public ContactExtraFields extraField5;
	@ManyToOne public ContactExtraFields extraField6;
	@ManyToOne public ContactExtraFields extraField7;
	@ManyToOne public ContactExtraFields extraField8;
	@ManyToOne public ContactExtraFields extraField9;

}
