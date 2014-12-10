/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package documents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.AdminDocument;
import models.AdminDocumentField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;
import utils.DateUtils;
import utils.Format;
import utils.NumericUtils;
import utils.QueryUtils;
import utils.StringUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import enums.ColumnTitleType;
import enums.DocBand;
import enums.DocTableType;
import enums.FieldType;
import enums.Module;
import enums.TransType;

/**
 * @author mdpinar
*/
public class Helper {

	private final static Logger log = LoggerFactory.getLogger(Helper.class);

	private static int LINE_LIMIT = 255;
	private static final String emptyLine = StringUtils.fill(" ", LINE_LIMIT);

	public static List<String> buildPage(AdminDocument doc, Integer dataId) {
		List<String> result = new ArrayList<String>(doc.pageRows);

		SqlRow masterRow = null;
		if (! doc.isSinglePage) {
			masterRow = Ebean.createSqlQuery(buildSqlQuery(doc.id, false)).setParameter("id", dataId).findUnique();
		}
		List<SqlRow> detailRowList = Ebean.createSqlQuery(buildSqlQuery(doc.id, true)).setParameter("id", dataId).findList();
		if (detailRowList != null && detailRowList.isEmpty()) return result;

		
		//for ref. accounts
		SqlRow refRow = null;
		String refQueryString = null;
		if (doc.isSinglePage) {
			refQueryString = buildRefSqlQuery(doc, detailRowList.get(0).getInteger("ref_id"), detailRowList.get(0).getString("ref_module"));
		} else {
			if (masterRow != null && ! masterRow.isEmpty()) {
				refQueryString = buildRefSqlQuery(doc, masterRow.getInteger("ref_id"), masterRow.getString("ref_module"));
			}
		}
		if (refQueryString != null) {
			refRow = Ebean.createSqlQuery(refQueryString).findUnique();
		}
		
		if (doc.topMargin > 0) {
			for (int i = 0; i < doc.topMargin; i++) {
				result.add(emptyLine);
			}
		}

		int columnTitleColumn = 0;
		String columnTitleSep = "";
		String columnTitleLabels = "";

		int detailTitleRows = 0;
		int detailFooterRows = 0;
		if (doc.columnTitleType != null && ! ColumnTitleType.NOTHING.equals(doc.columnTitleType)) {
			columnTitleLabels = getColumnTitlelabels(doc.detailFields);
			columnTitleColumn = getColumnTitleColumn(doc.detailFields);
			switch (doc.columnTitleType) {
				case PLAIN: {
					detailTitleRows = 1;
					break;
				}
				case DASHED: {
					detailTitleRows = 3;
					detailFooterRows = 1;
					columnTitleSep = getColumnTitleSep(doc.detailFields);
					break;
				}
				case UNLINED: {
					detailTitleRows = 3;
					break;
				}
			}
		}

		if (doc != null) {
			if (! doc.hasPaging) {
				doc.pageTitleRows = 0;
				doc.pageFooterRows = 0;
				doc.pageRows = detailRowList.size() + doc.topMargin + doc.reportTitleRows + detailTitleRows + detailFooterRows + doc.reportFooterRows + doc.bottomMargin;
			} else {
				if (doc.templateRows != null) {
					String[] tempRows = doc.templateRows.split("\\n");
					for (String row : tempRows) {
						result.add(StringUtils.padRight(row, LINE_LIMIT));
						if (result.size() + doc.bottomMargin >= doc.pageRows) break;
					}
				}
			}

			if (result.size() < doc.pageRows) {
				int size = result.size();
				for (int i = 0; i < doc.pageRows - size; i++) {
					result.add(emptyLine);
				}
			}

			/**
			 * Bantlar
			 */

			RowInfo info = new Helper.RowInfo();
			info.isSinglePage = doc.isSinglePage;
			info.rowNo = 1;
			info.pageNo = 1;
			info.pageCount = 1;
			info.startRow = doc.topMargin;
			info.module = doc.module;
			info.header = doc.header;
			info.carryingOverName = doc.carryingOverName;
			info.refRow = refRow;

			if (doc.isSinglePage) {
				if (doc.detailRows.intValue() > 0 && doc.detailFields != null && doc.detailFields.size() > 0) {
					info.rows = result;
					info.band = DocBand.Detail;
					info.dataRow = detailRowList.get(0);
					info.fieldList = doc.detailFields;
					info.bandLimit = doc.detailRows;
					info.hasBandLabels = doc.detailLabels;
					setLines(info);
					result = info.rows;

					info.fieldList = doc.detailFields;
					setTablesIfExists(false, info, doc.module,  dataId, doc.pageRows - doc.bottomMargin, doc.topMargin);
				}
			} else if (detailRowList != null && detailRowList.size() > 0) {
				int firstPageDetailLimit = doc.pageRows -
											(doc.topMargin + 
											doc.reportTitleRows + 
											doc.pageTitleRows + 
											detailTitleRows +
											detailFooterRows +
											doc.pageFooterRows + 
											doc.bottomMargin);

				int otherPageDetailLimit = doc.pageRows -
											(doc.topMargin + 
											doc.pageTitleRows + 
											detailTitleRows +
											detailFooterRows +
											doc.pageFooterRows + 
											doc.bottomMargin + 
											(info.carryingOverName != null ? 1 : 0));

				int lastPageDetailLimit = doc.pageRows -
											(doc.topMargin + 
											doc.pageTitleRows + 
											detailTitleRows +
											detailFooterRows +
											doc.pageFooterRows + 
											doc.reportFooterRows + 
											doc.bottomMargin + 
											(info.carryingOverName != null ? 1 : 0));
				
				info.pageCount = 1;
				if (doc.hasPaging) {
					int topLimit = firstPageDetailLimit;
					if (detailRowList.size() > firstPageDetailLimit) {
						info.pageCount++;
						topLimit += lastPageDetailLimit;
						while (detailRowList.size() > topLimit) {
							info.pageCount++;
							topLimit += otherPageDetailLimit;
						}
					} else if (detailRowList.size() > firstPageDetailLimit - doc.reportFooterRows) {
						info.pageCount++;
					}
				}
				
				int start = 0;
				boolean isNewPage = false;
				
				//Toplamlari alinacak alanlar
				info.sumOfMap = new HashMap<String, Double>();
				if (doc.reportTitleRows.intValue() > 0 && doc.reportTitleFields != null && doc.reportTitleFields.size() > 0) {
					for (AdminDocumentField field : doc.reportTitleFields) {
						if (FieldType.SUM_OF.equals(field.type)) {
							info.sumOfMap.put(field.nickName, 0d);
						}
					}
				}
				
				if (doc.hasPaging) {
					if (doc.pageTitleRows.intValue() > 0 && doc.pageTitleFields != null && doc.pageTitleFields.size() > 0) {
						for (AdminDocumentField field : doc.pageTitleFields) {
							if (FieldType.SUM_OF.equals(field.type)) {
								info.sumOfMap.put(field.nickName, 0d);
							}
						}
					}
					if (doc.pageFooterRows.intValue() > 0 && doc.pageFooterFields != null && doc.pageFooterFields.size() > 0) {
						for (AdminDocumentField field : doc.pageFooterFields) {
							if (FieldType.SUM_OF.equals(field.type)) {
								info.sumOfMap.put(field.nickName, 0d);
							}
						}
					}
				}

				if (doc.reportFooterRows.intValue() > 0 && doc.reportFooterFields != null && doc.reportFooterFields.size() > 0) {
					for (AdminDocumentField field : doc.reportFooterFields) {
						if (FieldType.SUM_OF.equals(field.type)) {
							info.sumOfMap.put(field.nickName, 0d);
						}
					}
				}

				boolean isFirts = false;
				int startRowForTables = 0;
				List<String> tempRows = new ArrayList<String>();

				for (int i = 1; i <= info.pageCount; i++) {

					info.pageNo = i;
					tempRows.addAll(new ArrayList<String>(result));
					info.rows = tempRows;

					//Report Title
					if (i == 1 && doc.reportTitleRows.intValue() > 0 && doc.reportTitleFields != null && doc.reportTitleFields.size() > 0) {
						info.startRow = doc.topMargin;
						info.band = DocBand.ReportTitle;
						info.dataRow = masterRow;
						info.fieldList = doc.reportTitleFields;
						info.bandLimit = doc.reportTitleRows;
						info.hasBandLabels = doc.reportTitleLabels;
						setLines(info);
					}

					//Page Title
					if (doc.hasPaging && doc.pageTitleRows.intValue() > 0 && doc.pageTitleFields != null && doc.pageTitleFields.size() > 0) {
						info.startRow = doc.topMargin + ((info.pageNo - 1) * doc.pageRows);
						if (i == 1) info.startRow += doc.reportTitleRows;
						info.band = DocBand.PageTitle;
						info.dataRow = masterRow;
						info.fieldList = doc.pageTitleFields;
						info.bandLimit = doc.pageTitleRows;
						info.hasBandLabels = doc.pageTitleLabels;
						setLines(info);
					}

					//Column title
					if (doc.columnTitleType != null && ! ColumnTitleType.NOTHING.equals(doc.columnTitleType) && doc.detailRows.intValue() > 0) {
						info.startRow += doc.topMargin + doc.pageTitleRows + (i == 1 ? doc.reportTitleRows : 0) - doc.topMargin;

						if (ColumnTitleType.DASHED.equals(doc.columnTitleType)) {
							repLine(info, columnTitleSep, info.startRow, columnTitleColumn);
						}

						int pos = info.startRow + (! ColumnTitleType.PLAIN.equals(doc.columnTitleType) ? 1 : 0);
						repLine(info, columnTitleLabels, pos, columnTitleColumn);

						if (ColumnTitleType.DASHED.equals(doc.columnTitleType)) {
							repLine(info, columnTitleSep, info.startRow+2, columnTitleColumn);
						}
					}

					//Nakli yekun yansitilmasi
					if (doc.hasPaging && isNewPage) {
						isNewPage = false;
						info.startRow = ((info.pageNo-1) * doc.pageRows) + doc.topMargin + doc.pageTitleRows + detailTitleRows;
						if (info.carryingOverName != null && info.carryingOverAmount > 0) {
							String label = Messages.get("carrying_over");
							String val = label + Format.asDouble(info.carryingOver.prefix, info.carryingOverAmount, info.carryingOver.format, info.carryingOver.width);
							int pos = info.carryingOver.column-label.length()-1;
							if (pos < 1) pos = 1;
							repLine(info, val, info.startRow, pos);
							info.startRow++;
						}
					}

					//Detail
					if (doc.detailRows.intValue() > 0) {
						int countNo = 0;
						for (int j = start; j < detailRowList.size(); j++) {
							if (j == 0) {
								info.startRow = doc.topMargin + doc.reportTitleRows + detailTitleRows + doc.pageTitleRows;
							}
	
							info.band = DocBand.Detail;
							info.dataRow = detailRowList.get(j);
							info.fieldList = doc.detailFields;
							info.bandLimit = doc.detailRows;
							info.hasBandLabels = false;
							setLines(info);
							info.rowNo = j + 1;
							info.startRow += doc.detailRows;
							countNo++;
	
							if (doc.hasPaging) { 
								if ((info.pageNo == 1 && countNo >= firstPageDetailLimit) || (info.pageNo > 1 && countNo >= otherPageDetailLimit)) {
									isNewPage = true;
									start = j + 1;
									countNo = 0;
									break;
								}
							}
						}
						if (countNo > 0) {
							start = detailRowList.size();
						}
					}

					//Report Footer
					if (start >= detailRowList.size() && ! isFirts && doc.detailRows.intValue() > 0 && doc.detailFields != null && doc.detailFields.size() > 0) {
						/*
						 * Detay satirlari nerede bitmis ise hemen sonrasinda baslar
						 */
						isFirts = true;
						startRowForTables = info.startRow;
						info.band = DocBand.ReportFooter;
						info.dataRow = masterRow;
						info.fieldList = doc.reportFooterFields;
						info.bandLimit = doc.reportFooterRows;
						info.hasBandLabels = doc.reportFooterLabels;
						setLines(info);
					}

					//Column Footer
					if (doc.hasPaging && ColumnTitleType.DASHED.equals(doc.columnTitleType) && doc.detailRows.intValue() > 0 && doc.detailRows != null && doc.detailRows.intValue() > 0) {
						int pos = (info.pageNo * doc.pageRows) - (doc.pageFooterRows + doc.bottomMargin + 1);
						repLine(info, columnTitleSep, pos, columnTitleColumn);
					}

					//Page Footer
					if (doc.hasPaging && doc.pageFooterRows != null && doc.pageFooterRows.intValue() > 0 && doc.pageFooterFields != null && doc.pageFooterFields.size() > 0) {
						info.startRow = (info.pageNo * doc.pageRows) - (doc.pageFooterRows + doc.bottomMargin);
						info.band = DocBand.PageFooter;
						info.dataRow = masterRow;
						info.fieldList = doc.pageFooterFields;
						info.bandLimit = doc.pageFooterRows;
						info.hasBandLabels = doc.pageFooterLabels;
						setLines(info);
					}
				}
				
				if (startRowForTables > 0 && doc.reportFooterRows.intValue() > 0) {
					int offset = doc.topMargin + doc.pageTitleRows + detailTitleRows + detailFooterRows + doc.pageFooterRows + doc.bottomMargin;
					int limit = 0;
					if (info.pageCount < 2) {
						limit = firstPageDetailLimit;
					} else {
						if (doc.columnTitleType != null || ColumnTitleType.NOTHING.equals(doc.columnTitleType)) {
							limit = ((info.pageCount) * doc.pageRows);
						} else {
							limit = ((info.pageCount-1) * doc.pageRows);
						}
						limit -= doc.bottomMargin + doc.pageFooterRows + detailFooterRows;
					}

					info.startRow = startRowForTables;
					info.fieldList = doc.reportFooterFields;
					setTablesIfExists(doc.hasPaging, info, doc.module,  dataId, limit, offset);
					if (! doc.hasPaging && detailFooterRows > 0) {
						repLine(info, columnTitleSep, info.rows.size()-1, columnTitleColumn);
					}
				}

			}
			result = info.rows;
		}

		if (result.size() > 0) {
			String leftPadding = StringUtils.fill(" ", doc.leftMargin);
			for (int i = 0; i < result.size(); i++) {
				result.set(i, leftPadding+StringUtils.trimRight(result.get(i)));
			}
		}

		return result;
	}

