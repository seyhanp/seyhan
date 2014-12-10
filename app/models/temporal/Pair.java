/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

/**
 * @author mdpinar
*/
public class Pair {

	public static final Pair EMPTY = new Pair(null, null);

	public String key;
	public String value;

	public Pair(String key, String value) {
		this.key = key;
		this.value = value;
	}

}
