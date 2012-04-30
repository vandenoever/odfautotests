package org.opendocumentformat.tester;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.example.documenttests.DocumenttestsType;
import org.example.documenttests.DocumenttestsconfigType;
import org.example.documenttests.DocumenttestsreportType;
import org.xml.sax.SAXException;

public class Main {

	class Loader {
		final JAXBContext jaxbContext;
		final Unmarshaller unmarshaller;
		final Marshaller marshaller;
		final Handler handler;

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

			String dir = "/home/oever/work/workspace/odfautotests/";
			Source source[] = new Source[] { new StreamSource(dir
					+ "documenttests.xsd") };
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
		}

		DocumenttestsType loadTests(String path) throws JAXBException {
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
			try {
				marshaller.marshal(root, System.out);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
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
				DocumenttestsType tests = loader.loadTests(arg);
				tester.addTests(tests);
			} catch (JAXBException e) {
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
						ex = e.getCause();
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

	}
}