	private static void setTablesIfExists(boolean hasPaging, RowInfo rowInfo, Module module, Integer transId, int limit, int offset) {
		for (AdminDocumentField field : rowInfo.fieldList) {
			
			int row = 0;
			int col = 0;
			List<String> tableLines = null;

			if (field.tableType != null && ! DocTableType.NONE.equals(field.tableType)) {
				row = field.row;
				col = field.column - 1;
				switch (field.tableType) {
					case TAX_1: {
						tableLines = Tables.getTaxTable1(field.label, module, transId);
						break;
					}
					case EXCHANGE_1: {
						tableLines = Tables.getExchangeTable1(field.label);
						break;
					}
					case CURRENCY_1: {
						tableLines = Tables.getCurrencyTable1(field.label, module, transId);
						break;
					}
					case FACTOR_1: {
						tableLines = Tables.getFactorTable1(field.label, module, transId);
						break;
					}
				}
			}

			if (tableLines != null && tableLines.size() > 0) {
				int newRow = 0;
				for (int i = 0; i < tableLines.size(); i++) {
					if (hasPaging && rowInfo.startRow+i+row > limit) {
						repLine(rowInfo, tableLines.get(i), limit+offset+newRow, col);
						newRow++;
					} else {
						if (! hasPaging && rowInfo.startRow+i+row >= rowInfo.rows.size()) {
							rowInfo.rows.add(emptyLine);
						}
						repLine(rowInfo, tableLines.get(i), rowInfo.startRow+i+row-1, col);
					}
				}
				tableLines = null;
			}
		}
	}

