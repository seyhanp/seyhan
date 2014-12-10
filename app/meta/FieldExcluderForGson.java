/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package meta;

import javax.persistence.ManyToOne;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * @author mdpinar
*/
public class FieldExcluderForGson implements ExclusionStrategy {

    public boolean shouldSkipClass(Class<?> arg0) {
        return false;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return (f.getName().equals("id") || f.getName().startsWith("_") || f.getAnnotation(ManyToOne.class) != null);
    }

}