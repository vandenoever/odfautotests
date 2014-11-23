package org.opendocumentformat.tester;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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
import org.example.documenttests.TargetOutputType;
import org.example.documenttests.TargetReportType;
import org.example.documenttests.TargetType;
import org.example.documenttests.TestType;
import org.example.documenttests.TestreportType;
import org.example.documenttests.ValidationReportType;
import org.opendocumentformat.tester.validator.OdfChecker;
import org.opendocumentformat.tester.validator.PdfOutputChecker;

public class Tester {

	private final RunConfiguration runconfig;
	private final DocumenttestsType tests;
	private final DocumenttestsconfigType config;
	private final Map<String, String> nsmap;

	private final OdfChecker checker = new OdfChecker(false);

	public Tester(RunConfiguration runconfig, DocumenttestsconfigType config,
			DocumenttestsType tests, Map<String, String> nsmap) {
		this.runconfig = runconfig;
		this.config = config;
		this.tests = tests;
		this.nsmap = nsmap;
	}

	public DocumenttestsreportType runAllTests() {
		DocumenttestsreportType report = new DocumenttestsreportType();
		OdfTypeType type = tests.getOdftype();
		for (TestType test : tests.getTest()) {
			runTest(test, type, report);
		}
		return report;
	}

	public void evaluateAllTests(DocumenttestsreportType report) {
		OdfTypeType type = tests.getOdftype();
		// loop over all the tests
		for (TestType test : tests.getTest()) {
			TestreportType tr = getTargetReportType(report, test.getName());
			evaluateTest(test, type, tr);
		}
	}

	private TestreportType getTargetReportType(DocumenttestsreportType report,
			String name) {
		TestreportType tr = null;
		for (TestreportType r : report.getTestreport()) {
			if (r.getName().equals(name)) {
				tr = r;
			}
		}
		if (tr == null) {
			tr = new TestreportType();
			tr.setName(name);
			report.getTestreport().add(tr);
		}
		return tr;
	}

	private TargetReportType getTargetReportType(TestreportType report,
			String name) {
		TargetReportType tr = null;
		for (TargetReportType r : report.getTarget()) {
			if (r.getName().equals(name)) {
				tr = r;
			}
		}
		if (tr == null) {
			tr = new TargetReportType();
			tr.setName(name);
			report.getTarget().add(tr);
		}
		return tr;
	}

	private OutputReportType getOutputReportType(TargetReportType tr,
			FiletypeType o) {
		OutputReportType or = null;
		for (OutputReportType r : tr.getOutput()) {
			if (r.getType().equals(o)) {
				or = r;
			}
		}
		if (or == null) {
			or = new OutputReportType();
			or.setType(o);
			tr.getOutput().add(or);
		}
		return or;
	}

	private void evaluateTest(TestType test, OdfTypeType type,
			TestreportType report) {
		report.setName(test.getName());

		// validate the input file
		OutputReportType inputReport = new OutputReportType();
		ValidationReportType vreport = new ValidationReportType();
		inputReport.setValidation(vreport);
		File inputfile = new File(runconfig.inputDir, test.getName()
				+ Main.getSuffix(type));
		OutputType output = null;
		for (OutputType o : test.getOutput()) {
			if (o.getType().equals(FiletypeType.ZIP)) {
				output = o;
			}
		}
		checker.check(inputfile, inputReport, output, nsmap);
		inputReport.setPath(inputfile.getName());
		inputReport.setSize(inputfile.length());
		inputReport.setType(FiletypeType.ZIP);
		report.setInput(inputReport);

		// loop over the applications that created output
		for (TargetType target : config.getTarget()) {
			TargetReportType tr = getTargetReportType(report, target.getName());
			// loop over the types of output (odf, pdf) of those applications
			for (TargetOutputType to : target.getOutput()) {
				// loop over the types of files output by the test (odf, pdf)
				for (OutputType o : test.getOutput()) {
					if (to.getOutputType().equals(o.getType())) {
						String suffix = Main.getSuffix(tests.getOdftype());
						if (o.getType().equals(FiletypeType.PDF)) {
							suffix = ".pdf";
						}
						File dir = new File(runconfig.resultDir,
								target.getName());
						File out = new File(dir, replaceSuffix(
								inputfile.getName(), suffix));
						evaluateTest(target, out, o, nsmap, tr);
					}
				}
			}
		}
	}

	private void evaluateTest(TargetType target, File result, OutputType out,
			Map<String, String> nsmap, TargetReportType report) {
		OutputReportType output = getOutputReportType(report, out.getType());
		output.setPath(result.getPath());
		output.setSize(result.length());
		ValidationReportType vreport = new ValidationReportType();
		output.setValidation(vreport);
		if (out.getType() == FiletypeType.ZIP
				|| out.getType() == FiletypeType.XML) {
			checker.check(result, output, out, nsmap);
		} else if (out.getType() == FiletypeType.PDF) {
			PdfOutputChecker pdf = new PdfOutputChecker();
			pdf.check(result, output, out.getMask());
		}
	}

