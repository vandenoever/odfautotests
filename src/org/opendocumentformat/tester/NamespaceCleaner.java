package org.opendocumentformat.tester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NamespaceCleaner {

	public NamespaceCleaner() {
	}

	private void findPrefixesOnOneNode(Node n, Map<String, String> prefixes) {
		String ns = n.getNamespaceURI();
		if (ns != null && ns.length() > 0) {
			String prefix = prefixes.get(ns);
			if (prefix == null || prefix.length() == 0) {
				prefixes.put(ns, n.getPrefix());
			}
		}
	}

	private final String xmlns = "http://www.w3.org/2000/xmlns/";

	private void findPrefixesOnAttribute(Node n, Map<String, String> prefixes) {
		String ns = n.getNamespaceURI();
		if (ns != null && ns.length() > 0) {
			String prefix = prefixes.get(ns);
			if (prefix == null || prefix.length() == 0) {
				prefixes.put(ns, n.getPrefix());
			}
		}
	}

	private void findPrefixes(Node n, Map<String, String> prefixes) {
		findPrefixesOnOneNode(n, prefixes);
		if (n.getAttributes() != null) {
			NamedNodeMap atts = n.getAttributes();
			int length = atts.getLength();
			for (int i = 0; i < length; ++i) {
				findPrefixesOnAttribute(atts.item(i), prefixes);
			}
		}
		Node c = n.getFirstChild();
		while (c != null) {
			findPrefixes(c, prefixes);
			c = c.getNextSibling();
		}
	}

	private void giveNamespacesUniquePrefixes(Map<String, String> prefixes) {
		Set<String> usedPrefixes = new HashSet<String>();
		for (Entry<String, String> e : prefixes.entrySet()) {
			if (usedPrefixes.contains(e.getValue())) {
				int i = 1;
				String prefix = "ns" + i;
				while (usedPrefixes.contains(prefix)) {
					i += 1;
					prefix = "ns" + i;
				}
				e.setValue(prefix);
			}
			usedPrefixes.add(e.getValue());
		}
	}

	public void cleanNamespaces(Document d) {
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put(xmlns, "xmlns");
		cleanNamespaces(d, prefixes);
	}

	private void removePrefixDeclarations(Element e, List<Attr> list) {
		list.clear();
		NamedNodeMap atts = e.getAttributes();
		int length = atts.getLength();
		for (int i = 0; i < length; ++i) {
			Attr a = (Attr) atts.item(i);
			if (xmlns.equals(a.getNamespaceURI())
					|| "xmlns".equals(a.getPrefix())) {
				list.add(a);
			}
		}
		for (Attr a : list) {
			e.removeAttributeNode(a);
		}
		Node c = e.getFirstChild();
		while (c != null) {
			if (c instanceof Element) {
				removePrefixDeclarations((Element) c, list);
			}
			c = c.getNextSibling();
		}
	}

	private void cleanNamespaces(Document d, Map<String, String> prefixes) {
		prefixes = new HashMap<String, String>(prefixes);
		findPrefixes(d, prefixes);
		giveNamespacesUniquePrefixes(prefixes);

		Element root = d.getDocumentElement();
		List<Attr> list = new ArrayList<Attr>();
		removePrefixDeclarations(root, list);
		for (Entry<String, String> e : prefixes.entrySet()) {
			if (!xmlns.equals(e.getKey())) {
				root.setAttributeNS(xmlns, "xmlns:" + e.getValue(), e.getKey());
			}	
		}
		cleanNamespaces(root, prefixes);
	}

	private void cleanNamespaces(Element e, Map<String, String> prefixes) {
		e.setPrefix(prefixes.get(e.getNamespaceURI()));
		Node c = e.getFirstChild();
		while (c != null) {
			if (c instanceof Element) {
				cleanNamespaces((Element) c, prefixes);
			}
			c = c.getNextSibling();
		}
		NamedNodeMap atts = e.getAttributes();
		int length = atts.getLength();
		for (int i = 0; i < length; ++i) {
			Attr a = (Attr) atts.item(i);
			a.setPrefix(prefixes.get(a.getNamespaceURI()));
		}
	}
}
