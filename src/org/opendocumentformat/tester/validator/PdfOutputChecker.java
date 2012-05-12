package org.opendocumentformat.tester.validator;

import org.example.documenttests.CommandReportType;
import org.opendocumentformat.tester.Tester;

public class PdfOutputChecker {

	public PdfOutputChecker() {

	}

	public void createPngs(String pdfpath) {
		String cmd[] = { "/usr/bin/pdftoppm", "-png", "-f", "1", "-l", "1",
				"-r", "150", pdfpath, "out" };
		String env[] = {};
		CommandReportType r = Tester.runCommand(cmd, env);
		System.out.println(r.getStderr());
		System.out.println(r.getStdout());
	}

}
