package org.opendocumentformat.tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.example.documenttests.ArgumentType;
import org.example.documenttests.CommandType;
import org.example.documenttests.DocumenttestsType;
import org.example.documenttests.DocumenttestsconfigType;
import org.example.documenttests.DocumenttestsreportType;
import org.example.documenttests.EnvType;
import org.example.documenttests.InputReportType;
import org.example.documenttests.OutputReportType;
import org.example.documenttests.TargetReportType;
import org.example.documenttests.TargetType;
import org.example.documenttests.TestType;
import org.example.documenttests.TestreportType;
import org.opendocumentformat.tester.InputCreator.ODFType;
import org.opendocumentformat.tester.InputCreator.ODFVersion;
import org.opendocumentformat.tester.validator.OutputChecker;

public class Tester {

	private final List<DocumenttestsType> tests;
	private final List<DocumenttestsconfigType> configs;

	private final OutputChecker outputchecker = new OutputChecker();

	public Tester() {
		tests = new ArrayList<DocumenttestsType>();
		configs = new ArrayList<DocumenttestsconfigType>();
	}

	public void addTests(DocumenttestsType tests) {
		this.tests.add(tests);
	}

	public void addConfig(DocumenttestsconfigType config) {
		this.configs.add(config);
	}

	public DocumenttestsreportType runAllTests() {
		DocumenttestsreportType report = new DocumenttestsreportType();
		List<TestreportType> testreports = report.getTestreport();
		for (DocumenttestsType t : tests) {
			ODFType type = null;
			switch (t.getInputmimetype()) {
			case APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT:
				type = InputCreator.ODFType.text;
				break;
			case APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION:
				type = InputCreator.ODFType.presentation;
				break;
			case APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
				type = InputCreator.ODFType.spreadsheet;
				break;
			}
			for (TestType test : t.getTest()) {
				testreports.add(runTest(test, type));
			}
		}
		return report;
	}

	public TestreportType runTest(TestType test, ODFType type) {
		TestreportType report = new TestreportType();
		report.setName(test.getName());
		InputCreator creator = new InputCreator(type, ODFVersion.v1_2);
		String path = creator.createInput(test.getInput());
		InputReportType inputReport = new InputReportType();
		inputReport.setValidation(outputchecker.check(path));
		report.setInput(inputReport);
		for (DocumenttestsconfigType config : configs) {
			for (TargetType target : config.getTarget()) {
				report.getTarget().add(runTest(target, path));
			}
		}
		(new File(path)).delete();
		return report;
	}

	public TargetReportType runTest(TargetType target, String path) {
		TargetReportType report = new TargetReportType();
		report.setName(target.getName());
		OutputReportType output = new OutputReportType();
		report.setOutput(output);
		for (CommandType cmd : target.getCommand()) {
			path = runCommand(cmd, path);
		}
		output.setPath(path);
		output.setSize((new File(path)).length());
		output.setValidation(outputchecker.check(path));
		return report;
	}

	public String runCommand(CommandType command, String inpath) {
		String cmd[] = new String[command.getInfileOrOutfileOrArgument().size() + 1];
		String outpath = inpath;
		cmd[0] = command.getExe();
		int i = 1;
		for (JAXBElement<?> a : command.getInfileOrOutfileOrArgument()) {
			String name = a.getName().getLocalPart();
			if (a.getValue() instanceof ArgumentType) {
				cmd[i] = ((ArgumentType) a.getValue()).getValue();
			} else if (name.equals("infile")) {
				cmd[i] = inpath;
			} else if (name.equals("outfile")) {
				File f = null;
				try {
					f = File.createTempFile("output", ".odt");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				cmd[i] = outpath = f.getAbsolutePath();
			}
			++i;
		}
		String env[] = new String[command.getEnv().size()];
		i = 0;
		for (EnvType e : command.getEnv()) {
			String value = System.getenv(e.getName());
			if (e.getValue() != null) {
				value = e.getValue();
			}
			if (value == null) {
				value = "";
			}
			env[i] = e.getName() + "=" + value;
			++i;
		}
		runCommand(cmd, env);
		return outpath;
	}

	private void runCommand(String cmd[], String env[]) {
		try {
			String line;
			Process p = Runtime.getRuntime().exec(cmd, env);
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			while ((line = bri.readLine()) != null) {
				System.err.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				System.err.println(line);
			}
			bre.close();
			p.waitFor();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}
