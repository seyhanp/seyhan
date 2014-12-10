/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.DB;
import play.i18n.Messages;
import play.mvc.Http.Response;
import utils.GlobalCons;
import utils.StringUtils;
import controllers.Application;
import controllers.admin.Settings;
import enums.ReportUnit;

/**
 * @author mdpinar
*/
public class ReportService {

	private final static Logger log = LoggerFactory.getLogger(ReportService.class);

	private final static String REPORT_DEFINITION_PATH = "./reports/";

	public static ReportResult generateReport(ReportParams params, Response res) {

		OutputStream stream = new ByteArrayOutputStream();
		Map<String, Object> paramMap = new HashMap<String, Object>();

		String fullReportName = REPORT_DEFINITION_PATH + params.modul + "/" + params.reportName + ".jrxml";

		if (params.query.equals("null")) params.query = "";
		if (params.orderBy != null && ! params.orderBy.trim().isEmpty()) paramMap.put("ORDER_BY", "order by " + params.orderBy);
		if (params.paramMap != null && params.paramMap.size() > 0) paramMap.putAll(params.paramMap);

		paramMap.put("COMPANY", Settings.getGlobal().companyName);
		paramMap.put("QUERY_STRING", params.query);
		paramMap.put("HAVING_STRING", params.having);
		paramMap.put("REPORT_RESOURCE_BUNDLE", Application.getResourceBundle());
		paramMap.put("FOR_DOTMATRIX", "");
		paramMap.put("TRU", GlobalCons.TRUE);
		paramMap.put(JRParameter.IS_IGNORE_PAGINATION, (params.reportUnit.equals(ReportUnit.Excel) || params.reportUnit.equals(ReportUnit.Csv)));

		JRExporter exporter = null;
		JasperPrint jasperPrint = null;

		Connection connection = DB.getConnection();
		try {

			File reportFile = new File(fullReportName);
			if (!reportFile.exists()) {
				log.error("File " + fullReportName + " not found.");
			} else {
				JasperDesign design = JRXmlLoader.load(reportFile.getPath());
				if (params.reportUnit.equals(ReportUnit.Text) || params.reportUnit.equals(ReportUnit.DotMatrix)) {
					design.setPageHeight(970);
					if (params.reportUnit.equals(ReportUnit.DotMatrix)) paramMap.put("FOR_DOTMATRIX", "\u0014");
				}
				JasperReport jasperReport = (JasperReport) JasperCompileManager.compileReport(design);
				jasperPrint = JasperFillManager.fillReport(jasperReport, paramMap, connection);

				if (jasperPrint.getPages().size() == 0) return new ReportResult(Messages.get("wrong.parameters"));
			}
		} catch (Exception e) {
			log.error("ERROR", e);
			return new ReportResult(Messages.get("unexpected.problem.occured", e.getMessage()));
		} finally {
			try {
				if (connection != null && ! connection.isClosed()) connection.close();
			} catch (SQLException e) {
				log.error("ERROR", e);
			}
		}

		if (params.reportUnit != null) {

			String convertedReportName = 
				StringUtils.deAccent(Messages.get("report_title." + (params.reportNameExtra != null ? params.reportNameExtra : params.reportName), Messages.get(params.modul)));

			if (params.reportUnit.equals(ReportUnit.Html)) {
				res.setContentType("text/html");
                exporter = new JRHtmlExporter();
                exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
                exporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.SIZE_UNIT, JRHtmlExporterParameter.SIZE_UNIT_POINT);
				exporter.setParameter(JRHtmlExporterParameter.ZOOM_RATIO, 1.35F);
				exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
				exporter.setParameter(JRHtmlExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.HTML_HEADER, StringUtils.getHtmHeaderForReport());
//				exporter.setParameter(JRHtmlExporterParameter.HTML_FOOTER, StringUtils.getHtmFooterForReport());

			} else if (params.reportUnit.equals(ReportUnit.Pdf)) {
				res.setContentType("application/pdf");
				res.setHeader("Content-Disposition", "attachment; filename=" + convertedReportName + ".pdf");
				exporter = new JRPdfExporter();

			} else if (params.reportUnit.equals(ReportUnit.Excel)) {
				res.setContentType("application/vnd.ms-excel");
				res.setHeader("Content-Disposition", "inline; filename=" + convertedReportName + ".xls");
				exporter = new JExcelApiExporter();
				exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
				exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
				exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
				exporter.setParameter(JRXlsExporterParameter.CHARACTER_ENCODING, "UTF-8");

			} else if (params.reportUnit.equals(ReportUnit.Text) || params.reportUnit.equals(ReportUnit.DotMatrix)) {
				res.setContentType("plain/text");
				res.setHeader("Content-Disposition", "inline; filename=" + convertedReportName + ".txt");
				exporter = new JRTextExporter();
				exporter.setParameter(JRTextExporterParameter.CHARACTER_ENCODING, "UTF-8");
				exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, 5f);
				exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, 15f);
				exporter.setParameter(JRTextExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);

			} else if (params.reportUnit.equals(ReportUnit.Csv)) {
				res.setContentType("plain/text");
				res.setHeader("Content-Disposition", "inline; filename=" + convertedReportName + ".csv");
				exporter = new JRCsvExporter();
			}

			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");

			try {
				exporter.exportReport();
			} catch (JRException e) {
				log.error("ERROR", e);
			}
		} else {
			return new ReportResult(Messages.get("unknown.report.unit"));
		}

		return 
			new ReportResult(
				new ByteArrayInputStream(
					((ByteArrayOutputStream) stream).toByteArray())
			);
	}

	public static class ReportResult {

		public String error;
		public InputStream stream;

		ReportResult(String error) {
			this.error = error;
			this.stream = null;
		}

		ReportResult(InputStream stream) {
			this.stream = stream;
			this.error = null;
		}

	}

}
