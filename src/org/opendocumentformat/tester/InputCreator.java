package org.opendocumentformat.tester;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.example.documenttests.FragmentType;
import org.example.documenttests.OdfTypeType;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class InputCreator {

	final OdfTypeType type;

	public enum ODFVersion {
		v1_0 {
			public String toString() {
				return "1.0";
			}
		},
		v1_1 {
			public String toString() {
				return "1.1";
			}
		},
		v1_2 {
			public String toString() {
				return "1.2";
			}
		}
	}

	final ODFVersion version;

	Document content;
	Document styles;
	Document meta;
	Document settings;
	Document manifest;

	Element documentStylesElement;
	Element stylesElement;
	Element stylesAutomaticStylesElement;
	Element masterStylesElement;

	Element documentContentElement;
	Element contentAutomaticStylesElement;
	Element bodyElement;
	Element bodyChildElement;

	final Transformer xformer;

	final DOMImplementation domimplementation;

	public final static String officens = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";
	public final static String stylens = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
	public final static String manifestns = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0";

	InputCreator(OdfTypeType type, ODFVersion version) {
		this.type = type;
		this.version = version;
		Transformer xformer = null;
		try {
			String stylesheet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
					+ "<xsl:output method=\"xml\" version=\"1.0\" indent=\"no\"/>"
					+ "<xsl:template match=\"*\">"
					+ "<xsl:element name=\"{local-name()}\">"
					+ "<xsl:for-each select=\"@*\">"
					+ "<xsl:attribute name=\"{local-name()}\">"
					+ "<xsl:value-of select=\".\"/>" + "</xsl:attribute>"
					+ "</xsl:for-each>" + "<xsl:apply-templates/>"
					+ "</xsl:element>" + "</xsl:template>"
					+ "</xsl:stylesheet>";

			StreamSource xslSource = new StreamSource(new StringReader(
					stylesheet));
			TransformerFactory tf = TransformerFactory.newInstance();
			xformer = tf.newTransformer(xslSource);

			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			xformer.setOutputProperty(OutputKeys.METHOD, "xml");
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.xformer = xformer;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		domimplementation = builder.getDOMImplementation();
	}

	private void createNewDocument() {
		createNewContent();
		createNewStyles();
		manifest = createNewManifest();
		meta = createNewMeta();
		settings = createNewSettings();
	}

	private void createNewContent() {
		content = domimplementation.createDocument(officens,
				"document-content", null);
		content.setXmlStandalone(true);
		documentContentElement = content.getDocumentElement();
		setAttribute(documentContentElement, "office", officens, "version",
				version.toString());
		contentAutomaticStylesElement = content.createElementNS(officens,
				"automatic-styles");
		documentContentElement.appendChild(contentAutomaticStylesElement);
		bodyElement = content.createElementNS(officens, "body");
		documentContentElement.appendChild(bodyElement);
		bodyChildElement = content.createElementNS(officens, type.toString()
				.toLowerCase());
		bodyElement.appendChild(bodyChildElement);
	}

	private void createNewStyles() {
		styles = domimplementation.createDocument(officens, "document-styles",
				null);
		styles.setXmlStandalone(true);
		documentStylesElement = styles.getDocumentElement();
		setAttribute(documentStylesElement, "office", officens, "version",
				version.toString());
		stylesAutomaticStylesElement = styles.createElementNS(officens,
				"automatic-styles");
		documentStylesElement.appendChild(stylesAutomaticStylesElement);
		masterStylesElement = styles.createElementNS(officens, "master-styles");
		documentStylesElement.appendChild(masterStylesElement);
	}

	private Document createNewManifest() {
		Document doc = domimplementation.createDocument(manifestns, "manifest",
				null);
		doc.setXmlStandalone(true);
		Element e = doc.getDocumentElement();
		e.setPrefix("manifest");
		if (version == ODFVersion.v1_2) {
			setAttribute(e, "manifest", manifestns, "version",
					version.toString());
		}
		addFileEntry(doc, "application/vnd.oasis.opendocument."
				+ type.toString().toLowerCase(), "/");
		addFileEntry(doc, "text/xml", "content.xml");
		addFileEntry(doc, "text/xml", "styles.xml");
		addFileEntry(doc, "text/xml", "meta.xml");
		addFileEntry(doc, "text/xml", "settings.xml");
		return doc;
	}

	private static void addFileEntry(Document doc, String mediatype,
			String fullpath) {
		Element fileentry = doc.createElementNS(manifestns, "file-entry");
		fileentry.setPrefix("manifest");
		setAttribute(fileentry, "manifest", manifestns, "media-type", mediatype);
		setAttribute(fileentry, "manifest", manifestns, "full-path", fullpath);
		doc.getDocumentElement().appendChild(fileentry);
	}

	private Document createNewMeta() {
		Document doc = domimplementation.createDocument(officens,
				"document-meta", null);
		doc.setXmlStandalone(true);
		Element e = doc.getDocumentElement();
		setAttribute(e, "office", officens, "version", version.toString());
		return doc;
	}

	private static void setAttribute(Element e, String prefix, String ns,
			String name, String value) {
		Attr a = e.getOwnerDocument().createAttributeNS(ns, name);
		a.setPrefix(prefix);
		a.setValue(value);
		e.setAttributeNode(a);
	}

	private Document createNewSettings() {
		Document doc = domimplementation.createDocument(officens,
				"document-settings", null);
		doc.setXmlStandalone(true);
		Element e = doc.getDocumentElement();
		setAttribute(e, "office", officens, "version", version.toString());
		return doc;
	}

	private void merge(Element source, Element target) {
		while (source.getFirstChild() != null) {
			target.appendChild(source.getFirstChild());
		}
	}

	private void setStylesPart(Element e) {
		String ns = e.getNamespaceURI();
		String name = e.getLocalName();
		e = (Element) styles.importNode(e, true);
		if (ns.equals(officens)) {
			if (name.equals("automatic-styles")) {
				merge(e, stylesAutomaticStylesElement);
			} else if (name.equals("master-styles")) {
				merge(e, masterStylesElement);
			}
		}
	}

	private boolean setContentPart(Element e) {
		String ns = e.getNamespaceURI();
		String name = e.getLocalName();
		e = (Element) content.importNode(e, true);
		if (ns.equals(officens)) {
			if (name.equals(type.toString().toLowerCase())) {
				bodyChildElement.getParentNode().replaceChild(e,
						bodyChildElement);
				return true;
			} else if (name.equals("automatic-styles")) {
				merge(e, contentAutomaticStylesElement);
				return true;
			}
		}
		return false;
	}

	private void setDocumentPart(Element e) {
		String ns = e.getNamespaceURI();
		String name = e.getLocalName();
		if (ns.equals(officens)) {
			if (name.equals("document-content")) {
				Node n = e.getFirstChild();
				while (n != null) {
					if (n instanceof Element) {
						setContentPart((Element) n);
					}
					n = n.getNextSibling();
				}
			} else if (name.equals("document-styles")) {
				Node n = e.getFirstChild();
				while (n != null) {
					if (n instanceof Element) {
						setStylesPart((Element) n);
					}
					n = n.getNextSibling();
				}
			} else {
				if (!setContentPart(e)) {
					setStylesPart(e);
				}
			}
		} else {
			System.err.println("No support for setting element {"
					+ e.getNamespaceURI() + "}" + e.getLocalName());
		}
	}

	private void createDocument(FragmentType input) {
		createNewDocument();
		for (Object o : input.getAny()) {
			Element e = (Element) o;
			setDocumentPart(e);
		}
		NamespaceCleaner nc = new NamespaceCleaner();
		nc.cleanNamespaces(content);
		nc.cleanNamespaces(styles);
	}

	String createInput(FragmentType input) {
		createDocument(input);

		String filename = null;
		try {
			File f = File.createTempFile("input", ".odt", new File("tmp"));
			filename = f.getPath();
			FileOutputStream fos = new FileOutputStream(f);
			ZipOutputStream zos = new ZipOutputStream(fos);
			zos.setMethod(ZipOutputStream.STORED);
			addEntry(zos, "mimetype", "application/vnd.oasis.opendocument."
					+ type.toString().toLowerCase());
			zos.setMethod(ZipOutputStream.DEFLATED);
			addEntry(zos, "META-INF/manifest.xml", manifest);
			addEntry(zos, "content.xml", content);
			addEntry(zos, "styles.xml", styles);
			addEntry(zos, "meta.xml", meta);
			addEntry(zos, "settings.xml", settings);
			zos.flush();
			fos.flush();
			fos.getChannel().force(true);
			fos.getFD().sync();
			zos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filename;
	}

	private long calculateCRC(byte b[]) {
		CRC32 crc = new CRC32();
		crc.update(b);
		return crc.getValue();
	}

	private void addEntry(ZipOutputStream zos, String path, String content)
			throws IOException {
		ZipEntry ze = new ZipEntry(path);
		byte[] b = content.getBytes("UTF-8");
		ze.setSize(b.length);
		ze.setCompressedSize(b.length);
		ze.setCrc(calculateCRC(b));
		zos.putNextEntry(ze);
		zos.write(b);
		zos.closeEntry();
	}

	private void addEntry(ZipOutputStream zos, String path, Document content)
			throws IOException {
		ZipEntry ze = new ZipEntry(path);
		zos.putNextEntry(ze);
		Result result = new StreamResult(zos);
		try {
			xformer.transform(new DOMSource(content), result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		zos.closeEntry();
	}
}
