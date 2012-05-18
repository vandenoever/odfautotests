package org.opendocumentformat.tester;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

	private final OdfOutputChecker outputchecker = new OdfOutputChecker(false);

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
		outputchecker.check(path, inputReport, null, nsmap);
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
			outputchecker.check(path, output, out, nsmap);
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
		if (!(new File(cmd[0])).isFile()) {
			return cr;
		}
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
			return cr;
		}
		try {
			p.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Reader stdout = new Reader(p.getInputStream());
		stdout.start();
		Reader stderr = new Reader(p.getErrorStream());
		stderr.start();
		try {
			Waiter w = new Waiter(Thread.currentThread());
			p.waitFor();
			w.clear();
		} catch (InterruptedException e) {
			cr.setTimedout(true);
		} finally {
			stdout.stopReading();
			stderr.stopReading();
			p.destroy();
			if (!cr.isTimedout()) {
				cr.setExitCode(p.exitValue());
			}
			cr.setDurationMs((int) ((System.nanoTime() - start) / 1000000));
		}
		return cr;
	}
}

class Reader extends Thread {

	InputStream in;
	ByteArrayOutputStream out;
	boolean keepRunning;

	Reader(InputStream in) {
		this.in = in;
		out = new ByteArrayOutputStream();
		keepRunning = true;
	}

	public void stopReading() {
		setKeepRunning(false);
		this.interrupt();
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	synchronized private void setKeepRunning(boolean value) {
		keepRunning = value;
	}

	synchronized private boolean keepRunning() {
		return keepRunning;
	}

	public void run() {
		int c;
		try {
			while (keepRunning()) {
				while (in.available() > 0) {
					c = in.read();
					out.write((byte) c);
				}
				try {
					sleep(1);
				} catch (InterruptedException e) {
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Waiter extends Thread {
	private Thread thread;

	Waiter(Thread t) {
		this.thread = t;
	}

	void clear() {
		stopWaiting();
		thread = null;
		interrupt();
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private synchronized void stopWaiting() {
		thread = null;
	}

	private synchronized void interruptThread() {
		if (thread != null) {
			thread.interrupt();
		}
	}

	public void run() {
		try {
			sleep(60000);
			interruptThread();
		} catch (InterruptedException e) {
		}
	}
}
