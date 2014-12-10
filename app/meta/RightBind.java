/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package meta;

import java.util.Map;

import play.libs.F;
import play.libs.F.Option;
import play.mvc.QueryStringBindable;
import enums.Right;

/**
 * @author mdpinar
*/
public class RightBind implements QueryStringBindable<RightBind> {

	public Right value;

	public RightBind() {
		;
	}

	public RightBind(Right value) {
		this.value = value; 
	}

	@Override
	@SuppressWarnings("unchecked")
	public Option<RightBind> bind(String key, Map<String, String[]> data) {
		String[] vs = data.get(key);
		if (vs != null && vs.length > 0) {
			value = Right.valueOf(vs[0]);
			return F.Some(this);
		}
		return F.None();
	}

	@Override
	public String unbind(String key) {
		return key + "=" + value.name();

	}

	@Override
	public String javascriptUnbind() {
		return (value != null ? value.name() : "no");
	}

	@Override
	public String toString() {
		return value.name();
	}


}
