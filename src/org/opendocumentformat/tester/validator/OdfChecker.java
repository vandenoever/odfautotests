package org.opendocumentformat.tester.validator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.eclipse.jdt.annotation.Nullable;
import org.example.documenttests.FileTestReportType;
import org.example.documenttests.FileType;
import org.example.documenttests.FragmentType;
import org.example.documenttests.OutputReportType;
import org.example.documenttests.OutputType;
import org.example.documenttests.SimpleResultType;
import org.example.documenttests.ValidationErrorType;
import org.example.documenttests.ValidationErrorTypeType;
import org.example.documenttests.ValidationReportType;
import org.example.documenttests.XpathReportType;
import org.example.documenttests.XpathType;
import org.opendocumentformat.tester.InputCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class OdfChecker {

	private final ValidationDriver odf10Validator;
	private final ValidationDriver odf10manifestValidator;
	private final ValidationDriver odf11Validator;
	private final ValidationDriver odf11manifestValidator;
	private final ValidationDriver odf11strictValidator;
	private final ValidationDriver odf12Validator;
	private final ValidationDriver odf12manifestValidator;
	private final ValidationDriver odf12dsigValidator;
	private final ErrorHandler errorhandler;
	private final DocumentBuilder documentBuilder;
	private final XPathFactory factory = XPathFactory.newInstance();
	private final XPath xpath = factory.newXPath();
	private final Transformer foreignRemover = createForeignRemover();

	class ErrorHandler extends ErrorHandlerImpl {
		final List<SAXParseException> errors = new LinkedList<SAXParseException>();

		ErrorHandler() {
			super();
		}

		@Override
		public void error(@Nullable SAXParseException e) {
			errors.add(e);
		}

		@Override
		public void fatalError(@Nullable SAXParseException e)
				throws SAXParseException {
			errors.add(e);
		}

		public void reset() {
			errors.clear();
		}
	}

	private class OdfData {
		public @Nullable String version;
		public Set<String> entries = new HashSet<String>();
	}

	static private void extract(String target, String path) {
		InputStream i = OdfChecker.class.getClassLoader().getResourceAsStream(
				path);
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

	static private ValidationDriver createValidationDriver(String tmpdir,
			String path, ErrorHandler errorhandler, boolean extendedODF,
			@Nullable ValidationDriver parent) {
		File rng = new File(tmpdir + File.separator + path);
		if (!rng.exists()) {
			extract(rng.getPath(), path);
		}
		String rngpath = rng.getPath();

		SchemaReader schemaReader = SAXSchemaReader.getInstance();
		PropertyMapBuilder properties = new PropertyMapBuilder();
		properties.put(ValidateProperty.ERROR_HANDLER, errorhandler);
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
			throw new Error(e);
		}
		return driver;
	}

	public OdfChecker(boolean extendedODF) {
		errorhandler = new ErrorHandler();
		String tmpdir = "rng";
		(new File(tmpdir)).mkdir();

		odf10Validator = createValidationDriver(tmpdir,
				"OpenDocument-schema-v1.0-os.rng", errorhandler, extendedODF,
				null);
		odf10manifestValidator = createValidationDriver(tmpdir,
				"OpenDocument-manifest-schema-v1.0-os.rng", errorhandler,
				extendedODF, null);
		odf11Validator = createValidationDriver(tmpdir,
				"OpenDocument-schema-v1.1.rng", errorhandler, extendedODF, null);
		odf11strictValidator = createValidationDriver(tmpdir,
				"OpenDocument-strict-schema-v1.1.rng", errorhandler,
				extendedODF, odf11Validator);
		odf11manifestValidator = createValidationDriver(tmpdir,
				"OpenDocument-manifest-schema-v1.1.rng", errorhandler,
				extendedODF, null);
		odf12manifestValidator = createValidationDriver(tmpdir,
				"OpenDocument-v1.2-os-manifest-schema.rng", errorhandler,
				extendedODF, null);
		odf12dsigValidator = createValidationDriver(tmpdir,
				"OpenDocument-v1.2-os-dsig-schema.rng", errorhandler,
				extendedODF, null);
		odf12Validator = createValidationDriver(tmpdir,
				"OpenDocument-v1.2-os-schema.rng", errorhandler, extendedODF,
				odf12dsigValidator);

		documentBuilder = createDocumentBuilder();

		XPathFunctionResolver resolver = new XPathFunctions();
		xpath.setXPathFunctionResolver(resolver);
	}

	private class NSMapper implements NamespaceContext {
		Map<String, String> nsmap;

		NSMapper(@Nullable Map<String, String> map) {
			nsmap = (map == null) ? new HashMap<String, String>() : map;
		}

		public @Nullable String getNamespaceURI(@Nullable String prefix) {
			return nsmap.get(prefix);
		}

		public @Nullable String getPrefix(@Nullable String nsuri) {
			for (Entry<String, String> e : nsmap.entrySet()) {
				if (e.getValue().equals(nsuri)) {
					return e.getKey();
				}
			}
			return null;
		}

		public Iterator<String> getPrefixes(@Nullable String nsuri) {
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

		} catch (ParserConfigurationException e) {
			throw new Error(e);
		}
		return builder;
	}

	static public void report(OutputReportType report,
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
			ValidationErrorTypeType type, List<SAXParseException> msgs) {
		for (SAXParseException e : msgs) {
			ValidationErrorType error = new ValidationErrorType();
			error.setType(type);
			error.setLineNumber(e.getLineNumber());
			error.setColumnNumber(e.getColumnNumber());
			error.setMessage(e.getMessage());
			report.getError().add(error);
		}
	}

	static private void report(ValidationReportType report,
			ValidationErrorTypeType type) {
		report(report, type, "");
	}

	public void check(File odfpath, OutputReportType report,
			@Nullable OutputType out, @Nullable Map<String, String> nsmap) {
		xpath.setNamespaceContext(new NSMapper(nsmap));

		report.setFragment(new FragmentType());

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
			OdfData data, @Nullable OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDSTYLESXML,
				ValidationErrorTypeType.MISSINGSTYLESXML, "styles.xml", out);
	}

	private void checkContentXml(ZipFile zip, OutputReportType report,
			OdfData data, @Nullable OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDCONTENTXML,
				ValidationErrorTypeType.MISSINGCONTENTXML, "content.xml", out);
	}

	private void checkMetaXml(ZipFile zip, OutputReportType report,
			OdfData data, @Nullable OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDMETAXML,
				ValidationErrorTypeType.MISSINGMETAXML, "meta.xml", out);
	}

	private void checkSettingsXml(ZipFile zip, OutputReportType report,
			OdfData data, @Nullable OutputType out) throws IOException {
		checkXml(zip, report, data, ValidationErrorTypeType.INVALIDSETTINGSXML,
				ValidationErrorTypeType.MISSINGSETTINGSXML, "settings.xml", out);
	}

	private @Nullable Element getChild(String localname, @Nullable Element e) {
		Node n = (e == null) ? null : e.getFirstChild();
		while (n != null) {
			if (n instanceof Element) {
				e = (Element) n;
				if (e.getLocalName().equals(localname)) {
					return e;
				} else {
					e = getChild(localname, e);
					if (e != null) {
						return e;
					}
				}
			}
			n = n.getNextSibling();
		}
		return null;
	}

	private @Nullable Document checkXml(ZipFile zip, OutputReportType report,
			OdfData data, ValidationErrorTypeType invalid,
			ValidationErrorTypeType missing, String path,
			@Nullable OutputType out) throws IOException {
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
		if (path.equals("content.xml")) {
			Element e = getChild("text", doc.getDocumentElement());
			if (e != null) {
				report.getFragment().getAny().add(e);
			}
		}

		FileTestReportType filereport = checkExpressions(doc, out, path);
		if (filereport != null) {
			report.getFile().add(filereport);
		}

		return doc;
	}

	private @Nullable FileTestReportType checkExpressions(Document doc,
			@Nullable OutputType out, String path) {
		if (out == null) {
			return null;
		}
		FileTestReportType ft = null;
		for (FileType f : out.getFile()) {
			if (f.getPath().equals(path)) {
				if (ft == null) {
					if (f.getXpath().size() == 0) {
						// TODO: handle error
					}
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
			report.setResult(SimpleResultType.INVALID);
			return report;
		}
		Object o = null;
		try {
			o = x.evaluate(doc);
		} catch (XPathExpressionException e) {
			report.setError(e.getMessage());
			report.setResult(SimpleResultType.INVALID);
			return report;
		}
		if ("true".equals(o)) {
			report.setResult(SimpleResultType.TRUE);
		} else if ("false".equals(o)) {
			report.setResult(SimpleResultType.FALSE);
		} else {
			report.setError("Result of XPath is not boolean but " + o);
			report.setResult(SimpleResultType.INVALID);
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

	private void checkXml(InputStream in, @Nullable String version,
			OutputReportType report, ValidationErrorTypeType error)
			throws IOException {
		ValidationDriver driver = null;
		if ("1.2".equals(version)) {
			driver = odf12Validator;
		} else if ("1.1".equals(version)) {
			driver = odf11strictValidator;
		} else if ("1.0".equals(version)) {
			driver = odf10Validator;
		}
		if (driver != null) {
			// create a stream where the foreign elements and attributes are
			// removed
			in = removeForeign(in);
			InputSource source = new InputSource(in);
			validateRelaxNG(driver, source, error, report.getValidation());
		}
	}

	private void checkManifestXml(InputStream in, @Nullable String version,
			ValidationReportType report, ValidationErrorTypeType error)
			throws IOException {
		ValidationDriver driver = null;
		if ("1.2".equals(version)) {
			driver = odf12manifestValidator;
		} else if ("1.1".equals(version)) {
			driver = odf11manifestValidator;
		} else if ("1.0".equals(version)) {
			driver = odf10manifestValidator;
		}
		if (driver != null) {
			// create a stream where the foreign elements and attributes are
			// removed
			in = removeForeign(in);
			InputSource source = new InputSource(in);
			validateRelaxNG(driver, source, error, report);
		}
	}

	Transformer createForeignRemover() {
		Source source;
		File f = new File("removeForeign.xsl");
		if (!f.exists()) {
			source = new StreamSource(OdfChecker.class.getClassLoader()
					.getResourceAsStream("removeForeign.xsl"));
		} else {
			source = new StreamSource(f);
		}
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer trans = null;
		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			trans = factory.newTransformer(source);
		} catch (Exception e) {
			throw new Error(e);
		}
		// abiwords manifest.xml needs Manifest.dtd
		// we create one if there is none yet
		File abiwordManifest = new File("Manifest.dtd");
		if (!abiwordManifest.exists()) {
			try {
				abiwordManifest.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return trans;
	}

	InputStream removeForeign(InputStream in) throws IOException {
		Source source = new StreamSource(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamResult stream = new StreamResult(out);
		try {
			foreignRemover.transform(source, stream);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		out.close();
		return new ByteArrayInputStream(out.toByteArray());
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

	private void validateRelaxNG(ValidationDriver driver, InputSource source,
			ValidationErrorTypeType errorIfInvalid, ValidationReportType report) {
		errorhandler.reset();
		try {
			driver.validate(source);
		} catch (Exception e) {
			report(report, errorIfInvalid, e.getMessage());
		}
		report(report, errorIfInvalid, errorhandler.errors);
	}

	private void checkMimetypeFile(File odfpath, ValidationReportType report) {
		byte b[] = new byte[73];
		int n;
		InputStream in = null;
		try {
			in = new FileInputStream(odfpath);
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

class XPathFunctions implements XPathFunctionResolver {
	final XPathFunction compareLength = new CompareLengthFunction();
	final XPathFunction item = new ItemFunction();

	@Override
	public @Nullable XPathFunction resolveFunction(@Nullable QName qname,
			int nargs) {
		if ((nargs == 2 || nargs == 3)
				&& qname != null
				&& qname.getNamespaceURI().equals(
						"http://www.example.org/documenttests")
				&& qname.getLocalPart().equals("compareLength")) {

			return compareLength;
		}
		if ((nargs == 2 || nargs == 3)
				&& qname != null
				&& qname.getNamespaceURI().equals(
						"http://www.example.org/documenttests")
				&& qname.getLocalPart().equals("item")) {

			return item;
		}
		return null;
	}

}

class CompareLengthFunction implements XPathFunction {

	public static @Nullable String getString(Object o) {
		if (o instanceof String) {
			return (String) o;
		}
		if (o instanceof NodeList) {
			NodeList nodeList = (NodeList) o;
			if (nodeList.getLength() == 1) {
				return nodeList.item(0).getTextContent();
			}
		}
		return null;
	}

	@Override
	public Object evaluate(@SuppressWarnings("rawtypes") @Nullable List args)
			throws XPathFunctionException {
		if (args == null || args.size() < 2 || args.size() > 3) {
			throw new XPathFunctionException("two or three arguments needed");
		}

		String length = getString(args.get(0));
		String reference = getString(args.get(1));
		String tolerance = null;
		if (args.size() == 3) {
			tolerance = getString(args.get(2));
		}
		if (length == null || reference == null) {
			return false;
		}
		boolean r = false;
		try {
			r = evaluate(length, reference, tolerance);
		} catch (XPathFunctionException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	final Pattern regexp = Pattern
			.compile("(-?(?:[0-9]+(?:\\.[0-9]*)?)|(?:\\.[0-9]+))(cm|mm|in|pt|pc|px)");

	private double convertToPx(String length) throws XPathFunctionException {
		Matcher matcher = regexp.matcher(length);
		if (!matcher.matches()) {
			throw new XPathFunctionException(length + " is not a valid length");
		}
		double value = Double.parseDouble(matcher.group(1));
		String unit = matcher.group(2);
		if ("cm".equals(unit)) {
			value = value * 96 / 2.54;
		} else if ("mm".equals(unit)) {
			value = value * 96 / 25.4;
		} else if ("in".equals(unit)) {
			value = value * 96;
		} else if ("pt".equals(unit)) {
			value = value / 0.75;
		} else if ("pc".equals(unit)) {
			value = value * 16;
		} else if (!"px".equals(unit)) {
			throw new XPathFunctionException("Invalid unit " + unit);
		}
		return value;
	}

	private boolean evaluate(String length, String reference,
			@Nullable String tolerance) throws XPathFunctionException {
		double lengthPx = convertToPx(length);
		double referencePx = convertToPx(reference);
		double tolerancePx = referencePx * 0.03; // default allows 3% error
		if (tolerance != null) {
			tolerancePx = convertToPx(tolerance);
		}
		return Math.abs(lengthPx - referencePx) <= Math.abs(tolerancePx);
	}
}

class ItemFunction implements XPathFunction {
	@Override
	public @Nullable Object evaluate(
			@SuppressWarnings("rawtypes") @Nullable List args)
			throws XPathFunctionException {

		if (args == null || args.size() < 2 || args.size() > 3) {
			throw new XPathFunctionException("two or three arguments needed");
		}

		String input = CompareLengthFunction.getString(args.get(0));
		if (input == null) {
			return null;
		}
		int index = ((Double) args.get(1)).intValue();
		String delimiter = CompareLengthFunction.getString(args.get(2));
		return input.split(delimiter)[index];
	}
}