	private static void setLines(RowInfo rowInfo) {
		for (int i = 0; i < rowInfo.fieldList.size(); i++) {
			AdminDocumentField field = rowInfo.fieldList.get(i);
			if (field.tableType != null && ! DocTableType.NONE.equals(field.tableType)) continue;
			if (field.row <= rowInfo.bandLimit) {

				StringBuilder valueSB = new StringBuilder();

				String value = null;
				if (field.isDbField) {
					if (DocBand.Detail.equals(rowInfo.band)) {
						try {
							Double sumOf = rowInfo.sumOfMap.get(field.nickName);
							if (sumOf != null) {
								Double val = rowInfo.dataRow.getDouble(field.nickName);
								if (val != null) {
									rowInfo.sumOfMap.put(field.nickName, sumOf.doubleValue() + val.doubleValue());
								}
							}
						} catch (Exception e) {
							;
						}
					}
					switch (field.type) {
						case STRING: {
							value = rowInfo.dataRow.getString(field.nickName);
							break;
						}
						case DATE:
						case LONGDATE: {
							if (field.format == null || field.format.isEmpty()) {
								field.format = Messages.get("formats." + field.type.name().toLowerCase());
							}
							value = DateUtils.formatDate(rowInfo.dataRow.getDate(field.nickName), field.format);
							break;
						}
						case INTEGER: {
							if (field.format == null || field.format.isEmpty()) {
								field.format = Messages.get("formats.integer");
							}
							Integer val = rowInfo.dataRow.getInteger(field.nickName);
							if (val != null) {
								value = Format.asInteger(field.prefix, val, field.format, field.width);
								field.prefix = "";
							}
							break;
						}
						case LONG: {
							if (field.format == null || field.format.isEmpty()) {
								field.format = Messages.get("formats.integer");
							}
							Long val = rowInfo.dataRow.getLong(field.nickName);
							if (val != null) {
								value = Format.asLong(field.prefix, val, field.format, field.width);
								field.prefix = "";
							}
							break;
						}
						case TAX:
						case RATE:
						case DOUBLE:
						case CURRENCY: {
							if (field.format == null || field.format.isEmpty()) {
								field.format = Messages.get("formats." + field.type.toString().toLowerCase());
							}
							Double val = rowInfo.dataRow.getDouble(field.nickName);
							if (val != null) {
								value = Format.asDouble(field.prefix, val, field.format, field.width);
								field.prefix = "";
							}
							break;
						}
						case BOOLEAN: {
							Boolean val = rowInfo.dataRow.getBoolean(field.nickName);
							if (val != null && val) {
								value = Messages.get("yes").toUpperCase();
							} else {
								value = Messages.get("no").toUpperCase();
							}
							break;
						}
						case MESSAGE: {
							String val = rowInfo.dataRow.getString(field.nickName);
							if (val != null) {
								value = Messages.get(field.msgPrefix + val).toUpperCase();
							}
							break;
						}
						case SUM_OF: {
							Double val = rowInfo.sumOfMap.get(field.nickName);
							if (val != null) {
								if (field.format == null || field.format.isEmpty()) {
									field.format = Messages.get("formats.currency");
								}
								value = Format.asDouble(field.prefix, val, field.format, field.width);
								field.prefix = "";
							}
						}
						case NUMBER_TO_TEXT: {
							Double val = rowInfo.dataRow.getDouble(field.hiddenField);
							if (val != null) {
								value = NumericUtils.withWritingInTurkish(rowInfo.dataRow.getDouble(field.hiddenField));
							}
							break;
						}
					}

					//Nakli yekun toplaminin alinmasi
					if (! rowInfo.isSinglePage && rowInfo.carryingOverName != null && rowInfo.band.equals(DocBand.Detail)) {
						Field coField = findCarryingOverField(rowInfo.carryingOverName);
						if (field.nickName.equals(coField.nickName)) {
							coField.column = field.column;
							coField.width = field.width;
							coField.format = field.format;
							coField.prefix = field.prefix;
							coField.suffix = field.suffix;
							Double val = rowInfo.dataRow.getDouble(field.nickName);
							if (val != null) {
								rowInfo.carryingOverAmount += val.doubleValue();
							}
							rowInfo.carryingOver = coField;
						}
					}

				} else {
					switch (field.type) {
						case SYS_DATE:
						case SYS_TIME:
						case SYS_DATE_FULL: {
							value = DateUtils.today(field.format);
							break;
						}
						case ROW_NO: {
							value = Format.asInteger(field.prefix, rowInfo.rowNo, field.format, field.width);
							field.prefix = "";
							break;
						}
						case PAGE_NUMBER: {
							value = Format.asInteger(field.prefix, rowInfo.pageNo, field.format, field.width);
							field.prefix = "";
							break;
						}
						case PAGE_COUNT: {
							value = Format.asInteger(field.prefix, rowInfo.pageCount, field.format, field.width);
							field.prefix = "";
							break;
						}
						case STATIC_TEXT: {
							value = (field.value);
							break;
						}
						case LINE: {
							value = StringUtils.fill(field.value, field.width);
							break;
						}
						case REF_NO:
						case REF_NAME:
						case REF_CURRENCY: {
							if (rowInfo.refRow != null) {
								value = rowInfo.refRow.getString(field.type.name().toLowerCase());
							}
							break;
						}
						case REF_AMOUNT: {
							if (rowInfo.refRow != null) {
								if (field.format == null || field.format.isEmpty()) {
									field.format = Messages.get("formats." + field.type.toString().toLowerCase());
								}
								Double val = rowInfo.refRow.getDouble(field.type.name().toLowerCase());
								if (val != null) {
									value = Format.asDouble(field.prefix, val, field.format, field.width);
									field.prefix = "";
								}
							}
							break;
						}
						case DEBT_SUM:
						case CREDIT_SUM:
						case BALANCE: {
							if (field.module != null) {
								Double tot = 0d;
								Integer infoId = rowInfo.dataRow.getInteger("infoId");

								if (infoId != null) {
									TransType ttype = null;
									if (field.hiddenField != null && ! field.hiddenField.isEmpty()) {
										ttype = TransType.valueOf(field.hiddenField);
									}
									if (Module.stock.equals(field.module)) {
										if (ttype == null) {
											tot = QueryUtils.findStockBalance(infoId);
										} else {
											tot = QueryUtils.findStockTotal(infoId, ttype);
										}
									} else {
										if (ttype == null) {
											tot = QueryUtils.findBalance(field.module, infoId);
										} else {
											tot = QueryUtils.findTotal(field.module, infoId, ttype);
										}
									}
									if (field.format == null || field.format.isEmpty()) {
										field.format = Messages.get("formats.currency");
									}
									value = Format.asDouble(field.prefix, tot, field.format, field.width);
									field.prefix = "";
								}
							}
							break;
						}
					}
					
				}

				valueSB.append(field.prefix);
				if (value != null) {
					valueSB.append(value);
				} else {
					valueSB.append(field.defauld);
				}
				valueSB.append(field.suffix);

				boolean isLabelExist = rowInfo.hasBandLabels && field.label != null && ! field.label.isEmpty();

				if (isLabelExist) {
					String label = field.label;
					Integer labelWidth = field.labelWidth;
					if (labelWidth == null || labelWidth.intValue() < 1) labelWidth = 15;
					valueSB.insert(0, StringUtils.pad(label, labelWidth, field.labelAlign));
				}

				if (valueSB.length() > 0) {
					int pre_suf_offset = 0;
					if (field.prefix != null) pre_suf_offset += field.prefix.trim().length();
					if (field.suffix != null) pre_suf_offset += field.suffix.trim().length();
				
					if (isLabelExist) {
						if (valueSB.length() > pre_suf_offset+field.width+field.labelWidth) valueSB.setLength(field.width+field.labelWidth);
					} else {
						if (valueSB.length() > pre_suf_offset+field.width) valueSB.setLength(field.width);
					}
					repLine(rowInfo, valueSB.toString(), field.row+rowInfo.startRow-1, field.column-1);
				}
			}
		}
	}
	
