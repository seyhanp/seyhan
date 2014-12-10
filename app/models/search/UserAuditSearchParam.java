/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;


/**
 * @author mdpinar
*/
public class UserAuditSearchParam extends AbstractSearchParam {

	public String workspace;
	public String username;
	public String ip;

	public Boolean loginAction;
	public Boolean logoutAction;
	public Boolean insertAction;
	public Boolean updateAction;
	public Boolean deleteAction;

}
