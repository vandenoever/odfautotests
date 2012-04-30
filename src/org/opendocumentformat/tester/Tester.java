package org.opendocumentformat.tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.example.documenttests.ArgumentType;
import org.example.documenttests.CommandReportType;
import org.example.documenttests.CommandType;
import org.example.documenttests.DocumenttestsType;
import org.example.documenttests.DocumenttestsconfigType;
import org.example.documenttests.DocumenttestsreportType;
import org.example.documenttests.EnvType;
import org.example.documenttests.InputReportType;
import org.example.documenttests.OdfTypeType;
import org.example.documenttests.OutputReportType;
import org.example.documenttests.TargetReportType;
import org.example.documenttests.TargetType;
import org.example.documenttests.TestType;
import org.example.documenttests.TestreportType;
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
			OdfTypeType type = t.getOdftype();
			for (TestType test : t.getTest()) {
				testreports.add(runTest(test, type));
			}
		}
		return report;
	}

	public TestreportType runTest(TestType test, OdfTypeType type) {
		TestreportType report = new TestreportType();
		report.setName(test.getName());
		InputCreator creator = new InputCreator(type, ODFVersion.v1_2);
		String path = creator.createInput(test.getInput());
		InputReportType inputReport = new InputReportType();
		inputReport.setValidation(outputchecker.check(path));
		report.setInput(inputReport);
		for (DocumenttestsconfigType config : configs) {
			for (TargetType target : config.getTarget()) {
				if (target.getOutputType().equals(test.getOutput().getType())) {
					report.getTarget().add(runTest(target, path));
				}
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
			path = runCommand(cmd, path, report);
		}
		output.setPath(path);
		output.setSize((new File(path)).length());
		output.setValidation(outputchecker.check(path));
		return report;
	}

	public String runCommand(CommandType command, String inpath,
			TargetReportType report) {
		String cmd[] = new String[command.getInfileOrOutfileOrOutdir().size() + 1];
		String outpath = inpath;
		cmd[0] = command.getExe();
		int i = 1;
		for (JAXBElement<?> a : command.getInfileOrOutfileOrOutdir()) {
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
			} else if (name.equals("outdir")) {
				cmd[i] = "tmp";
				outpath = cmd[i] + File.separator
						+ (new File(inpath)).getName();
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
		CommandReportType cr = runCommand(cmd, env);
		report.getCommands().add(cr);
		return outpath;
	}

	private CommandReportType runCommand(String cmd[], String env[]) {
		CommandReportType cr = new CommandReportType();
		cr.setExe(cmd[0]);
		cr.setExitCode(-255);
		long start = System.nanoTime();
		try {
			String line;
			Process p = Runtime.getRuntime().exec(cmd, env);
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			String stdout = "", stderr = "";
			while ((line = bri.readLine()) != null) {
				stdout += line + "\n";
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				stderr += line + "\n";
			}
			bre.close();
			p.waitFor();
			cr.setExitCode(p.exitValue());
			if (stdout.length() > 0) {
				cr.setStdout(stdout);
			}
			if (stderr.length() > 0) {
				cr.setStderr(stderr);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		cr.setRuntime((int) ((System.nanoTime() - start) / 1000000));
		return cr;
	}
}