	private static void repLine(RowInfo rowInfo, String value, int row, int col) {
		if (value != null && ! value.trim().isEmpty()) {
			StringBuilder content = new StringBuilder(rowInfo.rows.get(row));
			content.replace(col, col+value.length(), value);
			rowInfo.rows.set(row, content.toString());
		}
	}
	
	private static String buildRefSqlQuery(AdminDocument doc, Integer refId, String moduleName) {
		if (refId == null || moduleName == null || moduleName.trim().isEmpty()) return null;

		StringBuilder result = null;
		boolean isExist = false;

		if (doc.isSinglePage) {
			if (doc.detailFields != null) {
				for (AdminDocumentField fld : doc.detailFields) {
					if (fld.type.name().startsWith("REF_")) {
						isExist = true;
						break;
					}
				}
			}
		} else {
			if (doc.reportTitleFields != null) {
				for (AdminDocumentField fld : doc.reportTitleFields) {
					if (fld.type.name().startsWith("REF_")) {
						isExist = true;
						break;
					}
				}
			}
			if (! isExist && doc.pageTitleFields != null)  {
				for (AdminDocumentField fld : doc.pageTitleFields) {
					if (fld.type.name().startsWith("REF_")) {
						isExist = true;
						break;
					}
				}
			}
			if (! isExist && doc.pageFooterFields != null) {
				for (AdminDocumentField fld : doc.pageFooterFields) {
					if (fld.type.name().startsWith("REF_")) {
						isExist = true;
						break;
					}
				}
			}
			if (! isExist && doc.reportFooterFields != null) {
				for (AdminDocumentField fld : doc.reportFooterFields) {
					if (fld.type.name().startsWith("REF_")) {
						isExist = true;
						break;
					}
				}
			}
		}

		//bu kisimda sadece: cari, kasa, banka modulleri olabilir.
		if (isExist) {
			String refRel = "";
			Module module = Module.valueOf(moduleName);
			result = new StringBuilder("select ");
			switch (module) {
				case contact: {
					refRel = "t.contact_id";
					result.append("r.code as ref_no, ");
					break;
				}
				case safe: {
					refRel = "t.safe_id";
					result.append("r.code as ref_no, ");
					break;
				}
				case bank: {
					refRel = "t.bank_id";
					result.append("r.account_no as ref_no, ");
					break;
				}
			}
			result.append("r.name as ref_name, t.amount as ref_amount, t.exc_code as ref_currency from ");
			result.append(module.name());
			result.append("_trans as t ");
			result.append("inner join ");
			result.append(module.name());
			result.append(" as r on r.id = ");
			result.append(refRel);
			result.append(" where t.id = ");
			result.append(refId);
		}

		return (result != null ? result.toString() : null);
	}
	
