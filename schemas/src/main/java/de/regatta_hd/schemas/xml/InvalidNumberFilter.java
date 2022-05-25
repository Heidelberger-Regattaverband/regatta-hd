package de.regatta_hd.schemas.xml;

import static java.util.Objects.requireNonNull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class InvalidNumberFilter extends XMLFilterImpl {

	private final String elementName;
	private final String attributeName;

	public InvalidNumberFilter(String elementName, String attributeName) {
		this.elementName = requireNonNull(elementName, "elementName must not be null");
		this.attributeName = requireNonNull(attributeName, "attributeName must not be null");
	}

	@Override
	public void startElement(String namespaceUri, String localName, String qName, Attributes atts) throws SAXException {
		if (localName.equals(this.elementName)) {
			String value = atts.getValue(this.attributeName);
			if (value.isBlank()) {
				AttributesImpl impl = new AttributesImpl(atts);
				int index = impl.getIndex(this.attributeName);
				String attrUri = impl.getURI(index);
				String attrLocalName = impl.getLocalName(index);
				String attrQName = impl.getQName(index);
				String attrType = impl.getType(index);
				impl.setAttribute(index, attrUri, attrLocalName, attrQName, attrType, "0");
				atts = impl;
			}
		}
		super.startElement(namespaceUri, localName, qName, atts);
	}

}