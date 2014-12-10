/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import play.cache.Cache;
import play.i18n.Messages;
import utils.CacheUtils;

import com.avaje.ebean.annotation.EnumValue;

@SuppressWarnings("unchecked")
public enum ChqbllStep {

	/**
	 * Portfoyde
	 * Bir Cariden Alinan Musteri Cek/Senedi
	 */
	@EnumValue("InPortfolio")
	InPortfolio,

	/**
	 * Cirolu
	 * Bir Cariye Verilen Musteri Cek/Senedi
	 */
	@EnumValue("Endorsed")
	Endorsed,

	/**
	 * Tahsilata verilen
	 * Musteri Cek/Senedi
	 */
	@EnumValue("InCollection")
	InCollection,

	/**
	 * Teminata verilen
	 * Musteri Cek/Senedi
	 */
	@EnumValue("Warrantee")
	Warrantee,

	/**
	 * Elden Tahsil edildi
	 * Musteri Cek/Senedi
	 */
	@EnumValue("Collected")
	Collected,

	/**
	 * Banka Hesabına Gecti
	 * Musteri Cek/Senedi
	 */
	@EnumValue("Deposited")
	Deposited,

	/**
	 * Musteriye Iade edilen
	 * Firma Cek/Senedi
	 */
	@EnumValue("Returned")
	Returned,

	/**
	 * Takipte
	 * Musteri Senedi
	 */
	@EnumValue("InPursue")
	InPursue,

	/**
	 * Karsiliksiz
	 * Musteri Cek/Senedi
	 */
	@EnumValue("Bounced")
	Bounced,

	/**
	 * Verildi (Bir Baskasina Verilen) 
	 * Firma Cek/Senedi
	 */
	@EnumValue("Issued")
	Issued,

	/**
	 * Kasadan Odendi
	 * Firma Cek/Senedi
	 */
	@EnumValue("PaidInCash")
	PaidByCash,

	/**
	 * Bankadan Odendi
	 * Firma Cek/Senedi
	 */
	@EnumValue("PaidByBank")
	PaidByBank,

	/**
	 * Kapandi
	 * Firma/Musteri - Cek/Senet
	 */
	@EnumValue("Closed")
	Closed,

	/**
	 * Parcali Tahsilat
	 * Musteri - Cek/Senet
	 */
	@EnumValue("PartCollection")
	PartCollection,

	/**
	 * Parcali Odeme
	 * Firma - Cek/Senet
	 */
	@EnumValue("PartPayment")
	PartPayment;

	public String key = "enum.cqbl.step." + name();

	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, ChqbllStep.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		for(ChqbllStep enm : values()) {
			options.put(enm.name(), Messages.get(enm.key));
		}