	private static String buildSqlQuery(Integer documentId, boolean isDetailBand) {
		StringBuilder selectSB = new StringBuilder();

		String mainTableName = "";
		String wherePart = "";
		Set<String> innerSet = new HashSet<String>();
		Set<String> leftSet = new HashSet<String>();

		AdminDocument doc = AdminDocument.findById(documentId);
		if (doc != null) {
			List<AdminDocumentField> fieldList = null;
			if (isDetailBand) {
				fieldList = doc.detailFields;
				if (doc.reportTitleFields != null) {
					for (AdminDocumentField fld : doc.reportTitleFields) {
						if (FieldType.SUM_OF.equals((fld.type))) fieldList.add(fld);
					}
				}
				if (doc.pageTitleFields != null)  {
					for (AdminDocumentField fld : doc.pageTitleFields) {
						if (FieldType.SUM_OF.equals((fld.type))) fieldList.add(fld);
					}
				}
				if (doc.pageFooterFields != null) {
					for (AdminDocumentField fld : doc.pageFooterFields) {
						if (FieldType.SUM_OF.equals((fld.type))) fieldList.add(fld);
					}
				}
				if (doc.reportFooterFields != null) {
					for (AdminDocumentField fld : doc.reportFooterFields) {
						if (FieldType.SUM_OF.equals((fld.type))) fieldList.add(fld);
					}
				}
				
			} else {
				fieldList = new ArrayList<AdminDocumentField>();
				if (doc.reportTitleFields != null) fieldList.addAll(doc.reportTitleFields);
				if (doc.pageTitleFields != null) fieldList.addAll(doc.pageTitleFields);
				if (doc.pageFooterFields != null) fieldList.addAll(doc.pageFooterFields);
				if (doc.reportFooterFields != null) fieldList.addAll(doc.reportFooterFields);
			}

			if (fieldList != null && fieldList.size() > 0) {
				for (AdminDocumentField field : fieldList) {

					/*
					 * Select string alani
					 */
					if (field.isDbField) {
						if (! isDetailBand && FieldType.SUM_OF.equals(field.type)) continue;
						if (field.module == null) {
							selectSB.append(field.name);
							if (field.nickName != null && ! field.nickName.trim().isEmpty()) {
								selectSB.append(" as ");
								selectSB.append(field.nickName);
							}
							selectSB.append(", ");
						}

						/*
						 * Iliskiler icin tablo isimleri cekilir
						 */
						Map<String, String> relMap = findRelation(doc.module, doc.header, isDetailBand);
						if (FieldType.SUM_OF.equals(field.type)) {
							relMap = findRelation(doc.module, doc.header, true);
						}

						String tableName = field.name.substring(0, field.name.indexOf("."));
						String rel = relMap.get(tableName);
						
						if (rel == null) {
							log.error("For " + tableName + ", relation map is null!!");
							continue;
						}
						
						if (rel.startsWith(" INNER")) {
							innerSet.add(rel);
						} else if (rel.startsWith(" LEFT")) {
							leftSet.add(rel);
						}
					}
				}
				
				Map<String, String> relMap = findRelation(doc.module, doc.header, isDetailBand);
				mainTableName = relMap.keySet().iterator().next();
				wherePart = relMap.get(mainTableName);
			}
		}

		if (selectSB.length() > 0) {
			//Master/Detail bilgilerin arasinda yansi hesap bilgilerinin de olmasi gerekir!
			switch (doc.module) {
				case stock:
				case invoice: {
					if (! isDetailBand) selectSB.append("ref_id, ref_module, ");
					break;
				}
				case contact:
				case safe:
				case bank: {
					selectSB.append("ref_id, ref_module, ");
					break;
				}
			}
			//Master bilgilerin arasinda ana tanitim tablosunun da olmasi gerekir!
			if (! isDetailBand) {
				Map<String, String> relMap = findRelation(doc.module, doc.header, false);
				String relTableName = null;
				switch (doc.module) {
					case safe: {
						selectSB.append("safe.id as infoId");
						relTableName = "safe";
						break;
					}
					case bank: {
						selectSB.append("bank.id as infoId");
						relTableName = "bank";
						break;
					}
					default: {
						selectSB.append("contact.id as infoId");
						relTableName = "contact";
						break;
					}
				}
				if (relTableName != null) {
					String rel = relMap.get(relTableName);
					if (rel.startsWith(" INNER")) {
						innerSet.add(rel);
					} else if (rel.startsWith(" LEFT")) {
						leftSet.add(rel);
					}
				}
			} else {
				//detay tablolarinda son virgulden sonra hazir olarak eklenen alan
				selectSB.append("0");
			}

			selectSB.append(" FROM " + mainTableName);
			for (String inner : innerSet) {
				selectSB.append(inner);
			}
			for (String left : leftSet) {
				selectSB.append(left);
			}
			selectSB.append(wherePart);
			selectSB.insert(0, "select ");
		}
		
		return selectSB.toString();
	}

