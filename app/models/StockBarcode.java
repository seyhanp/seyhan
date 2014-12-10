/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;

import com.avaje.ebean.ExpressionList;

@Entity
/**
 * @author mdpinar
*/
public class StockBarcode extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public Integer workspace;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(50)
	public String barcode;

	@Constraints.MaxLength(30)
	public String prefix;

	@Constraints.MaxLength(30)
	public String suffix;

	public Integer unitNo = 1;

	public boolean isPrimary = false;

	@ManyToOne
	public Stock stock;

	private static Model.Finder<Integer, StockBarcode> find = new Model.Finder<Integer, StockBarcode>(Integer.class, StockBarcode.class);

	public StockBarcode(StockBarcode other) {
		this(other.barcode, other.prefix, other.suffix);
		this.id = other.id;
	}

	public StockBarcode(String primaryBarcode) {
		this(primaryBarcode, null, null);
		this.isPrimary = true;
	}

	public StockBarcode(String barcode, String prefix, String suffix) {
		this.barcode = barcode;
		this.prefix = prefix;
		this.suffix = suffix;
		this.unitNo = 1;
		this.workspace = CacheUtils.getWorkspaceId();
	}

	public static boolean areUsedForElse(Set<String> value, Integer id) {
		ExpressionList<StockBarcode> el = find.where()
												.eq("workspace", CacheUtils.getWorkspaceId())
												.in("barcode", value);
		if (id != null) el.ne("stock.id", id);

		return el.findUnique() != null;
	}

	@Override
	public void save() {
		this.workspace = CacheUtils.getWorkspaceId();
		super.save();
	}

	@Override
	public void update() {
		this.workspace = CacheUtils.getWorkspaceId();
		super.update();
	}

	@Override
	public String toString() {
		return barcode;
	}

}
