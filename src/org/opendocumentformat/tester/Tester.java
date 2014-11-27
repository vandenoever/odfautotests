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
import org.example.documenttests.OutputReportType;
import org.example.documenttests.OutputType;
import org.example.documenttests.PdfType;
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

	private final OdfChecker odfchecker = new OdfChecker(false);
	PdfOutputChecker pdfchecker = new PdfOutputChecker();

	public Tester(RunConfiguration runconfig, DocumenttestsconfigType config,
			DocumenttestsType tests, Map<String, String> nsmap) {
		this.runconfig = runconfig;
		this.config = config;
		this.tests = tests;
		this.nsmap = nsmap;
	}

	public DocumenttestsreportType runAllTests() {
		DocumenttestsreportType report = new DocumenttestsreportType();
		for (TestType test : tests.getTest()) {
			runTest(test, report);
		}
		return report;
	}

	public void evaluateAllTests(DocumenttestsreportType report) {
		// loop over all the tests
		for (TestType test : tests.getTest()) {
			TestreportType tr = getTargetReportType(report, test.getName());
			evaluateTest(test, tr);
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

	private void evaluateTest(TestType test, TestreportType report) {
		report.setName(test.getName());

		// validate the input file
		OutputReportType inputReport = new OutputReportType();
		ValidationReportType vreport = new ValidationReportType();
		inputReport.setValidation(vreport);
		File inputfile = new File(runconfig.inputDir, test.getName()
				+ Main.getSuffixForFileType(test.getInput().getType()));
		OutputType odfoutput = null;
		for (OutputType o : test.getOutput()) {
			odfoutput = o;
		}
		odfchecker.check(inputfile, inputReport, odfoutput, nsmap);
		inputReport.setPath(inputfile.getName());
		inputReport.setSize(inputfile.length());
		inputReport.setType(test.getInput().getType());
		report.setInput(inputReport);
		FiletypeType inputType = test.getInput().getType();

		// loop over the applications that created output
		for (TargetType target : config.getTarget()) {
			TargetReportType tr = getTargetReportType(report, target.getName());
			// loop over the types of output (odf, pdf) of those applications
			for (TargetOutputType to : target.getOutput()) {
				// loop over the types of files output by the test (odf, pdf)
				for (OutputType o : test.getOutput()) {
					if (to.getInputTypes().contains(inputType)
							&& o.getTypes().contains(to.getOutputType())) {
						String suffix = Main.getSuffixForFileType(to
								.getOutputType());
						File dir = new File(runconfig.resultDir,
								target.getName());
						File out = new File(dir, Main.getTestName(inputfile
								.getName()) + suffix);
						evaluateTest(target, out, o, nsmap, tr);
					}
				}
				if (test.getPdf() != null
						&& to.getInputTypes().contains(inputType)
						&& to.getOutputType().equals(FiletypeType.PDF)) {
					File dir = new File(runconfig.resultDir, target.getName());
					File out = new File(dir, Main.getTestName(inputfile
							.getName()) + ".pdf");
					evaluateTest(target, out, test.getPdf(), nsmap, tr);
				}
			}
		}
	}

	private void evaluateTest(TargetType target, File result, OutputType out,
			Map<String, String> nsmap, TargetReportType report) {
		OutputReportType output = getOutputReportType(report,
				Main.getFileType(result.getName()));
		output.setPath(result.getPath());
		output.setSize(result.length());
		ValidationReportType vreport = new ValidationReportType();
		output.setValidation(vreport);
		System.out.println("> " + result.getPath());
		odfchecker.check(result, output, out, nsmap);
	}

	private void evaluateTest(TargetType target, File result, PdfType pdf,
			Map<String, String> nsmap, TargetReportType report) {
		OutputReportType output = getOutputReportType(report, FiletypeType.PDF);
		output.setPath(result.getPath());
		output.setSize(result.length());
		ValidationReportType vreport = new ValidationReportType();
		output.setValidation(vreport);
		pdfchecker.check(result, output, pdf.getMask());
	}

	private void runTest(TestType test, DocumenttestsreportType report) {
		TestreportType tr = getTargetReportType(report, test.getName());
		FiletypeType inputType = test.getInput().getType();
		File inputfile = new File(runconfig.inputDir, test.getName()
				+ Main.getSuffixForFileType(inputType));
		for (TargetType target : config.getTarget()) {
			for (OutputType o : test.getOutput()) {
				runTest(target, inputType, inputfile, o, nsmap, tr);
			}
		}
		if (test.getPdf() != null) {
			for (TargetType target : config.getTarget()) {
				runTest(target, inputType, inputfile, test.getPdf(), nsmap, tr);
			}
		}
	}

	private void runTest(TargetType target, FiletypeType inputType, File path,
			OutputType out, Map<String, String> nsmap, TestreportType report) {
		System.out.println("runTest " + target.getName());
		TargetReportType tr = getTargetReportType(report, target.getName());
		for (TargetOutputType to : target.getOutput()) {
			if (to.getInputTypes().contains(inputType)
					&& out.getTypes().contains(to.getOutputType())) {
				String suffix = Main.getSuffixForFileType(to.getOutputType());
				OutputReportType or = getOutputReportType(tr,
						to.getOutputType());
				CommandReportType r = runCommand(to.getCommand(), path, suffix,
						target.getName());
				or.getCommands().add(r);
			}
		}
	}

	private void runTest(TargetType target, FiletypeType inputType, File path,
			PdfType out, Map<String, String> nsmap, TestreportType report) {
		TargetReportType tr = getTargetReportType(report, target.getName());
		for (TargetOutputType to : target.getOutput()) {
			if (to.getInputTypes().contains(inputType)
					&& to.getOutputType().equals(FiletypeType.PDF)) {
				String suffix = Main.getSuffixForFileType(to.getOutputType());
				OutputReportType or = getOutputReportType(tr,
						to.getOutputType());
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

	private void replaceVariables(String cmd[]) {
		int i;
		String pwduri = new File(System.getProperty("user.dir")).toURI()
				.toString();
		pwduri = pwduri.replace("file:/", "file:///");
		for (i = 0; i < cmd.length; i += 1) {
			cmd[i] = cmd[i].replace("${pwduri}", pwduri);
		}
	}

	private CommandReportType runCommand(CommandType command, File inpath,
			String outsuffix, String targetName) {
		String cmd[] = new String[command.getInfileOrOutfileOrOutdir().size() + 1];
		cmd[0] = command.getExe();
		int i = 1;
		File outfile = null;
		File outdir = null;
		String outfilename = Main.getTestName(inpath.getName()) + outsuffix;
		for (JAXBElement<?> a : command.getInfileOrOutfileOrOutdir()) {
			String name = a.getName().getLocalPart();
			if (a.getValue() instanceof ArgumentType) {
				cmd[i] = ((ArgumentType) a.getValue()).getValue();
			} else if (name.equals("infile")) {
				cmd[i] = inpath.getPath();
			} else if (name.equals("outfile")) {
				File dir = new File(runconfig.resultDir, targetName);
				dir.mkdirs();
				outfile = new File(dir, outfilename);
				cmd[i] = outfile.getPath();
			} else if (name.equals("outdir")) {
				outdir = new File(runconfig.resultDir, targetName);
				outdir.mkdirs();
				cmd[i] = outdir.getPath();
			}
			++i;
		}
		replaceVariables(cmd);
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
		new File(outdir, outfilename).delete();
		CommandReportType report = runCommand(cmd, env);
		if (outfile == null && outdir != null) {
			// outfile was not uses, so file was written with old name to
			// new dir, if the file type and hence the suffix was changed,
			// then the output file must be renamed
			String fromname = inpath.getName();
			if (outfilename.endsWith("pdf")) {
				fromname = fromname.substring(0, fromname.length() - 4)
						+ ".pdf";
			}
			File from = new File(outdir, fromname);
			File to = new File(outdir, outfilename);
			if (from != to) {
				System.out.println(from + " " + to);
				from.renameTo(to);
			}
		}
		return report;
	}

	public static String resolveExe(String exe) {
		File f = new File(exe);
		if (!f.exists()) {
			String paths[] = System.getenv("PATH").split(File.pathSeparator);
			for (String p : paths) {
				f = new File(p, exe);
				if (f.exists()) {
					break;
				}
			}
		}
		try {
			f = f.getCanonicalFile();
		} catch (IOException e) {
		}
		if (f.isFile()) {
			return f.getPath();
		}
		return exe;
	}

	public static CommandReportType runCommand(String cmd[],
			Map<String, String> env) {
		cmd[0] = resolveExe(cmd[0]);
		if (env == null) {
			env = new HashMap<String, String>();
		}
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