	private void runTest(TestType test, OdfTypeType type,
			DocumenttestsreportType report) {
		TestreportType tr = getTargetReportType(report, test.getName());
		File inputfile = new File(runconfig.inputDir, test.getName()
				+ Main.getSuffix(type));
		for (TargetType target : config.getTarget()) {
			for (OutputType o : test.getOutput()) {
				runTest(target, inputfile, o, nsmap, tr);
			}
		}
	}

	private void runTest(TargetType target, File path, OutputType out,
			Map<String, String> nsmap, TestreportType report) {
		System.out.println("runTest " + target.getName());
		TargetReportType tr = getTargetReportType(report, target.getName());
		tr.setName(target.getName());
		for (TargetOutputType to : target.getOutput()) {
			if (to.getOutputType().equals(out.getType())) {
				String suffix = Main.getSuffix(tests.getOdftype());
				if (to.getOutputType().equals(FiletypeType.PDF)) {
					suffix = ".pdf";
				}
				OutputReportType or = getOutputReportType(tr, out.getType());
				CommandReportType r = runCommand(to.getCommand(), path, suffix,
						target.getName());
				or.getCommands().add(r);
			}
		}
	}

	static private String join(String a[]) {
		String str = a[0];
		for (int i = 1; i < a.length; ++i) {
			str += " " + a[i];
		}
		return str;
	}

	static private String join(Map<String, String> m) {
		String str = "";
		for (Entry<String, String> e : m.entrySet()) {
			str += e.getKey() + "=" + e.getValue() + " ";
		}
		return str;
	}

	private String replaceSuffix(String name, String newSuffix) {
		return name.substring(0, name.lastIndexOf(".")) + newSuffix;
	}

	private CommandReportType runCommand(CommandType command, File inpath,
			String outsuffix, String targetName) {
		String cmd[] = new String[command.getInfileOrOutfileOrOutdir().size() + 1];
		cmd[0] = command.getExe();
		int i = 1;
		for (JAXBElement<?> a : command.getInfileOrOutfileOrOutdir()) {
			String name = a.getName().getLocalPart();
			if (a.getValue() instanceof ArgumentType) {
				cmd[i] = ((ArgumentType) a.getValue()).getValue();
			} else if (name.equals("infile")) {
				cmd[i] = inpath.getPath();
			} else if (name.equals("outfile")) {
				File dir = new File(runconfig.resultDir, targetName);
				File f = new File(dir, replaceSuffix(inpath.getName(),
						outsuffix));
				cmd[i] = f.getPath();
			} else if (name.equals("outdir")) {
				File dir = new File(runconfig.resultDir, targetName);
				cmd[i] = dir.getPath();
			}
			++i;
		}
		Map<String, String> env = new HashMap<String, String>();
		for (EnvType e : command.getEnv()) {
			String value = System.getenv(e.getName());
			if (e.getValue() != null) {
				value = e.getValue();
			}
			if (value != null) {
				env.put(e.getName(), value);
			}
		}
		return runCommand(cmd, env);
	}

	public static CommandReportType runCommand(String cmd[],
			Map<String, String> env) {
		System.err.println("Running '" + join(env) + " " + join(cmd) + "'.");
		CommandReportType cr = new CommandReportType();
		cr.setExe(cmd[0]);
		cr.setExitCode(-255);
		long start = System.nanoTime();
		if (!(new File(cmd[0])).isFile()) {
			System.err.println("Executable " + cmd[0] + " cannot be found.");
			return cr;
		}
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.environment().clear();
		pb.environment().putAll(env);
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
			w.start();
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
			try {
				cr.setStderr(stdout.out.toString("UTF-8"));
				cr.setStdout(stderr.out.toString("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				// happens when there a lot of output on stderr or stdout
			}
			cr.setDurationMs((int) ((System.nanoTime() - start) / 1000000));
		}
		if (cr.getExitCode() != 0) {
			System.err.print(cr.getStderr());
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
		final int l = 1024;
		byte b[] = new byte[l];
		try {
			while (keepRunning()) {
				int n = in.available();
				while (n > 0) {
					if (n > l) {
						n = l;
					}
					in.read(b, 0, n);
					out.write(b, 0, n);
					n = in.available();
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

	private synchronized Thread getThread() {
		return thread;
	}

	public void run() {
		try {
			sleep(60000);
		} catch (InterruptedException e) {
		}
		Thread thread = getThread();
		if (thread != null) {
			thread.interrupt();
		}
	}
}
