package org.opendocumentformat.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class SpecWriter {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException,
			ParserConfigurationException, SAXException {
		SpecWriter writer = new SpecWriter();
		writer.writeAll();
	}

	SpecWriter() {

	}

	final static String htmlns = "http://www.w3.org/1999/xhtml";
	final static String officens = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";
	final static String textns = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";

	Document createHTMLDocument() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.setXmlStandalone(true);
		Element root = doc.createElementNS(htmlns, "html");
		doc.appendChild(root);
		return doc;
	}

	void write(Document doc, String path) throws FileNotFoundException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException {
		File f = new File(path);
		FileOutputStream fos = new FileOutputStream(f);
		DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
		DOMImplementationLS impl = (DOMImplementationLS) reg
				.getDOMImplementation("LS");
		LSSerializer serializer = impl.createLSSerializer();
		LSOutput lso = impl.createLSOutput();
		lso.setByteStream(fos);
		serializer.write(doc, lso);
	}

	Document readContent(String path) throws IOException,
			ParserConfigurationException, SAXException {
		ZipFile z = new ZipFile(path);
		InputStream i = z.getInputStream(z.getEntry("content.xml"));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document d = builder.parse(i);
		z.close();
		return d;
	}

	@Nullable
	Element getChild(Element e, String ns, String name) {
		Node n = e.getFirstChild();
		while (n != null
				&& !(ns.equals(n.getNamespaceURI()) && name.equals(n
						.getLocalName()))) {
			n = n.getNextSibling();
		}
		return (n == null) ? null : (Element) n;
	}

	void parse(SpecPart p, Element text) {
		Node n = text.getFirstChild();
		while (n != null) {
			if (textns.equals(n.getNamespaceURI())
					&& "h".equals(n.getLocalName())) {
				int level;
				try {
					level = Integer.parseInt(n.getAttributes()
							.getNamedItemNS(textns, "outline-level")
							.getNodeValue());
				} catch (NumberFormatException e) {
					level = 1;
				}
				level = Math.min(Math.max(1, level), 100);
				while (p.level > level) {
					SpecPart sp = p.parent;
					if (sp == null) {
						throw new Error();
					}
					p = sp;
				}
				while (p.level <= level) {
					p = new SpecPart(p);
				}
			}
			p.frag.appendChild(p.doc.importNode(n, true));
			n = n.getNextSibling();
		}
	}

	void write12() throws IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException,
			ParserConfigurationException, SAXException {
		Document out = createHTMLDocument();
		Document content = readContent("specsplit/v1.2/OpenDocument-v1.2-part1.odt");
		@Nullable
		Element e = getChild(content.getDocumentElement(), officens, "body");
		if (e != null) {
			e = getChild(e, officens, "text");
		}
		if (e == null) {
			throw new IOException("Invalid ODT document.");
		}
		SpecPart p = new SpecPart(out);
		parse(p, e);
		write(out, "1.2.html");
	}

	void writeAll() throws IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException,
			ParserConfigurationException, SAXException {
		write12();
	}
}

class SpecPart {
	Document doc;
	int level;
	DocumentFragment frag;
	@Nullable
	SpecPart parent;
	List<SpecPart> children = new LinkedList<SpecPart>();

	SpecPart(Document doc) {
		this.doc = doc;
		level = 0;
		frag = doc.createDocumentFragment();
		parent = null;
	}

	SpecPart(SpecPart p) {
		this.doc = p.doc;
		level = p.level + 1;
		frag = doc.createDocumentFragment();
		parent = p;
		p.children.add(this);
		System.out.println("YI " + p.level + " " + p.children.size());
	}
}
