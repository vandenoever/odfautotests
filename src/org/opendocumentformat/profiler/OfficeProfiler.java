package org.opendocumentformat.profiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.example.documenttests.CommandReportType;
import org.example.documenttests.OutputReportType;
import org.example.documenttests.ValidationErrorType;
import org.example.documenttests.ValidationReportType;
import org.opendocumentformat.tester.Tester;
import org.opendocumentformat.tester.validator.OdfOutputChecker;

public class OfficeProfiler {

	final static Map<String, List<String>> applications;
	final static Map<String, String> extensions;
	static String env[] = null;

	static void addEnv(String name, List<String> list) {
		list.add(name + "=" + System.getenv(name));
	}

	static {
		applications = new HashMap<String, List<String>>();
		applications.put("calligrawords", Arrays.asList("odt", "doc", "docx"));
		applications.put("calligrastage", Arrays.asList("odp", "ppt", "pptx"));
		applications.put("calligrasheets", Arrays.asList("ods", "xls", "xlsx"));
		List<String> envlist = new ArrayList<String>();
		addEnv("HOME", envlist);
		addEnv("KDEDIRS", envlist);
		addEnv("DISPLAY", envlist);
		addEnv("DBUS_SESSION_BUS_ADDRESS", envlist);
		env = envlist.toArray(new String[envlist.size()]);
		extensions = new HashMap<String, String>();
		extensions.put("odt", "odt");
		extensions.put("doc", "odt");
		extensions.put("docx", "odt");
		extensions.put("odp", "odp");
		extensions.put("ppt", "odp");
		extensions.put("pptx", "odp");
		extensions.put("ods", "ods");
		extensions.put("xls", "ods");
		extensions.put("xlsx", "ods");
	}

	static Collection<String> scanDirectory(String dir, List<String> exts)
			throws IOException {
		Set<String> files = new HashSet<String>();
		File d = new File(dir);
		File list[] = d.listFiles();
		if (list == null) {
			return files;
		}
		for (File f : list) {
			if (f.isFile()) {
				String path = f.getCanonicalPath();
				if (extensions.containsKey(getExt(path))) {
					if (path.indexOf("_8mb_") == -1
							&& path.indexOf("me07_data_at_last_row") == -1) {
						// temporary workaround for build server memory
						// limitations, the files matching that patterns are too
						// large to parse in java dom
						files.add(path);
					}
				}
			} else if (f.isDirectory()) {
				files.addAll(scanDirectory(f.getCanonicalPath(), exts));
			}
		}
		return files;
	}

	static Collection<String> getExtensions(Collection<String> officefiles) {
		Set<String> exts = new HashSet<String>();
		for (String f : officefiles) {
			exts.add(getExt(f));
		}
		List<String> e = new ArrayList<String>(exts);
		java.util.Collections.sort(e);
		return e;
	}

	static String getExt(String path) {
		return path.substring(path.lastIndexOf('.') + 1);
	}

	static String getExecutablePath(String executableName) throws IOException {
		String systemPath = System.getenv("PATH");
		String[] pathDirs = systemPath.split(File.pathSeparator);
		for (String pathDir : pathDirs) {
			File file = new File(pathDir, executableName);
			if (file.isFile()) {
				return file.getCanonicalPath();
			}
		}
		return executableName;
	}

	static List<String> readLines(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		List<String> lines = new ArrayList<String>();
		String line = br.readLine();
		while (line != null) {
			lines.add(line);
			line = br.readLine();
		}
		return lines;
	}

	static int maxbacktraces = 50;

