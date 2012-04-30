package org.opendocumentformat.tester.validator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.example.documenttests.FileTestReportType;
import org.example.documenttests.FileType;
import org.example.documenttests.OutputReportType;
import org.example.documenttests.OutputType;
import org.example.documenttests.ValidationErrorType;
import org.example.documenttests.ValidationErrorTypeType;
import org.example.documenttests.ValidationReportType;
import org.example.documenttests.XpathReportType;
import org.example.documenttests.XpathResultType;
import org.example.documenttests.XpathType;
import org.opendocumentformat.tester.InputCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class OutputChecker {

	private final ValidationDriver odf11Validator;
	private final ValidationDriver odf11manifestValidator;
	private final ValidationDriver odf11strictValidator;
	private final ValidationDriver odf12Validator;
	private final ValidationDriver odf12manifestValidator;
	private final ValidationDriver odf12dsigValidator;
	private final ErrorBuffer errorbuffer;
	private final DocumentBuilder documentBuilder;
	private final XPathFactory factory = XPathFactory.newInstance();
	private final XPath xpath = factory.newXPath();

	class ErrorBuffer extends Writer {
		private StringWriter buffer;

		public ErrorBuffer() {
			reset();
		}

		public void reset() {
			buffer = new StringWriter();
		}

		public void close() throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			buffer.write(cbuf, off, len);
		}

		public String toString() {
			return buffer.toString();
		}

	}

	private class OdfData {
		public String version;
		public Set<String> entries = new HashSet<String>();
	}

	static void extract(String target, String path) {
		InputStream i = OutputChecker.class.getClassLoader()
				.getResourceAsStream(path);
		try {
			FileOutputStream out = new FileOutputStream(target);
			int c;
			while ((c = i.read()) != -1) {
				out.write(c);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static ValidationDriver createValidationDriver(String tmpdir, String path,
			ErrorBuffer errorbuffer) {
		File rng = new File(tmpdir + File.separator + path);
		if (!rng.exists()) {
			extract(rng.getAbsolutePath(), path);
		}
		String rngpath = rng.getAbsolutePath();

		ErrorHandlerImpl eh = new ErrorHandlerImpl(errorbuffer);
		SchemaReader schemaReader = SAXSchemaReader.getInstance();
		PropertyMapBuilder properties = new PropertyMapBuilder();
		properties.put(ValidateProperty.ERROR_HANDLER, eh);
		RngProperty.CHECK_ID_IDREF.add(properties);
		properties.put(RngProperty.CHECK_ID_IDREF, null);

		ValidationDriver driver = new ValidationDriver(
				properties.toPropertyMap(), schemaReader);
		InputSource in = ValidationDriver.uriOrFileInputSource(rngpath);
		try {
			if (!driver.loadSchema(in)) {
				throw new Error("Could not load schema " + rngpath + ".");
			}
		} catch (Exception e) {
			e.printStackTrace();
			driver = null;
		}
		return driver;
	}

	public OutputChecker() {
		errorbuffer = new ErrorBuffer();
		String tmpdir = "rng";
		(new File(tmpdir)).mkdir();

		odf11Validator = createValidationDriver(tmpdir,
				"OpenDocument-schema-v1.1.rng", errorbuffer);
		odf11strictValidator = createValidationDriver(tmpdir,
				"OpenDocument-strict-schema-v1.1.rng", errorbuffer);
		odf11manifestValidator = createValidationDriver(tmpdir,
				"OpenDocument-manifest-schema-v1.1.rng", errorbuffer);
		odf12manifestValidator = createValidationDriver(tmpdir,
				"OpenDocument-v1.2-os-manifest-schema.rng", errorbuffer);
		odf12dsigValidator = createValidationDriver(tmpdir,
				"OpenDocument-v1.2-os-dsig-schema.rng", errorbuffer);
		odf12Validator = createValidationDriver(tmpdir,
				"OpenDocument-v1.2-os-schema.rng", errorbuffer);

		documentBuilder = createDocumentBuilder();
	}

	class NSMapper implements NamespaceContext {
		Map<String, String> nsmap;

		NSMapper(Map<String, String> map) {
			nsmap = map;
		}

		public String getNamespaceURI(String prefix) {
			return nsmap.get(prefix);
		}

		public String getPrefix(String nsuri) {
			for (Entry<String, String> e : nsmap.entrySet()) {
				if (e.getValue().equals(nsuri)) {
					return e.getKey();
				}
			}
			return null;
		}

		public Iterator<String> getPrefixes(String nsuri) {
			Set<String> set = new HashSet<String>();
			for (Entry<String, String> e : nsmap.entrySet()) {
				if (e.getValue().equals(nsuri)) {
					set.add(e.getKey());
				}
			}
			return set.iterator();
		}
	}

	static public DocumentBuilder createDocumentBuilder() {
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			factory.setCoalescing(false);
			factory.setValidating(false);
			factory.setExpandEntityReferences(true);
			factory.setFeature("http://xml.org/sax/features/namespaces", true);
			factory.setFeature("http://xml.org/sax/features/validation", false);
			factory.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
					false);
			factory.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					// deal with <!DOCTYPE manifest:manifest PUBLIC
					// "-//OpenOffice.org//DTD Manifest 1.0//EN" "Manifest.dtd">
					if (systemId.contains("Manifest.dtd")) {
						System.out.println(systemId); // this deactivates the
														// open office DTD
						return new InputSource(new ByteArrayInputStream(""
								.getBytes()));
					} else {
						return null;
					}
				}
			});

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return builder;
	}

	static private void report(OutputReportType report,
			ValidationErrorTypeType type, String msg) {
		report(report.getValidation(), type, msg);
	}

	static private void report(ValidationReportType report,
			ValidationErrorTypeType type, String msg) {
		ValidationErrorType error = new ValidationErrorType();
		error.setType(type);
		error.setMessage(msg);
		report.getError().add(error);
	}

	static private void report(ValidationReportType report,
			ValidationErrorTypeType type) {
		report(report, type, null);
	}

	public void check(String odfpath, OutputReportType report, OutputType out,
			Map<String, String> nsmap) {
		xpath.setNamespaceContext(new NSMapper(nsmap));

		checkMimetypeFile(odfpath, report.getValidation());

		OdfData data = new OdfData();
		try {
			ZipFile zip = new ZipFile(odfpath);
			checkStylesXml(zip, report, data, out);
			checkManifestXml(zip, report.getValidation(), data);
			checkContentXml(zip, report, data, out);
			checkMetaXml(zip, report, data, out);
			checkSettingsXml(zip, report, data, out);
		} catch (IOException e) {
			report(report, ValidationErrorTypeType.INVALIDZIPFILE,
					e.getMessage());
			e.printStackTrace();
		}
	}

	private void checkStylesXml(ZipFile zip, OutputReportType report,
			OdfData data, OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDSTYLESXML,
				ValidationErrorTypeType.MISSINGSTYLESXML, "styles.xml", out);
	}

	private void checkContentXml(ZipFile zip, OutputReportType report,
			OdfData data, OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDCONTENTXML,
				ValidationErrorTypeType.MISSINGCONTENTXML, "content.xml", out);
	}

	private void checkMetaXml(ZipFile zip, OutputReportType report,
			OdfData data, OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDMETAXML,
				ValidationErrorTypeType.MISSINGMETAXML, "meta.xml", out);
	}

	private void checkSettingsXml(ZipFile zip, OutputReportType report,
			OdfData data, OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDSETTINGSXML,
				ValidationErrorTypeType.MISSINGSETTINGSXML, "settings.xml", out);
	}

	private Document checkXml(ZipFile zip, OutputReportType report,
			OdfData data, ValidationErrorTypeType invalid,
			ValidationErrorTypeType missing, String path, OutputType out)
			throws IOException {
		ZipEntry ze = zip.getEntry(path);
		if (ze == null) {
			report(report.getValidation(), missing);
			return null;
		}
		Document doc = null;
		try {
			doc = documentBuilder.parse(zip.getInputStream(ze));
		} catch (SAXException e) {
			report(report, invalid, e.getMessage());
			return null;
		}
		checkVersion(data, doc, report.getValidation(), InputCreator.officens,
				path);
		checkXml(zip.getInputStream(ze), data.version, report, invalid);

		FileTestReportType filereport = checkExpressions(doc, out, path);
		if (filereport != null) {
			report.getFile().add(filereport);
		}

		return doc;
	}

	private FileTestReportType checkExpressions(Document doc, OutputType out,
			String path) {
		if (out == null) {
			return null;
		}
		FileTestReportType ft = null;
		for (FileType f : out.getFile()) {
			if (f.getPath().equals(path)) {
				if (ft == null && f.getXpath().size() > 0) {
					ft = new FileTestReportType();
					ft.setPath(path);
				}
				for (XpathType xpath : f.getXpath()) {
					ft.getXpath().add(checkExpression(doc, xpath.getExpr()));
				}
			}
		}
		return ft;
	}

	private XpathReportType checkExpression(Document doc, String xpathstring) {
		XpathReportType report = new XpathReportType();
		report.setExpr(xpathstring);
		XPathExpression x = null;
		try {
			x = xpath.compile(xpathstring);
		} catch (XPathExpressionException e) {
			report.setError(e.getMessage());
			report.setResult(XpathResultType.INVALID);
			return report;
		}
		Object o = null;
		try {
			o = x.evaluate(doc);
		} catch (XPathExpressionException e) {
			report.setError(e.getMessage());
			report.setResult(XpathResultType.INVALID);
			return report;
		}
		if ("true".equals(o)) {
			report.setResult(XpathResultType.TRUE);
		} else if ("false".equals(o)) {
			report.setResult(XpathResultType.FALSE);
		} else {
			report.setError("Result of XPath is not boolean but " + o);
			report.setResult(XpathResultType.INVALID);
		}
		return report;
	}

	private void checkVersion(OdfData data, Document doc,
			ValidationReportType report, String namespace, String filename) {
		String version = null;
		if (!doc.getDocumentElement().hasAttributeNS(namespace, "version")) {
			if (!namespace.equals(InputCreator.manifestns)) {
				report(report, ValidationErrorTypeType.MISSINGVERSIONNUMBER,
						filename);
			}
		} else {
			version = doc.getDocumentElement().getAttributeNS(namespace,
					"version");
		}
		if (version != null) {
			if (!"1.2".equals(version) && !"1.1".equals(version)
					&& !"1.0".equals(version)) {
				report(report, ValidationErrorTypeType.INVALIDVERSIONNUMBER,
						version);
			}
			if (data.version != null && !data.version.equals(version)) {
				report(report,
						ValidationErrorTypeType.INCONSISTENTVERSIONNUMBER,
						version + " != " + data.version);
			} else {
				data.version = version;
			}
		}
	}

	private void checkXml(InputStream in, String version,
			OutputReportType report, ValidationErrorTypeType error) {
		ValidationDriver driver = null;
		if ("1.2".equals(version)) {
			driver = odf12Validator;
		} else if ("1.1".equals(version)) {
			driver = odf11Validator;
		} else if ("1.0".equals(version)) {
		}
		if (driver != null) {
			validateRelaxNG(driver, in, error, report.getValidation());
		}
	}

	private void checkManifestXml(InputStream in, String version,
			ValidationReportType report, ValidationErrorTypeType error) {
		ValidationDriver driver = null;
		if ("1.2".equals(version)) {
			driver = odf12manifestValidator;
		} else if ("1.1".equals(version)) {
			driver = odf11manifestValidator;
		} else if ("1.0".equals(version)) {
		}
		if (driver != null) {
			validateRelaxNG(driver, in, error, report);
		}
	}

	private void checkManifestXml(ZipFile zip, ValidationReportType report,
			OdfData data) throws IOException {
		ZipEntry ze = zip.getEntry("META-INF/manifest.xml");
		if (ze == null) {
			report(report, ValidationErrorTypeType.MISSINGMANIFESTXML);
			return;
		}
		Document manifest = null;
		try {
			manifest = documentBuilder.parse(zip.getInputStream(ze));
		} catch (Exception e) {
			report(report, ValidationErrorTypeType.INVALIDMANIFESTXML,
					e.getMessage());
			return;
		}
		checkVersion(data, manifest, report, InputCreator.manifestns,
				"META-INF/manifest.xml");
		checkManifestXml(zip.getInputStream(ze), data.version, report,
				ValidationErrorTypeType.INVALIDMANIFESTXML);
		Node n = manifest.getDocumentElement().getFirstChild();
		while (n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE
					&& n.getNamespaceURI().equals(InputCreator.manifestns)
					&& "file-entry".equals(n.getLocalName())) {
				String path = n.getAttributes()
						.getNamedItemNS(InputCreator.manifestns, "full-path")
						.getNodeValue();
				if (path != null && !"/".equals(path) && !path.endsWith("/")) {
					ZipEntry member = zip.getEntry(path);
					if (member == null) {
						report(report, ValidationErrorTypeType.MISSINGFILE,
								path);
					}
					data.entries.add(path);
				}
			}
			n = n.getNextSibling();
		}
	}

	private void validateRelaxNG(ValidationDriver driver, InputStream in,
			ValidationErrorTypeType errorIfInvalid, ValidationReportType report) {
		errorbuffer.reset();
		InputSource source = new InputSource(in);
		try {
			if (!driver.validate(source)) {
				report(report, errorIfInvalid, errorbuffer.toString());
			}
		} catch (Exception e) {
			report(report, errorIfInvalid,
					errorbuffer.toString() + e.getMessage());
		}
	}

	private void checkMimetypeFile(String odfpath, ValidationReportType report) {
		byte b[] = new byte[73];
		int n;
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(odfpath));
			in.mark(128);
			n = in.read(b);
		} catch (IOException e) {
			report(report, ValidationErrorTypeType.INVALIDMIMETYPE);
			return;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (n < 73) {
			report(report, ValidationErrorTypeType.INVALIDZIPFILE);
			return;
		}
		final String mimetype = "mimetypeapplication/vnd.oasis.opendocument.";
		for (int i = 30; i < 73; ++i) {
			if (b[i] != mimetype.codePointAt(i - 30)) {
				report(report, ValidationErrorTypeType.INVALIDMIMETYPE);
				return;
			}
		}
	}
}