		return options;
	}

	public static Map<String, String> options(Right right) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, right, ChqbllStep.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();

		if (! isCustomer(right)) {
			options.put(Issued.name(), Messages.get(Issued.key));
		} else {
			for(ChqbllStep enm : EnumSet.range(InPortfolio, Bounced)) {
				options.put(enm.name(), Messages.get(enm.key));
			}
		}
		options.put(Closed.name(), Messages.get(Closed.key));

		return options;
	}

	public static List<String> openingOptions(Right right) {
		List<String> options = new ArrayList<String>();

		if (! isCustomer(right)) {
			options.add(Issued.name());
			options.add(PartPayment.name());
		} else {
			options.add(InPortfolio.name());
			options.add(InCollection.name());
			options.add(Warrantee.name());
			options.add(InPursue.name());
			options.add(Bounced.name());
			options.add(PartCollection.name());
		}

		return options;
	}

	public static Map<String, String> partialOptions(Right right) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, right, "partial", ChqbllStep.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();

		if (isCustomer(right)) {
			options.put(PartCollection.name(), Messages.get(PartCollection.key));
			options.put(InPortfolio.name(), Messages.get(InPortfolio.key));
		} else {
			options.put(PartPayment.name(), Messages.get(PartPayment.key));
			options.put(Issued.name(), Messages.get(Issued.key));
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> sourceOptions(Right right) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, right, "source", ChqbllStep.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		if (! isCustomer(right)) {
			options.put(Issued.name(), Messages.get(Issued.key));
		} else {
			for(ChqbllStep enm : EnumSet.range(InPortfolio, Bounced)) {
				options.put(enm.name(), Messages.get(enm.key));
			}
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> targetOptions(String stepStr) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, stepStr, "target", ChqbllStep.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();

		ChqbllStep step = null;
		try {
			step = valueOf(stepStr);
		} catch (Exception e) {
			options.put("", Messages.get("choose"));
			return options;
		}

		switch (step) {

			case InPortfolio: {
				options.put(Collected.name(), Messages.get(Collected.key));			//Kasa Borc
				options.put(Endorsed.name(), Messages.get(Endorsed.key));			//Cari Borc
				options.put(InCollection.name(), Messages.get(InCollection.key));
				options.put(Warrantee.name(), Messages.get(Warrantee.key));
				options.put(Returned.name(), Messages.get(Returned.key));			//Cari Borc
				options.put(Bounced.name(), Messages.get(Bounced.key));
				break;
			}

			case Issued: {
				options.put(PaidByCash.name(), Messages.get(PaidByCash.key));		//Kasa Alacak
				options.put(PaidByBank.name(), Messages.get(PaidByBank.key));		//Banka Alacak
				break;
			}

			case Endorsed: {
				options.put(InPortfolio.name(), Messages.get(InPortfolio.key));		//Cari Alacak
				options.put(Bounced.name(), Messages.get(Bounced.key));
				options.put(Closed.name(), Messages.get(Closed.key));
				break;
			}

			case Warrantee:
			case InCollection: {
				options.put(Deposited.name(), Messages.get(Deposited.key));			//Banka Borc
				options.put(InPortfolio.name(), Messages.get(InPortfolio.key));
				options.put(Bounced.name(), Messages.get(Bounced.key));
				break;
			}

			case Bounced: {
				options.put(Collected.name(), Messages.get(Collected.key));			//Kasa Borc
				options.put(Deposited.name(), Messages.get(Deposited.key));			//Banka Borc
				options.put(InPortfolio.name(), Messages.get(InPortfolio.key));
				options.put(InPursue.name(), Messages.get(InPursue.key));
				options.put(Closed.name(), Messages.get(Closed.key));
				break;
			}

			case InPursue: {
				options.put(Collected.name(), Messages.get(Collected.key));			//Kasa Borc
				options.put(Deposited.name(), Messages.get(Deposited.key));			//Banka Borc
				options.put(InPortfolio.name(), Messages.get(InPortfolio.key));
				options.put(Closed.name(), Messages.get(Closed.key));
				break;
			}

			case Returned:
			case PaidByCash:
			case PaidByBank:
			case Collected:
			case Deposited: {
				options.put(Closed.name(), Messages.get(Closed.key));
				break;
			}

		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Module findRefModule(ChqbllStep fromStep, ChqbllStep toStep) {
		return findRefModule(fromStep, toStep, null);
	}

	public static Module findRefModule(ChqbllStep fromStep, ChqbllStep toStep, Map<String, String> targetSteps) {
		Module result = Module.no;

		if (toStep == null) {
			if (targetSteps != null && ! "".equals(targetSteps.entrySet().iterator().next().getKey())) {
				toStep = ChqbllStep.valueOf(targetSteps.entrySet().iterator().next().getKey());
			} else {
				return result;
			}
		}

		switch (toStep) {

			case Endorsed: {
				result = Module.contact;
				break;
			}

			case Collected:
			case PaidByCash: {
				result = Module.safe;
				break;
			}

			case Warrantee:
			case InCollection: {
				result = Module.bank;
				break;
			}

			case Deposited: {
				if (Bounced.equals(fromStep) || InPursue.equals(fromStep)) result = Module.bank;
			}

		}

		return result;
	}

	private final static List<Right> customerRightList = new ArrayList<Right>();

	static {
		customerRightList.add(Right.CEK_GIRIS_BORDROSU);
		customerRightList.add(Right.CEK_MUSTERI_HAREKETLERI);
		customerRightList.add(Right.CEK_PARCALI_TAHSILAT);
		customerRightList.add(Right.CEK_MUSTERI_ACILIS_ISLEMI);
		customerRightList.add(Right.SENET_GIRIS_BORDROSU);
		customerRightList.add(Right.SENET_MUSTERI_HAREKETLERI);
		customerRightList.add(Right.SENET_PARCALI_TAHSILAT);
		customerRightList.add(Right.SENET_MUSTERI_ACILIS_ISLEMI);
	}

	public static boolean isCustomer(Right right) {
		return customerRightList.contains(right);
	}

}
