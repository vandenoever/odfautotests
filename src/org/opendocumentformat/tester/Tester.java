package org.opendocumentformat.tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import org.example.documenttests.ArgumentType;
import org.example.documenttests.CommandReportType;
import org.example.documenttests.CommandType;
import org.example.documenttests.DocumenttestsType;
import org.example.documenttests.DocumenttestsconfigType;
import org.example.documenttests.DocumenttestsreportType;
import org.example.documenttests.EnvType;
import org.example.documenttests.FiletypeType;
import org.example.documenttests.OdfTypeType;
import org.example.documenttests.OutputReportType;
import org.example.documenttests.OutputType;
import org.example.documenttests.TargetReportType;
import org.example.documenttests.TargetType;
import org.example.documenttests.TestType;
import org.example.documenttests.TestreportType;
import org.example.documenttests.ValidationReportType;
import org.opendocumentformat.tester.InputCreator.ODFVersion;
import org.opendocumentformat.tester.validator.OdfOutputChecker;
import org.opendocumentformat.tester.validator.PdfOutputChecker;

public class Tester {

	private final Map<DocumenttestsType, Map<String, String>> tests;
	private final List<DocumenttestsconfigType> configs;

	private final OdfOutputChecker outputchecker = new OdfOutputChecker();

	public Tester() {
		tests = new HashMap<DocumenttestsType, Map<String, String>>();
		configs = new ArrayList<DocumenttestsconfigType>();
	}

	public void addTests(DocumenttestsType tests, Map<String, String> nsmap) {
		this.tests.put(tests, nsmap);
	}

	public void addConfig(DocumenttestsconfigType config) {
		this.configs.add(config);
	}

	public DocumenttestsreportType runAllTests() {
		DocumenttestsreportType report = new DocumenttestsreportType();
		List<TestreportType> testreports = report.getTestreport();
		for (Entry<DocumenttestsType, Map<String, String>> e : tests.entrySet()) {
			OdfTypeType type = e.getKey().getOdftype();
			for (TestType test : e.getKey().getTest()) {
				testreports.add(runTest(test, type, e.getValue()));
			}
		}
		return report;
	}

	public TestreportType runTest(TestType test, OdfTypeType type,
			Map<String, String> nsmap) {
		TestreportType report = new TestreportType();
		report.setName(test.getName());
		InputCreator creator = new InputCreator(type, ODFVersion.v1_2);
		String path = creator.createInput(test.getInput());
		OutputReportType inputReport = new OutputReportType();
		ValidationReportType vreport = new ValidationReportType();
		inputReport.setValidation(vreport);
		outputchecker.check(path, inputReport, null, nsmap, false);
		File inputfile = new File(path);
		inputReport.setPath(path);
		inputReport.setSize(inputfile.length());
		inputReport.setType(FiletypeType.ZIP);
		report.setInput(inputReport);
		for (DocumenttestsconfigType config : configs) {
			for (TargetType target : config.getTarget()) {
				for (int i = 0; i < test.getOutput().size(); ++i) {
					OutputType o = test.getOutput().get(i);
					if (target.getOutputType().equals(o.getType())) {
						report.getTarget().add(runTest(target, path, o, nsmap));
					}
				}
			}
		}
		return report;
	}

	public TargetReportType runTest(TargetType target, String path,
			OutputType out, Map<String, String> nsmap) {
		TargetReportType report = new TargetReportType();
		report.setName(target.getName());
		OutputReportType output = new OutputReportType();
		report.setOutput(output);
		String suffix = ".odt";
		if (target.getOutputType().equals(FiletypeType.PDF)) {
			suffix = ".pdf";
		}
		for (CommandType cmd : target.getCommand()) {
			path = runCommand(cmd, path, report, suffix);
		}
		output.setPath(path);
		output.setSize((new File(path)).length());
		ValidationReportType vreport = new ValidationReportType();
		output.setValidation(vreport);
		if (out.getType() == FiletypeType.ZIP
				|| out.getType() == FiletypeType.XML) {
			outputchecker.check(path, output, out, nsmap, false);
		} else if (out.getType() == FiletypeType.PDF) {
			PdfOutputChecker pdf = new PdfOutputChecker();
			pdf.check(path, output);
		}
		output.setType(out.getType());
		return report;
	}

	public String runCommand(CommandType command, String inpath,
			TargetReportType report, String outsuffix) {
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
					f = File.createTempFile("output", outsuffix,
							new File("tmp"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				cmd[i] = outpath = f.getPath();
			} else if (name.equals("outdir")) {
				cmd[i] = "tmp/out";
				String str = (new File(inpath)).getName();
				outpath = cmd[i] + File.separator
						+ str.substring(0, str.lastIndexOf('.')) + outsuffix;
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

	public static CommandReportType runCommand(String cmd[], String env[]) {
		CommandReportType cr = new CommandReportType();
		cr.setExe(cmd[0]);
		cr.setExitCode(-255);
		long start = System.nanoTime();
		Process p = null;
		String line;
		try {
			p = Runtime.getRuntime().exec(cmd, env);
		} catch (IOException e) {
			cr.setExitCode(-1);
			cr.setDurationMs((int) ((System.nanoTime() - start) / 1000000));
			return cr;
		}
		Waiter waiter = new Waiter(p, cr);
		waiter.start();
		BufferedReader bri = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		BufferedReader bre = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		String stdout = "", stderr = "";
		try {
			while ((line = bri.readLine()) != null) {
				stdout += line + "\n";
			}
			bri.close();
			while (bre.ready()) {
			    line = bre.readLine();
				stderr += line + "\n";
			}
			bre.close();
		} catch (IOException err) {
		}
		try {
			p.waitFor();
			waiter.report = null;
		} catch (InterruptedException e) {
		}
		cr.setExitCode(p.exitValue());
		if (stdout.length() > 0) {
			cr.setStdout(stdout);
		}
		if (stderr.length() > 0) {
			cr.setStderr(stderr);
		}
		cr.setDurationMs((int) ((System.nanoTime() - start) / 1000000));
		return cr;
	}
}

class Waiter extends Thread {
	private final Process process;
	CommandReportType report;

	public Waiter(Process process, CommandReportType report) {
		this.process = process;
		this.report = report;
	}

	public void run() {
		try {
			// wait 60 seconds
			for (int i = 0; i < 60; ++i) {
			    sleep(1000);
			    if (report == null) {
			    	return;
			    }
			}
			report.setTimedout(true);
			process.destroy();
		} catch (InterruptedException ignore) {
			return;
		}
	}
}
