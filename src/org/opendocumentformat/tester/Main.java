package org.opendocumentformat.tester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.example.documenttests.DocumenttestsType;
import org.example.documenttests.DocumenttestsconfigType;
import org.example.documenttests.DocumenttestsreportType;
import org.example.documenttests.FiletypeType;
import org.example.documenttests.TargetOutputType;
import org.example.documenttests.TargetType;
import org.example.documenttests.TestType;
import org.opendocumentformat.tester.validator.OdfChecker;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Main {

	class Loader {
		final JAXBContext jaxbContext;
		final Unmarshaller unmarshaller;
		final Marshaller marshaller;
		final Handler handler;
		private final DocumentBuilder documentBuilder;

		class Handler extends ValidationEventCollector {
			public int linenumber;
			public int offset;
			public ValidationEvent lastEvent = null;

			@Override
			public boolean handleEvent(ValidationEvent event) {
				linenumber = event.getLocator().getLineNumber();
				offset = event.getLocator().getOffset();
				lastEvent = event;
				return false;
			}
		}

		Loader() throws JAXBException {
			final SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			File f = new File("documenttests.xsd");
			Source source;
			if (!f.exists()) {
				source = new StreamSource(Loader.class.getClassLoader()
						.getResourceAsStream("documenttests.xsd"));
			} else {
				source = new StreamSource(f);
			}
			Schema schema = null;
			try {
				schema = schemaFactory.newSchema(source);
			} catch (SAXException e) {
				e.printStackTrace();
			}

			jaxbContext = JAXBContext.newInstance(DocumenttestsType.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			handler = new Handler();
			unmarshaller.setEventHandler(handler);
			unmarshaller.setSchema(schema);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);

			documentBuilder = OdfChecker.createDocumentBuilder();
		}

		void getNSPrefixMap(NamedNodeMap atts, Map<String, String> nsmap) {
			nsmap.clear();
			for (int i = 0; i < atts.getLength(); ++i) {
				Node n = atts.item(i);
				if ("http://www.w3.org/2000/xmlns/".equals(n.getNamespaceURI())) {
					if ("xmlns".equals(n.getLocalName())) {
						nsmap.put("", n.getNodeValue());
					} else {
						nsmap.put(n.getLocalName(), n.getNodeValue());
					}
				}
			}
		}

		DocumenttestsType loadTests(File testFile, Map<String, String> nsmap)
				throws JAXBException, SAXException, IOException {
			Document doc = documentBuilder.parse(testFile);
			getNSPrefixMap(doc.getDocumentElement().getAttributes(), nsmap);
			JAXBElement<DocumenttestsType> root;
			root = unmarshaller.unmarshal(new StreamSource(testFile),
					DocumenttestsType.class);
			return root.getValue();
		}

		DocumenttestsconfigType loadConfig(File runConfFile)
				throws JAXBException {
			JAXBElement<DocumenttestsconfigType> root;
			root = unmarshaller.unmarshal(new StreamSource(runConfFile),
					DocumenttestsconfigType.class);
			return root.getValue();
		}

		void writeReport(DocumenttestsreportType report, String path) {
			JAXBElement<DocumenttestsreportType> root = new JAXBElement<DocumenttestsreportType>(
					new QName("http://www.example.org/documenttests",
							"documenttestsreport"),
					DocumenttestsreportType.class, report);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				marshaller.marshal(root, out);
				unmarshaller.unmarshal(new StreamSource(
						new ByteArrayInputStream(out.toByteArray())),
						DocumenttestsreportType.class);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
			try {
				FileOutputStream f = new FileOutputStream(path);
				f.write(out.toByteArray());
				f.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void writeHTML(String inpath, String outpath) {
		Source source;
		File f = new File("report2html.xsl");
		if (!f.exists()) {
			source = new StreamSource(Loader.class.getClassLoader()
					.getResourceAsStream("report2html.xsl"));
		} else {
			source = new StreamSource(f);
		}
		TransformerFactory transFact = TransformerFactory.newInstance();
		try {
			Transformer trans = transFact.newTransformer(source);
			source = new StreamSource(new FileInputStream(inpath));
			Result result = new StreamResult(new FileOutputStream(outpath));
			trans.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static private void fatalFileError(int linenumber, Throwable t, File file) {
		while (t.getMessage() == null & t.getCause() != null) {
			t = t.getCause();
		}
		System.err.println("Could not load " + file.getPath() + " line "
				+ linenumber + ": " + t.getMessage());
		System.exit(1);
	}

	/**
	 * Return the suffix as used in OdfAutoTests. PDF has suffix .pdf. The ODF
	 * types have more complicated suffixes that encodes the file type into the
	 * name. For example, and ODF 1.0 text file in package format (enumeration
	 * value ODT_1_0) has suffix '-1.0.odt' and the single xml version has
	 * (enumeration value ODT_1_0_XML) has suffix '-1.0xml.odt'.
	 * 
	 * @param type
	 * @return
	 */
	static String getSuffixForFileType(FiletypeType type) {
		if (type.equals(FiletypeType.PDF)) {
			return ".pdf";
		}
		String value = type.value();
		String subtype = value.substring(3);
		String ext = value.substring(0, 3);
		return "_" + subtype + "." + ext;
	}

	// derive the file type from the file suffix. see getSuffix()
	static FiletypeType getFileType(String name) {
		if (name.endsWith(".pdf")) {
			return FiletypeType.PDF;
		}
		String ext = name.substring(name.length() - 3);
		int pos = name.lastIndexOf('_');
		if (pos == -1) {
			return null;
		}
		String subtype = name.substring(pos + 1, name.length() - 4);
		FiletypeType type;
		try {
			type = FiletypeType.fromValue(ext + subtype);
		} catch (IllegalArgumentException e) {
			type = null;
		}
		return type;
	}

	static String getTestName(String filename) {
		int end = filename.lastIndexOf('_');
		if (end == -1) {
			end = filename.length();
		}
		return filename.substring(0, end);
	}

	private static void createInputFiles(RunConfiguration conf,
			DocumenttestsType tests) {
		for (TestType test : tests.getTest()) {
			File target = new File(conf.inputDir, test.getName()
					+ getSuffixForFileType(test.getInput().getType()));
			InputCreator creator = new InputCreator(test.getInput().getType());
			creator.createInput(target, test.getInput());
		}
	}

	static private DocumenttestsconfigType inferConfig(RunConfiguration rc,
			DocumenttestsType tests) {
		Map<String, InferredTest> testNames = new HashMap<String, InferredTest>();
		for (TestType test : tests.getTest()) {
			InferredTest i = new InferredTest(test.getInput().getType());
			testNames.put(test.getName(), i);
		}
		DocumenttestsconfigType conf = new DocumenttestsconfigType();
		// look through the output directory
		for (File targetDir : rc.resultDir.listFiles()) {
			if (targetDir.isDirectory()) {
				// if at least one test result is in the directory, the dir
				// name is the name of a configuration
				TargetType target = null;
				Map<FiletypeType, TargetOutputType> t = new HashMap<FiletypeType, TargetOutputType>();
				for (File output : targetDir.listFiles()) {
					String filename = output.getName();
					String testname = Main.getTestName(filename);

					FiletypeType type = getFileType(filename);
					InferredTest test = testNames.get(testname);
					if (test != null && type != null && output.canRead()) {
						if (target == null) {
							target = new TargetType();
							target.setName(targetDir.getName());
						}
						TargetOutputType out = t.get(type);
						if (out == null) {
							out = new TargetOutputType();
							out.setOutputType(type);
							t.put(type, out);
							target.getOutput().add(out);
						}
						out.getInputTypes().add(test.input);
					}
				}
				if (target != null) {
					conf.getTarget().add(target);
				}
			}
		}
		return conf;
	}

	public static void removeMissingTargets(DocumenttestsconfigType config) {
		List<TargetType> targets = config.getTarget();
		SortedSet<String> neededExecutables = new TreeSet<String>();
		SortedSet<String> missingExecutables = new TreeSet<String>();
		for (TargetType t : config.getTarget()) {
			for (TargetOutputType o : t.getOutput()) {
				neededExecutables.add(o.getCommand().getExe());
			}
		}
		for (String exe : neededExecutables) {
			String fullexe = Tester.resolveExe(exe);
			if (exe == fullexe && !new File(exe).exists()) {
				System.err.println("Executable " + exe
						+ " was not found in PATH.");
				missingExecutables.add(exe);
			}
		}
		int i = 0;
		while (i < targets.size()) {
			TargetType t = targets.get(i);
			List<TargetOutputType> outputs = t.getOutput();
			int j = 0;
			while (j < outputs.size()) {
				TargetOutputType o = outputs.get(j);
				String exe = o.getCommand().getExe();
				if (missingExecutables.contains(exe)) {
					outputs.remove(j);
				} else {
					j++;
				}
			}
			if (outputs.size() == 0) {
				targets.remove(i);
			} else {
				i++;
			}
		}
	}

	/**
	 * The main entry point for the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// parse the command-line arguments or exit if they make no sense
		RunConfiguration conf;
		try {
			conf = RunConfiguration.parseArguments(args);
		} catch (ArgumentException e) {
			return;
		}
		Loader loader;
		try {
			loader = (new Main()).new Loader();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// load the tests file
		DocumenttestsType tests = null;
		Map<String, String> nsmap = new HashMap<String, String>();
		try {
			tests = loader.loadTests(conf.tests, nsmap);
		} catch (Throwable t) {
			fatalFileError(loader.handler.linenumber, t, conf.tests);
		}
		// create any missing input files
		createInputFiles(conf, tests);
		// if a run configuration is assigned, run the files through the
		// configured applications
		DocumenttestsconfigType config = null;
		if (conf.runConfiguration != null) {
			try {
				config = loader.loadConfig(conf.runConfiguration);
			} catch (Throwable t) {
				fatalFileError(loader.handler.linenumber, t,
						conf.runConfiguration);
			}
			removeMissingTargets(config);
		}

		Tester tester = null;
		DocumenttestsreportType report = null;
		if (config != null) {
			tester = new Tester(conf, config, tests, nsmap);
			report = tester.runAllTests();
		} else {
			config = inferConfig(conf, tests);
			tester = new Tester(conf, config, tests, nsmap);
			report = new DocumenttestsreportType();
		}
		tester.evaluateAllTests(report);
		loader.writeReport(report, "report.xml");
		writeHTML("report.xml", "report.html");
	}
}

class InferredTest {
	public final FiletypeType input;
	public final Set<FiletypeType> outputs = new HashSet<FiletypeType>();

	InferredTest(FiletypeType input) {
		this.input = input;
	}
}