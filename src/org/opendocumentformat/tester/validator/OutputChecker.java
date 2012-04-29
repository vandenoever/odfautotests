package org.opendocumentformat.tester.validator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.example.documenttests.ValidationErrorType;
import org.example.documenttests.ValidationErrorTypeType;
import org.example.documenttests.ValidationReportType;
import org.opendocumentformat.tester.InputCreator;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.Validator;
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

	}

	static Validator loadValidator(String path) {
		SchemaReader schemaReader = SAXSchemaReader.getInstance();
		Schema schema;
		ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
		PropertyMapBuilder properties = new PropertyMapBuilder();
		properties.put(ValidateProperty.ERROR_HANDLER, eh);
		RngProperty.CHECK_ID_IDREF.add(properties);
		properties.put(RngProperty.CHECK_ID_IDREF, null);
		try {
			schema = schemaReader.createSchema(
					ValidationDriver.fileInputSource(new File(path)),
					properties.toPropertyMap());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// can use different error handler here (try DraconianErrorHandler
		// http://www.thaiopensource.com/relaxng/api/jing/com/thaiopensource/xml/sax/DraconianErrorHandler.html)
		PropertyMapBuilder builder = new PropertyMapBuilder();
		ErrorHandler seh = new ErrorHandlerImpl();
		builder.put(ValidateProperty.ERROR_HANDLER, seh);

		// Validator is NOT thread safe
		return schema.createValidator(builder.toPropertyMap());
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

		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			factory.setCoalescing(false);
			factory.setValidating(false);
			factory.setExpandEntityReferences(true);
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		documentBuilder = builder;
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

	public ValidationReportType check(String odfpath) {
		ValidationReportType report = new ValidationReportType();

		checkMimetypeFile(odfpath, report);

		Set<String> entrynames = new HashSet<String>();
		OdfData data = new OdfData();
		try {
			ZipFile zip = new ZipFile(odfpath);
			checkStylesXml(zip, report, data);
			checkManifestXml(zip, report);
			/*
			 * Enumeration<?> entries = zip.entries(); while
			 * (entries.hasMoreElements()) { ZipEntry ze = (ZipEntry)
			 * entries.nextElement(); if (entrynames.contains(ze.getName())) {
			 * System.err.println(pos + " double entry:'" + ze.getName() + "'");
			 * report(report, ValidationErrorTypeType.DOUBLEZIPENTRY); }
			 * entrynames.add(ze.getName()); version = checkEntry(ze.getName(),
			 * zip.getInputStream(ze), version, report); pos++; }
			 */
		} catch (IOException e) {
			report(report, ValidationErrorTypeType.INVALIDZIPFILE,
					e.getMessage());
			return report;
		}
		return report;
	}

	private void checkManifestXml(ZipFile zip, ValidationReportType report) {

	}

	private String checkEntry(String name, InputStream in, String version,
			ValidationReportType report) {
		String fileversion = null;
		if (name.equals("content.xml")) {
			fileversion = checkContentXml(in, version, report);
		}
		/*
		 * if (name.equals("meta.xml")) { fileversion = checkMetaXml(in,
		 * version, report); } if (name.equals("settings.xml")) { fileversion =
		 * checkSettingsXml(in, version, report); }
		 */
		if (version == null) {
			fileversion = findVersion(in);
			if (name.equals("content.xml") || name.equals("styles.xml")
					|| name.equals("meta.xml") || name.equals("settings.xml")) {
				if (fileversion == null) {
					report(report, ValidationErrorTypeType.MISSINGVERSIONNUMBER);
					return version;
				}
			}
		}
		return fileversion;
	}

	private String checkContentXml(ZipFile zip, ValidationReportType report) {
		return "1.2";
	}

	private String checkContentXml(InputStream in, String version,
			ValidationReportType report) {
		validateRelaxNG(odf12Validator, in,
				ValidationErrorTypeType.INVALIDCONTENTXML, report);
		return "1.2";
	}

	private void checkStylesXml(ZipFile zip, ValidationReportType report,
			OdfData data) throws IOException {
		ZipEntry ze = zip.getEntry("styles.xml");
		if (ze == null) {
			report(report, ValidationErrorTypeType.MISSINGSTYLESXML);
			return;
		}
		Document styles = null;
		try {
			styles = documentBuilder.parse(zip.getInputStream(ze));
		} catch (SAXException e) {
			report(report, ValidationErrorTypeType.INVALIDSTYLESXML,
					e.getMessage());
			System.err.println("STYLESXML: " + e.getMessage());
			return;
		}
		if (!styles.getDocumentElement().hasAttributeNS(InputCreator.officens,
				"version")) {
			report(report, ValidationErrorTypeType.MISSINGVERSIONNUMBER,
					"styles.xml");
		} else {
			data.version = styles.getDocumentElement().getAttributeNS(
					InputCreator.officens, "version");
			if (!data.version.equals("1.2") && !data.version.equals("1.1")
					&& !data.version.equals("1.0")) {
				report(report, ValidationErrorTypeType.INVALIDVERSIONNUMBER,
						data.version);
			}
		}
		checkXml(zip.getInputStream(ze), data.version, report,
				ValidationErrorTypeType.INVALIDSTYLESXML);
	}

	private void checkXml(InputStream in, String version,
			ValidationReportType report, ValidationErrorTypeType error) {
		ValidationDriver driver = null;
		if ("1.2".equals(version)) {
			driver = odf12Validator;
		} else if ("1.1".equals(version)) {
			driver = odf11Validator;
		} else if ("1.0".equals(version)) {
		}
		if (driver != null) {
			validateRelaxNG(driver, in, error, report);
		}
	}

	private String checkManifestXml(InputStream in, String version,
			ValidationReportType report) {
		validateRelaxNG(odf12Validator, in,
				ValidationErrorTypeType.INVALIDSTYLESXML, report);
		return "1.2";
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
			report(report, errorIfInvalid, errorbuffer.toString());
		}
	}

	private String findVersion(InputStream in) {
		return "1.2";
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