	private static Map<String, String> findRelation(Module module, String header, boolean isDetail) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		switch (module) {

			case contact: {
				result.put("contact_trans", 		" WHERE contact_trans.id = :id");
				result.put("contact", 				" INNER JOIN contact ON contact.id = contact_trans.contact_id");
				result.put("contact_category", 		" LEFT JOIN contact_category ON contact_category.id = contact.category_id");
				result.put("sale_seller", 			" LEFT JOIN sale_seller ON sale_seller.id = contact.seller_id");
				result.put("contact_trans_source", 	" LEFT JOIN contact_trans_source ON contact_trans_source.id = contact_trans.trans_source_id");
				result.put("global_trans_point", 	" LEFT JOIN global_trans_point ON global_trans_point.id = contact_trans.trans_point_id");
				result.put("global_private_code",	" LEFT JOIN global_private_code ON global_private_code.id = contact_trans.private_code_id");

				result.put("contact_extra_fields0",	" LEFT JOIN contact_extra_fields as contact_extra_fields0 ON contact_extra_fields.id = contact.extra_fields0_id");
				result.put("contact_extra_fields1",	" LEFT JOIN contact_extra_fields as contact_extra_fields1 ON contact_extra_fields.id = contact.extra_fields1_id");
				result.put("contact_extra_fields2",	" LEFT JOIN contact_extra_fields as contact_extra_fields2 ON contact_extra_fields.id = contact.extra_fields2_id");
				result.put("contact_extra_fields3",	" LEFT JOIN contact_extra_fields as contact_extra_fields3 ON contact_extra_fields.id = contact.extra_fields3_id");
				result.put("contact_extra_fields4",	" LEFT JOIN contact_extra_fields as contact_extra_fields4 ON contact_extra_fields.id = contact.extra_fields4_id");
				result.put("contact_extra_fields5",	" LEFT JOIN contact_extra_fields as contact_extra_fields5 ON contact_extra_fields.id = contact.extra_fields5_id");
				result.put("contact_extra_fields6",	" LEFT JOIN contact_extra_fields as contact_extra_fields6 ON contact_extra_fields.id = contact.extra_fields6_id");
				result.put("contact_extra_fields7",	" LEFT JOIN contact_extra_fields as contact_extra_fields7 ON contact_extra_fields.id = contact.extra_fields7_id");
				result.put("contact_extra_fields8",	" LEFT JOIN contact_extra_fields as contact_extra_fields8 ON contact_extra_fields.id = contact.extra_fields8_id");
				result.put("contact_extra_fields9",	" LEFT JOIN contact_extra_fields as contact_extra_fields9 ON contact_extra_fields.id = contact.extra_fields9_id");
				break;
			}
			case bank: {
				result.put("bank_trans", 			" WHERE bank_trans.id = :id");
				result.put("bank", 					" INNER JOIN bank ON bank.id = bank_trans.bank_id");
				result.put("bank_expense", 			" LEFT JOIN bank_expense ON bank_expense.id = bank_trans.expense_id");
				result.put("bank_trans_source", 	" LEFT JOIN bank_trans_source ON bank_trans_source.id = bank_trans.trans_source_id");
				result.put("global_trans_point", 	" LEFT JOIN global_trans_point ON global_trans_point.id = bank_trans.trans_point_id");
				result.put("global_private_code", 	" LEFT JOIN global_private_code ON global_private_code.id = bank_trans.private_code_id");
				break;
			}
			case safe: {
				result.put("safe_trans", 			" WHERE safe_trans.id = :id");
				result.put("safe", 					" INNER JOIN safe ON safe.id = safe_trans.safe_id");
				result.put("safe_expense", 			" LEFT JOIN safe_expense ON safe_expense.id = safe_trans.expense_id");
				result.put("safe_trans_source", 	" LEFT JOIN safe_trans_source ON safe_trans_source.id = safe_trans.trans_source_id");
				result.put("global_trans_point", 	" LEFT JOIN global_trans_point ON global_trans_point.id = safe_trans.trans_point_id");
				result.put("global_private_code", 	" LEFT JOIN global_private_code ON global_private_code.id = safe_trans.private_code_id");
				break;
			}
			case stock:
			case order:
			case waybill:
			case invoice: {
				String master = module.name() + "_trans";
				String detail = module.name() + "_trans_detail";
				if (isDetail) {
					result.put(detail, 					String.format(" WHERE %s.trans_id = :id", detail));
					result.put("stock", 				String.format(" INNER JOIN stock ON stock.id = %s.stock_id", detail));
					result.put("stock_category", 		" LEFT JOIN stock_category ON stock_category.id = stock.category_id");

					result.put("stock_extra_fields0",	" LEFT JOIN stock_extra_fields as contact_extra_fields0 ON stock_extra_fields.id = stock.extra_fields0_id");
					result.put("stock_extra_fields1",	" LEFT JOIN stock_extra_fields as contact_extra_fields1 ON stock_extra_fields.id = stock.extra_fields1_id");
					result.put("stock_extra_fields2",	" LEFT JOIN stock_extra_fields as contact_extra_fields2 ON stock_extra_fields.id = stock.extra_fields2_id");
					result.put("stock_extra_fields3",	" LEFT JOIN stock_extra_fields as contact_extra_fields3 ON stock_extra_fields.id = stock.extra_fields3_id");
					result.put("stock_extra_fields4",	" LEFT JOIN stock_extra_fields as contact_extra_fields4 ON stock_extra_fields.id = stock.extra_fields4_id");
					result.put("stock_extra_fields5",	" LEFT JOIN stock_extra_fields as contact_extra_fields5 ON stock_extra_fields.id = stock.extra_fields5_id");
					result.put("stock_extra_fields6",	" LEFT JOIN stock_extra_fields as contact_extra_fields6 ON stock_extra_fields.id = stock.extra_fields6_id");
					result.put("stock_extra_fields7",	" LEFT JOIN stock_extra_fields as contact_extra_fields7 ON stock_extra_fields.id = stock.extra_fields7_id");
					result.put("stock_extra_fields8",	" LEFT JOIN stock_extra_fields as contact_extra_fields8 ON stock_extra_fields.id = stock.extra_fields8_id");
					result.put("stock_extra_fields9",	" LEFT JOIN stock_extra_fields as contact_extra_fields9 ON stock_extra_fields.id = stock.extra_fields9_id");
				} else {
					result.put(master, 					String.format(" WHERE %s.id = :id", master));
					result.put(detail, 					String.format(" INNER JOIN %s ON %s.trans_id = %s.id", detail, detail, master));
					result.put("contact", 				String.format(" LEFT JOIN contact ON contact.id = %s.contact_id", master));
					result.put("stock_depot", 			String.format(" LEFT JOIN stock_depot ON stock_depot.id = %s.depot_id", master));
					result.put("global_trans_point", 	String.format(" LEFT JOIN global_trans_point ON global_trans_point.id = %s.trans_point_id", master));
					result.put("global_private_code", 	String.format(" LEFT JOIN global_private_code ON global_private_code.id = %s.private_code_id", master));
					result.put("sale_seller", 			String.format(" LEFT JOIN sale_seller ON sale_seller.id = %s.seller_id", master));
					if (Module.stock.equals(module)) {
						result.put("ref_stock_depot",	" LEFT JOIN stock_depot ON stock_depot.id = stock_trans.ref_depot_id");
					}
				}
				break;
			}
			case bill:
			case cheque: {
				if (header == null) break; 
				if (header.endsWith("payroll")) {
					if (isDetail) {
						result.put("chqbll_payroll_detail",	" WHERE chqbll_payroll_detail.trans_id = :id");
						result.put("chqbll_type", 			" LEFT JOIN chqbll_type ON chqbll_type.id = chqbll_payroll_detail.cbtype_id");
					} else {
						result.put("chqbll_payroll",		" WHERE chqbll_payroll.id = :id");
						result.put("contact", 				" INNER JOIN contact ON contact.id = chqbll_payroll.contact_id");
						result.put("chqbll_payroll_source", " LEFT JOIN chqbll_payroll_source ON chqbll_payroll_source.id = chqbll_payroll.trans_source_id");
						result.put("global_trans_point", 	" LEFT JOIN global_trans_point ON global_trans_point.id = chqbll_payroll.trans_point_id");
						result.put("global_private_code",	" LEFT JOIN global_private_code ON global_private_code.id = chqbll_payroll.private_code_id");
					}
				} else if (header.endsWith("transaction")) {
					if (isDetail) {
						result.put("chqbll_trans_detail",	" WHERE chqbll_trans_detail.trans_id = :id");
						result.put("chqbll_payroll_detail", " INNER JOIN chqbll_payroll_detail ON chqbll_payroll_detail.id = chqbll_trans_detail.detail_id");
						result.put("chqbll_type", 			" LEFT JOIN chqbll_type ON chqbll_type.id = chqbll_payroll_detail.cbtype_id");
					} else {
						result.put("chqbll_trans",			" WHERE chqbll_trans.id = :id");
						result.put("contact", 				" LEFT JOIN contact ON contact.id = chqbll_trans.contact_id");
						result.put("safe", 					" LEFT JOIN safe ON safe.id = chqbll_trans.safe_id");
						result.put("bank", 					" LEFT JOIN bank ON bank.id = chqbll_trans.bank_id");
						result.put("chqbll_payroll_source", " LEFT JOIN chqbll_payroll_source ON chqbll_payroll_source.id = chqbll_trans.trans_source_id");
						result.put("global_trans_point", 	" LEFT JOIN global_trans_point ON global_trans_point.id = chqbll_trans.trans_point_id");
						result.put("global_private_code",	" LEFT JOIN global_private_code ON global_private_code.id = chqbll_trans.private_code_id");
					}
				} else if (header.endsWith("partial")) {
					result.put("chqbll_payroll_detail",	" WHERE chqbll_payroll_detail.id = :id");
					if (isDetail) {
						result.put("chqbll_detail_partial", " INNER JOIN chqbll_detail_partial ON chqbll_detail_partial.detail_id = chqbll_payroll_detail.id");
						result.put("safe", 					" LEFT JOIN safe ON safe.id = chqbll_detail_partial.safe_id");
					} else {
						result.put("contact", 				" INNER JOIN contact ON contact.id = chqbll_payroll_detail.contact_id");
					}
				}
				break;
			}

		}

