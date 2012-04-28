package org.opendocumentformat.tester.validator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.example.documenttests.ValidationErrorType;
import org.example.documenttests.ValidationReportType;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

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

	class ErrorBuffer extends Writer {
		private StringWriter buffer;

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

	static ValidationDriver createValidationDriver(String path,
			ErrorBuffer errorbuffer) {
		ErrorHandlerImpl eh = new ErrorHandlerImpl(errorbuffer);
		SchemaReader schemaReader = SAXSchemaReader.getInstance();
		PropertyMapBuilder properties = new PropertyMapBuilder();
		properties.put(ValidateProperty.ERROR_HANDLER, eh);
		RngProperty.CHECK_ID_IDREF.add(properties);
		properties.put(RngProperty.CHECK_ID_IDREF, null);

		ValidationDriver driver = new ValidationDriver(
				properties.toPropertyMap(), schemaReader);
		InputSource in = ValidationDriver.uriOrFileInputSource(path);
		try {
			if (!driver.loadSchema(in)) {
				System.err.println("Could not load schema " + path + ".");
				driver = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			driver = null;
		}
		return driver;
	}

	public OutputChecker() {
		errorbuffer = new ErrorBuffer();
		String dir = "/home/oever/work/workspace/odfautotests/b/docs.oasis-open.org/office/";
		odf11Validator = createValidationDriver(dir
				+ "v1.1/OS/OpenDocument-schema-v1.1.rng", errorbuffer);
		odf11strictValidator = createValidationDriver(dir
				+ "v1.1/OS/OpenDocument-strict-schema-v1.1.rng", errorbuffer);
		odf11manifestValidator = createValidationDriver(dir
				+ "v1.1/OS/OpenDocument-manifest-schema-v1.1.rng", errorbuffer);
		odf12manifestValidator = createValidationDriver(dir
				+ "v1.2/os/OpenDocument-v1.2-os-manifest-schema.rng",
				errorbuffer);
		odf12dsigValidator = createValidationDriver(dir
				+ "v1.2/os/OpenDocument-v1.2-os-dsig-schema.rng", errorbuffer);

		// Source source = ...//your XML source
		// TransformerFactory.newInstance().newTransformer().transform(source,
		// new SAXResult(validator.getContentHandler()));

		odf12Validator = createValidationDriver(dir
				+ "v1.2/os/OpenDocument-v1.2-os-schema.rng", errorbuffer);

		// odf12Validator = loadValidator(dir
		// + "v1.2/os/OpenDocument-v1.2-os-schema.rng",errorbuffer);
	}

	public ValidationReportType check(String odfpath) {
		ValidationReportType report = new ValidationReportType();

		checkMimetypeFile(odfpath, report);

		Set<String> entrynames = new HashSet<String>();
		String version = null;
		int pos = 0;
		try {
			ZipFile zip = new ZipFile(odfpath);
			Enumeration<?> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) entries.nextElement();
				if (entrynames.contains(ze.getName())) {
					System.err.println(pos + " double entry:'" + ze.getName()
							+ "'");
					report.getError().add(ValidationErrorType.DOUBLEZIPENTRY);
				}
				entrynames.add(ze.getName());
				version = checkEntry(ze.getName(), zip.getInputStream(ze),
						version, report);
				pos++;
			}
		} catch (IOException e) {
			report.getError().add(ValidationErrorType.INVALIDZIPFILE);
			return report;
		}
		return report;
	}

	private String checkEntry(String name, InputStream in, String version,
			ValidationReportType report) {
		if (version == null
				&& (name.equals("content.xml") || name.equals("styles.xml")
						|| name.equals("meta.xml") || name
							.equals("settings.xml"))) {
			version = findVersion(in);
			if (version == null) {
				report.getError().add(ValidationErrorType.MISSINGVERSIONNUMBER);
				return version;
			}
		}
		if (name.equals("content.xml")) {
			checkContentXml(in, version, report);
		}
		if (name.equals("styles.xml")) {
			checkStylesXml(in, version, report);
		}
		return version;
	}

	private void checkContentXml(InputStream in, String version,
			ValidationReportType report) {
		validateRelaxNG(odf12Validator, in, ValidationErrorType.INVALIDCONTENTXML,
				report);
	}

	private void checkStylesXml(InputStream in, String version,
			ValidationReportType report) {
		validateRelaxNG(odf12Validator, in, ValidationErrorType.INVALIDSTYLESXML,
				report);
	}

	private void validateRelaxNG(ValidationDriver driver, InputStream in,
			ValidationErrorType errorIfInvalid, ValidationReportType report) {
		errorbuffer.reset();
		InputSource source = new InputSource(in);
		try {
			if (!driver.validate(source)) {
				report.getError().add(errorIfInvalid);
			}
		} catch (Exception e) {
			report.getError().add(errorIfInvalid);
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
			report.getError().add(ValidationErrorType.INVALIDMIMETYPE);
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
			report.getError().add(ValidationErrorType.INVALIDZIPFILE);
			return;
		}
		final String mimetype = "mimetypeapplication/vnd.oasis.opendocument.";
		for (int i = 30; i < 73; ++i) {
			if (b[i] != mimetype.codePointAt(i - 30)) {
				report.getError().add(ValidationErrorType.INVALIDMIMETYPE);
				return;
			}
		}
	}
}
