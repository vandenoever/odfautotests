package org.opendocumentformat.tester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.opendocumentformat.tester.validator.OdfOutputChecker;
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

			documentBuilder = OdfOutputChecker.createDocumentBuilder();
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

		DocumenttestsType loadTests(String path, Map<String, String> nsmap)
				throws JAXBException, SAXException, IOException {
			Document doc = documentBuilder.parse(new File(path));
			getNSPrefixMap(doc.getDocumentElement().getAttributes(), nsmap);
			JAXBElement<DocumenttestsType> root;
			root = unmarshaller.unmarshal(new StreamSource(new File(path)),
					DocumenttestsType.class);
			return root.getValue();
		}

		DocumenttestsconfigType loadConfig(String path) throws JAXBException {
			JAXBElement<DocumenttestsconfigType> root;
			root = unmarshaller.unmarshal(new StreamSource(new File(path)),
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

	public static void main(String[] args) {
		try {
			(new File("tmp")).mkdir();
		} catch (Exception e) {
		}
		try {
			(new File("tmp/out")).mkdir();
		} catch (Exception e) {
		}
		Tester tester = new Tester();
		Loader loader;
		try {
			loader = (new Main()).new Loader();
		} catch (JAXBException e1) {
			throw new RuntimeException(e1);
		}
		// the each argument can be either a config file or a set of tests
		for (String arg : args) {
			try {
				Map<String, String> nsmap = new HashMap<String, String>();
				DocumenttestsType tests = loader.loadTests(arg, nsmap);
				tester.addTests(tests, nsmap);
			} catch (Exception e) {
				int linenumber = loader.handler.linenumber;
				try {
					DocumenttestsconfigType config = loader.loadConfig(arg);
					tester.addConfig(config);
				} catch (JAXBException e2) {
					Throwable ex = e;
					if (loader.handler.linenumber > linenumber) {
						linenumber = loader.handler.linenumber;
						ex = e2;
					}
					System.err.print("Could not load " + arg + " line "
							+ linenumber + ": ");
					if (ex.getMessage() == null) {
						if (e.getCause() != null) {
							ex = e.getCause();
						}
						if (ex.getMessage() == null) {
							ex.printStackTrace();
						}
						System.err.println(ex.getMessage());
					} else {
						System.err.println(ex.getMessage());
					}
				}
			}
		}
		DocumenttestsreportType report = tester.runAllTests();
		loader.writeReport(report, "report.xml");
		writeHTML("report.xml", "report.html");
	}
}
