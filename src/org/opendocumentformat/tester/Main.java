package org.opendocumentformat.tester;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.example.documenttests.DocumenttestsType;
import org.example.documenttests.DocumenttestsconfigType;
import org.example.documenttests.DocumenttestsreportType;

public class Main {

	class Loader {
		final JAXBContext jaxbContext;
		final Unmarshaller unmarshaller;
		final Marshaller marshaller;
		final Handler handler;

		class Handler extends ValidationEventCollector {
			public int linenumber;
			public int offset;

			@Override
			public boolean handleEvent(ValidationEvent event) {
				linenumber = event.getLocator().getLineNumber();
				offset = event.getLocator().getOffset();
				return false;
			}
		}

		Loader() throws JAXBException {
			jaxbContext = JAXBContext.newInstance(DocumenttestsType.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			handler = new Handler();
			unmarshaller.setEventHandler(handler);
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
					System.err.println("Could not load " + arg + " line: "
							+ linenumber + " " + loader.handler.linenumber);
					if (loader.handler.linenumber > linenumber) {
						System.err.println(e2.getMessage());
					} else {
						System.err.println(e.getMessage());
					}
				}
			}
		}
		DocumenttestsreportType report = tester.runAllTests();
		loader.writeReport(report, "report.xml");

	}
}