		return result;
	}

	private static Field findCarryingOverField(String fieldName) {
		return new Field(fieldName, "amount", 13, FieldType.CURRENCY);
	}

	public static Map<String, String> findCarryingOverOptions(String moduleName, String header) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		Module module = Module.valueOf(moduleName);
		switch (module) {
			case stock:
			case order:
			case waybill:
			case invoice: {
				result.put(module.name() + "_trans_detail.amount", Messages.get("amount"));
				result.put(module.name() + "_trans_detail.total", Messages.get("total"));
				break;
			}
			case bill:
			case cheque: {
				if (header != null && header.endsWith("partial")) {
					result.put("chqbll_detail_partial.amount", Messages.get("amount"));
				} else {
					result.put("chqbll_partial_detail.amount", Messages.get("amount"));
				}
				break;
			}

		}

		return result;
	}

	private static String getColumnTitleSep(List<AdminDocumentField> detailFields) {
		StringBuilder sb = new StringBuilder(emptyLine);
		for (AdminDocumentField field : detailFields) {
			sb.replace(field.column-1, field.column-1+field.width, StringUtils.fill("-", field.width));
		}
		return StringUtils.trimRight(sb.toString());
	}
	
	private static String getColumnTitlelabels(List<AdminDocumentField> detailFields) {
		StringBuilder sb = new StringBuilder(emptyLine);
		for (AdminDocumentField field : detailFields) {
			String val = field.label;
			if (val.length() > field.width) val = val.substring(0, field.width);
			sb.replace(field.column-1, field.column-1+field.width, StringUtils.pad(val, field.width, field.labelAlign));
		}
		return StringUtils.trimRight(sb.toString());
	}

	private static int getColumnTitleColumn(List<AdminDocumentField> detailFields) {
		int result = 255;
		for (AdminDocumentField field : detailFields) {
			if (field.column < result) result = field.column;
		}
		return result - 1;
	}

	static class RowInfo {
		DocBand band;
		boolean hasBandLabels;
		int bandLimit;
		List<AdminDocumentField> fieldList;
		List<String> rows;
		SqlRow dataRow;
		SqlRow refRow;
		boolean isSinglePage;
		int pageNo;
		int pageCount;
		int rowNo;
		int startRow;
		String carryingOverName;
		double carryingOverAmount = 0;
		Field carryingOver;
		Module module;
		String header;
		Map<String, Double> sumOfMap;
	}

}
