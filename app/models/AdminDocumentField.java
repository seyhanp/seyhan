/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
import enums.Alignment;
import enums.DocBand;
import enums.DocTableType;
import enums.FieldType;
import enums.Module;

@Entity
/**
 * @author mdpinar
*/
public class AdminDocumentField extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@ManyToOne
	public AdminDocument reportTitleDoc;

	@ManyToOne
	public AdminDocument pageTitleDoc;

	@ManyToOne
	public AdminDocument detailDoc;

	@ManyToOne
	public AdminDocument pageFooterDoc;

	@ManyToOne
	public AdminDocument reportFooterDoc;

	public Module module;

	public DocBand band;

	/*
	 * verinin turu (ozellikle format kisminda kullanilmak uzere)
	 */
	@Column(name = "_type")
	public FieldType type;

	/*
	 * veritabani alani ise sql sorgularinda kullanilacak
	 * degilse value olarak kullanilacak
	 */
	public String name;

	public String nickName;

	public String hiddenField;

	/*
	 * Kullanicinin gorecegi etiket 
	 * coklu dil destegi icin dogrudan degil de messages key kullanilacak 
	 */
	@Column(name = "_label")
	public String label;
	public String originalLabel;
	public Integer labelWidth;
	public Alignment labelAlign = Alignment.Right;

	@Column(name = "_width")
	public Integer width;

	@Column(name = "_row")
	public Integer row;

	@Column(name = "_column")
	public Integer column;

	@Column(name = "_format")
	public String format;
	public String prefix;
	public String suffix;

	/*
	 * Mesaj dosyasindan okunacak bir deger ise, on eki
	 */
	public String msgPrefix;
	
	/*
	 * Deger null oldugunda kullanilacak
	 */
	public String defauld;

	/*
	 * veritabani alani degilse bu deger kullanilacak
	 */
	@Column(name = "_value")
	public String value;

	public Boolean isDbField;

	public DocTableType tableType = DocTableType.NONE;

}