	static Result profile(String dir, String file, Logger logger,
			ODFValidator validator) throws IOException {
		String ext = getExt(file);
		String exe = null;
		for (String f : applications.keySet()) {
			if (applications.get(f).contains(ext)) {
				exe = f;
			}
		}
		if (exe == null) {
			return null;
		}
		logger.startTest(file);
		file = (new File(dir, file)).getCanonicalPath();
		String exepath = getExecutablePath(exe);
		// profile
		File profilefile = File.createTempFile("profile", "");
		String profilefilename = profilefile.getPath();
		File roundtripfile = File.createTempFile("output",
				"." + extensions.get(ext));
		String roundtripfilename = roundtripfile.getPath();
		// in case of ODF file, do a roundtrip
		boolean isOdfFile = "odt".equals(ext) || "ods".equals(ext)
				|| "odp".equals(ext);
		List<String> args = new ArrayList<String>();
		//args.add("/usr/bin/strace");
		args.add(exepath);
		if (isOdfFile) {
			args.addAll(Arrays
					.asList("--roundtrip-filename", roundtripfilename));
		}
		args.addAll(Arrays.asList("--benchmark-loading", "--profile-filename",
				profilefilename, "--nocrashhandler", file));
		System.out.println(args);
		String argarray[] = new String[0];
		CommandReportType crt = Tester.runCommand(args.toArray(argarray), env);
		Result r = new Result();
		r.lines = readLines(profilefile);
		r.returnValue = crt.getExitCode();
		CommandReportType convert = null;
		if (crt.isTimedout()) {
			logger.failTest("Timeout!");
		} else if (r.returnValue != 0) {
			if (maxbacktraces > 0) {
				maxbacktraces -= 1;
				// generate a backtrace
				List<String> gdbargs = new ArrayList<String>(Arrays.asList(
						getExecutablePath("gdb"), "--batch",
						"--eval-command=run", "--eval-command=bt", "--args"));
				gdbargs.addAll(args);
				CommandReportType debugresult = Tester.runCommand(
						args.toArray(argarray), env);
				r.backtrace = debugresult.getStdout();
				logger.failTest(r.backtrace);
			} else {
				logger.failTest("Crash, no backtrace: limit reached.");
			}
		} else {
			if (!isOdfFile) {
				// convert ms office file to odf
				exepath = getExecutablePath("calligraconverter");
				args = Arrays.asList(exepath, file, roundtripfilename);
				System.out.println(args);
				convert = Tester.runCommand(args.toArray(argarray), env);
			}
			String err = validator.validate(roundtripfilename);
			if (err != null) {
				logger.failTest(err);
			}
		}
		roundtripfile.delete();
		profilefile.delete();
		logger.endTest(crt.getDurationMs()
				+ ((convert == null) ? 0 : convert.getDurationMs()));
		return null;
	}

	static Map<String, Result> profileAll(String dir, String loggername)
			throws IOException {
		List<String> exts = new ArrayList<String>();
		for (List<String> v : applications.values()) {
			for (String e : v) {
				exts.add(e);
			}
		}
		Collection<String> officefiles = scanDirectory(dir, exts);
		Collection<String> usedExts = getExtensions(officefiles);
		Map<String, Result> results = new HashMap<String, Result>();
		Logger log = new Logger();
		ODFValidator validator = new ODFValidator();
		for (String ext : usedExts) {
			if (loggername != null) {
				log.startTestSuite(loggername + '-' + ext);
			}
			for (String f : officefiles) {
				if (f.endsWith(ext)) {
					String relf = (new File(dir)).toURI()
							.relativize((new File(f)).toURI()).getPath();
					System.out.println(relf);
					Result result = profile(dir, relf, log, validator);
					if (result != null) {
						results.put(f, result);
					}
				}
			}
			log.endTestSuite();
		}
		return results;
	}

	static Times summarize(Object lines) {
		return null;
	}

	static int getUnaccountedTime(Object lines, Object times) {
		return 0;
	}

	static void createStackTraceGraph(Map<String, Result> results) {

	}

