package org.opendocumentformat.tester;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class RunConfiguration {

	final File inputDir;
	final File resultDir;
	final File runConfiguration;
	final File tests;

	private RunConfiguration(File inputDir, File resultDir,
			File runConfiguration, File tests) {
		this.inputDir = inputDir;
		this.resultDir = resultDir;
		this.runConfiguration = runConfiguration;
		this.tests = tests;
	}

	static File getReadableFile(Options options, CommandLine line, Option option)
			throws ArgumentException {
		String shorto = option.getOpt();
		File file = null;
		if (line.hasOption(shorto)) {
			file = new File(line.getOptionValue(shorto));
			if (!file.exists() || !file.isFile() || !file.canRead()) {
				error(options, "Option " + shorto + "/"
						+ option.getLongOpt()
						+ " should be followed by a readable file path.");
			}
		}
		return file;
	}

	static File getDir(Options options, CommandLine line, Option option)
			throws ArgumentException {
		String shorto = option.getOpt();
		File dir = new File(line.getOptionValue(shorto));
		if (dir.exists() && !dir.isDirectory()) {
			error(options, "Option " + shorto + "/"
					+ option.getLongOpt()
					+ " should be followed by an directory path.");
		} else if (!dir.exists() && !dir.mkdirs()) {
			error(options, "The directory "
					+ dir.getPath() + " cannot be created.");
		}
		return dir;
	}

	static void error(Options options, String msg) throws ArgumentException {
		System.out.println("Error: " + msg);
		usage(options);
		throw new ArgumentException(msg);
	}

	static RunConfiguration parseArguments(String[] args)
			throws ArgumentException {// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();

		Option inputDir = new Option("i", "input-dir", true,
				"the directory for the documents that are generated from the test definition");
		inputDir.setRequired(true);
		options.addOption(inputDir);

		Option resultDir = new Option(
				"r",
				"result-dir",
				true,
				"the directory for the results of the loading and saving of the files from input-dir");
		resultDir.setRequired(true);
		options.addOption(resultDir);

		Option runConfiguration = new Option(
				"c",
				"run-config",
				true,
				"xml file that contains the instructions for running the loading and saving of the documents in the various ODF implementations. If this option is absent, the files will not be loaded and saved, but the files from results-dir will be analyzed into a report");
		runConfiguration.setRequired(false);
		options.addOption(runConfiguration);

		Option tests = new Option("t", "tests", true,
				"xml file that contains the tests");
		tests.setRequired(true);
		options.addOption(tests);

		// parse the command line arguments
		RunConfiguration conf = null;
		try {
			CommandLine line;
			line = parser.parse(options, args);
			File i = getDir(options, line, inputDir);
			File r = getDir(options, line, resultDir);
			File c = getReadableFile(options, line, runConfiguration);
			File t = getReadableFile(options, line, tests);
			conf = new RunConfiguration(i, r, c, t);
		} catch (ParseException e) {
			error(options, e.getMessage());
		}
		return conf;
	}

	static void usage(Options options) {
		String cmd = System.getProperty("sun.java.command");
		if (cmd.endsWith(".jar")) {
			cmd = "java -jar " + cmd;
		} else {
			cmd = "java " + cmd;
		}
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp(cmd, options, true);
	}
}
