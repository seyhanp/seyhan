/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;
import play.libs.Json;
import utils.CacheUtils;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class GlobalPrivateCode extends BaseModel {

	private static final long serialVersionUID = 1L;

	public Integer par1Id;
	public Integer par2Id;
	public Integer par3Id;
	public Integer par4Id;
	public Integer par5Id;

	@Constraints.Required
	@Constraints.MinLength(1)
	@Constraints.MaxLength(30)
	public String name;

	public GlobalPrivateCode() {
		super();
	}

	public GlobalPrivateCode(Integer parentid) {
		this();

		GlobalPrivateCode parent = findById(parentid);

		if (parent == null) return;

		if (parent.par1Id == null) {
			this.par1Id = parent.id;
		} else if (parent.par2Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.id;
		} else if (parent.par3Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.par2Id;
			this.par3Id = parent.id;
		} else if (parent.par4Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.par2Id;
			this.par3Id = parent.par3Id;
			this.par4Id = parent.id;
		} else if (parent.par5Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.par2Id;
			this.par3Id = parent.par3Id;
			this.par4Id = parent.par4Id;
			this.par5Id = parent.id;
		}
	}

	public GlobalPrivateCode(GlobalPrivateCode parent) {
		this();
		if (parent == null) return;

		this.name = parent.name;

		if (parent.par1Id == null) {
			this.par1Id = parent.id;
		} else if (parent.par2Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.id;
		} else if (parent.par3Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.par2Id;
			this.par3Id = parent.id;
		} else if (parent.par4Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.par2Id;
			this.par3Id = parent.par3Id;
			this.par4Id = parent.id;
		} else if (parent.par5Id == null) {
			this.par1Id = parent.par1Id;
			this.par2Id = parent.par2Id;
			this.par3Id = parent.par3Id;
			this.par4Id = parent.par4Id;
			this.par5Id = parent.id;
		}
	}

	private static Model.Finder<Integer, GlobalPrivateCode> find = new Model.Finder<Integer, GlobalPrivateCode>(Integer.class, GlobalPrivateCode.class);

	/**
	 * Converts json tree what flat rows in database
	 * 
	 * There are many repeated code blocks which same of the each other in this method, fix it
	 * 
	 */
	public static String listAllAsJson() {
		String result = CacheUtils.getValue(GlobalPrivateCode.class, "listAll");
		if (result != null) return result;

		List<GlobalPrivateCode> rootList = new ArrayList<GlobalPrivateCode>();
		Map<Integer, List<GlobalPrivateCode>> level1Map = new LinkedHashMap<Integer, List<GlobalPrivateCode>>();
		Map<Integer, List<GlobalPrivateCode>> level2Map = new LinkedHashMap<Integer, List<GlobalPrivateCode>>();
		Map<Integer, List<GlobalPrivateCode>> level3Map = new LinkedHashMap<Integer, List<GlobalPrivateCode>>();
		Map<Integer, List<GlobalPrivateCode>> level4Map = new LinkedHashMap<Integer, List<GlobalPrivateCode>>();
		Map<Integer, List<GlobalPrivateCode>> level5Map = new LinkedHashMap<Integer, List<GlobalPrivateCode>>();

		List<GlobalPrivateCode> codeList = find.where()
													.eq("workspace", CacheUtils.getWorkspaceId())
												.orderBy("name")
											.findList();

		for (GlobalPrivateCode item: codeList) {
			if (item.par5Id != null) {
				arrangeItems(level5Map, item, item.par5Id);
			} else if (item.par5Id == null && item.par4Id != null) {
				arrangeItems(level4Map, item, item.par4Id);
			} else if (item.par4Id == null && item.par3Id != null) {
				arrangeItems(level3Map, item, item.par3Id);
			} else if (item.par3Id == null && item.par2Id != null) {
				arrangeItems(level2Map, item, item.par2Id);
			} else if (item.par2Id == null && item.par1Id != null) {
				arrangeItems(level1Map, item, item.par1Id);
			} else if (item.par1Id == null) {
				rootList.add(item);
			}
		}

		/**
		 * Root level
		 */
		ArrayNode rootAN = Json.newObject().arrayNode();
		for (GlobalPrivateCode rootItem: rootList) {
			ObjectNode itemRL = Json.newObject();
			itemRL.put("key", rootItem.id);
			itemRL.put("title", rootItem.name);
			if (level1Map.containsKey(rootItem.id)) {
				itemRL.put("isFolder", true);

				/**
				 * Level 1
				 */
				ArrayNode level1AN = Json.newObject().arrayNode();
				List<GlobalPrivateCode> level1List = level1Map.get(rootItem.id);
				for (GlobalPrivateCode level1Item: level1List) {
					ObjectNode item1L = Json.newObject();
					item1L.put("key", level1Item.id);
					item1L.put("title", level1Item.name);
					if (level2Map.containsKey(level1Item.id)) {
						item1L.put("isFolder", true);

						/**
						 * Level 2
						 */
						ArrayNode level2AN = Json.newObject().arrayNode();
						List<GlobalPrivateCode> level2List = level2Map.get(level1Item.id);
						for (GlobalPrivateCode level2Item: level2List) {
							ObjectNode item2L = Json.newObject();
							item2L.put("key", level2Item.id);
							item2L.put("title", level2Item.name);
							if (level3Map.containsKey(level2Item.id)) {
								item2L.put("isFolder", true);

								/**
								 * Level 3
								 */
								ArrayNode level3AN = Json.newObject().arrayNode();
								List<GlobalPrivateCode> level3List = level3Map.get(level2Item.id);
								for (GlobalPrivateCode level3Item: level3List) {
									ObjectNode item3L = Json.newObject();
									item3L.put("key", level3Item.id);
									item3L.put("title", level3Item.name);
									if (level4Map.containsKey(level3Item.id)) {
										item3L.put("isFolder", true);

										/**
										 * Level 4
										 */
										ArrayNode level4AN = Json.newObject().arrayNode();
										List<GlobalPrivateCode> level4List = level4Map.get(level3Item.id);
										for (GlobalPrivateCode level4Item: level4List) {
											ObjectNode item4L = Json.newObject();
											item4L.put("key", level4Item.id);
											item4L.put("title", level4Item.name);
											if (level5Map.containsKey(level4Item.id)) {
												item4L.put("isFolder", true);

												/**
												 * Level 5
												 */
												ArrayNode level5AN = Json.newObject().arrayNode();
												List<GlobalPrivateCode> level5List = level5Map.get(level4Item.id);
												for (GlobalPrivateCode level5Item: level5List) {
													ObjectNode item5L = Json.newObject();
													item5L.put("key", level5Item.id);
													item5L.put("title", level5Item.name);
													level5AN.add(item5L);
												}
												item4L.put("children", level5AN);
											}
											level4AN.add(item4L);
										}
										item3L.put("children", level4AN);
									}
									level3AN.add(item3L);
								}
								item2L.put("children", level3AN);
							}
							level2AN.add(item2L);
						}
						item1L.put("children", level2AN);
					}
					level1AN.add(item1L);
				}
				itemRL.put("children", level1AN);
			}
			rootAN.add(itemRL);
		}

		result = Json.stringify(rootAN);
		CacheUtils.setValue(GlobalPrivateCode.class, "listAll", result);

		return result;
	}

	private static void arrangeItems(Map<Integer, List<GlobalPrivateCode>> map, GlobalPrivateCode item, Integer parId) {
		List<GlobalPrivateCode> list = map.get(parId);
		if (list == null) list = new ArrayList<GlobalPrivateCode>();

		if (! list.contains(item)) {
			list.add(item);
		}
		map.put(parId, list);
	}

	public static GlobalPrivateCode findById(Integer id) {
		GlobalPrivateCode result = CacheUtils.getById(GlobalPrivateCode.class, id);

		if (result == null) {
			result = find.byId(id);
			if (result != null) CacheUtils.setById(GlobalPrivateCode.class, id, result);
		}

		return result;
	}

	@Override
	public Right getAuditRight() {
		return Right.GNEL_OZEL_KODLAR;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.name") + this.name;
	}

	@Override
	public String toString() {
		return name;
	}

}