	public static void main(String[] args) throws IOException {
		String dir = (new File(args[0])).getAbsolutePath();
		String output = args[1];
		String loggername = null;
		if (args.length > 2) {
			loggername = args[2];
		}
		Map<String, Result> results = profileAll(dir, loggername);
		Map<String, Field> fields = new HashMap<String, Field>();
		for (Object r : results.keySet()) {
			Result result = results.get(r);
			result.times = summarize(result.lines);
			result.unaccounted = getUnaccountedTime(result.lines, result.times);
			for (String f : result.times.min.keySet()) {
				Field field = fields.get(f);
				if (field == null) {
					field = new Field();
					fields.put(f, field);
					field.count = 0;
					field.totalduration = 0;
					field.totaldurationsquared = 0;
					field.min = 10000000;
				}
				int min = result.times.min.get(f);
				int duration = result.times.max.get(f) - min;
				field.totalduration += duration;
				field.totaldurationsquared += duration * duration;
				field.count += 1;
				if (field.min > min) {
					field.min = min;
				}
			}
		}
		List<String> fieldnames = new ArrayList<String>(fields.keySet());
		java.util.Collections.sort(fieldnames, new FieldComparator(fields));

		// collect all fieldnames
		FileWriter out = new FileWriter(output);
		// write header
		out.write("filename\text\tsize\tr\tutime\tstime\tunaccounted");
		for (String f : fieldnames) {
			out.write(f + "\t");
		}
		out.write("\n");
		// write average
		out.write("average\t\t\t\t\t\t");
		for (String f : fieldnames) {
			out.write("\t");
			Field field = fields.get(f);
			if (field.count > 0) {
				out.write(field.totalduration / field.count);
			}
		}
		out.write("\n");
		// write for each analyzed file
		for (String file : results.keySet()) {
			Result result = results.get(file);
			File f = new File(file);
			String ext = getExt(file);
			out.write(file + "\t" + ext + "\t" + f.length() + "\t"
					+ result.returnValue);
			out.write("\t" + result.utime + "\t" + result.stime + "\t"
					+ result.unaccounted);
			for (String field : fieldnames) {
				out.write('\t');
				if (result.times.min.containsKey(field)) {
					out.write(result.times.max.get(field)
							- result.times.min.get(field));
				}
			}
			out.write('\n');
		}
		out.close();

		createStackTraceGraph(results);
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}
}

class FieldComparator implements Comparator<String> {

	final Map<String, Field> fields;

	FieldComparator(Map<String, Field> fields) {
		this.fields = fields;
	}

	public int compare(String e1, String e2) {
		return fields.get(e1).min - fields.get(e2).min;
	}
}

class Field {
	int min;
	int count;
	int totalduration;
	int totaldurationsquared;
}

class Times {
	Map<String, Integer> min;
	Map<Object, Integer> max;
}

class Result {
	Times times;
	List<String> lines;
	int unaccounted;
	int returnValue;
	int utime;
	int stime;
	String backtrace;
}

class ODFValidator {
	final static OdfOutputChecker odfvalidator = new OdfOutputChecker(true);

	String validate(String path) {
		OutputReportType report = new OutputReportType();
		ValidationReportType v = new ValidationReportType();
		report.setValidation(v);
		odfvalidator.check(path, report, null, null);
		if (v.getError().size() == 0) {
			return null;
		}
		String error = "";
		for (ValidationErrorType e : v.getError()) {
			error += e.getType();
			if (e.getMessage() != null) {
				error += ": " + e.getMessage() + "\n";
			}
		}
		return error;
	}

}

class Logger {
	String suitename;
	String testname;

	void startTestSuite(String name) {
		suitename = name;
		System.out.println("##teamcity[testSuiteStarted name='" + suitename
				+ "']");
		System.out.flush();
	}

	void endTestSuite() {
		System.out.println("##teamcity[testSuiteFinished name='" + suitename
				+ "']");
		System.out.flush();
		suitename = null;
	}

	void startTest(String name) {
		if (suitename != null) {
			testname = name;
			System.out.println("##teamcity[testStarted name='" + testname
					+ "']");
			System.out.flush();
		}
	}

	void endTest(int duration) {
		if (suitename != null && testname != null) {
			System.out.println("##teamcity[testFinished name='" + testname
					+ "' duration='" + duration + "']");
			System.out.flush();
			testname = null;
		}
	}

	String escape(String s) {
		return s.replaceAll("\\|", "||").replaceAll("'", "|'")
				.replaceAll("]", "|]").replaceAll("\n", "|n")
				.replaceAll("\r", "|n");
	}

	void failTest(String err) {
		if (suitename != null && testname != null) {
			if (err == null) {
				err = "";
			} else {
				err = escape(err);
			}
			System.out.println("##teamcity[testFailed name='" + testname
					+ "' details='" + err + "']");
			System.out.flush();
		}
	}
}