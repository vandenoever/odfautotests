package org.opendocumentformat.tester.validator;

import java.io.IOException;

import org.example.documenttests.OutputReportType;
import org.example.documenttests.PdfinfoType;
import org.example.documenttests.ValidationErrorTypeType;
import org.opendocumentformat.tester.Tester;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;

public class PdfOutputChecker {

	public PdfOutputChecker() {

	}

	public void check(String pdfpath, OutputReportType report) {
		PdfReader reader = null;
		try {
			reader = new PdfReader(pdfpath);
		} catch (IOException e) {
			OdfOutputChecker.report(report.getValidation(),
					ValidationErrorTypeType.INVALIDPDFFILE, e.getMessage());
			return;
		}
		PdfinfoType info = new PdfinfoType();
		report.setPdfinfo(info);
		int n = reader.getNumberOfPages();
		info.setPages(n);
		String pathbase = pdfpath.substring(0, pdfpath.lastIndexOf('.'));
		String format = "-%01d.png";
		for (int i = 1; i <= n; ++i) {
			PdfinfoType.Page p = new PdfinfoType.Page();
			Rectangle r = reader.getPageSize(i);
			p.setHeight(r.getHeight());
			p.setWidth(r.getWidth());
			info.getPage().add(p);
			String pngpath = pathbase + String.format(format, i);
			p.setPng(pngpath);
		}
		createPngs(pdfpath, pathbase);
	}

	private void createPngs(String pdfpath, String pngpath) {
		String cmd[] = { "/usr/bin/pdftoppm", "-png", "-r", "150", pdfpath,
				pngpath };
		String env[] = {};
		Tester.runCommand(cmd, env);
	}

}
