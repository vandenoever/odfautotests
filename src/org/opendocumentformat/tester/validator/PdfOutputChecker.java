package org.opendocumentformat.tester.validator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
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
			String png = String.format(format, i);
			p.setPng(pathbase + png);
			p.setPngthumb(pathbase + "-thumb" + png);

		}
		createPngs(pdfpath, pathbase, 150);
		createPngs(pdfpath, pathbase + "-thumb", 15);
	}

	private void createPngs(String pdfpath, String pngpath, int resolution) {
		PDDocument document = null;
		try {
			document = PDDocument.loadNonSeq(new File(pdfpath), null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		pngpath = pngpath + "-";
		PDFImageWriter imageWriter = new PDFImageWriter();
		try {
			imageWriter.writeImage(document, "png", null, 1, 1, pngpath, BufferedImage.TYPE_INT_ARGB, resolution);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
