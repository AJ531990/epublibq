package nl.siegmann.epublib.epub;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Section;

import org.apache.commons.lang.StringUtils;

/**
 * Writes the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *  
 * @author paul
 *
 */
public class PackageDocument {
	public static final String NAMESPACE_OPF = "http://www.idpf.org/2007/opf";
	public static final String NAMESPACE_DUBLIN_CORE = "http://purl.org/dc/elements/1.1/";
	public static final String PREFIX_DUBLIN_CORE = "dc";
	public static final String dateFormat = "yyyy-MM-dd";
	
	public static void write(EpubWriter writeAction, XMLStreamWriter writer, Book book) throws XMLStreamException {
		writer.writeStartDocument(Constants.encoding, "1.0");
		writer.setDefaultNamespace(NAMESPACE_OPF);
		writer.writeStartElement(NAMESPACE_OPF, "package");
		writer.writeNamespace(PREFIX_DUBLIN_CORE, NAMESPACE_DUBLIN_CORE);
//		writer.writeNamespace("ncx", NAMESPACE_NCX);
		writer.writeAttribute("xmlns", NAMESPACE_OPF);
		writer.writeAttribute("version", "2.0");
		writer.writeAttribute("unique-identifier", "BookID");

		writeMetaData(book, writer);

		writer.writeStartElement(NAMESPACE_OPF, "manifest");

		writer.writeEmptyElement(NAMESPACE_OPF, "item");
		writer.writeAttribute("id", writeAction.getNcxId());
		writer.writeAttribute("href", writeAction.getNcxHref());
		writer.writeAttribute("media-type", writeAction.getNcxMediaType());

		for(Resource resource: book.getResources()) {
			writer.writeEmptyElement(NAMESPACE_OPF, "item");
			writer.writeAttribute("id", resource.getId());
			writer.writeAttribute("href", resource.getHref());
			writer.writeAttribute("media-type", resource.getMediaType());
		}
		
		writer.writeEndElement(); // manifest

		writer.writeStartElement(NAMESPACE_OPF, "spine");
		writer.writeAttribute("toc", writeAction.getNcxId());
		writeSections(book.getSections(), writer);
		writer.writeEndElement(); // spine

		writer.writeEndElement(); // package
		writer.writeEndDocument();
	}

	private static void writeMetaData(Book book, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(NAMESPACE_OPF, "metadata");
		
		writer.writeStartElement(NAMESPACE_DUBLIN_CORE, "identifier");
		writer.writeAttribute(NAMESPACE_DUBLIN_CORE, "id", "BookdID");
		writer.writeAttribute(NAMESPACE_OPF, "scheme", "UUID");
		writer.writeCharacters(book.getUid());
		writer.writeEndElement(); // dc:identifier

		writer.writeStartElement(NAMESPACE_DUBLIN_CORE, "title");
		writer.writeCharacters(book.getTitle());
		writer.writeEndElement(); // dc:title

		for(Author author: book.getAuthors()) {
			writer.writeStartElement(NAMESPACE_DUBLIN_CORE, "creator");
			writer.writeAttribute(NAMESPACE_OPF, "role", "aut");
			writer.writeAttribute(NAMESPACE_OPF, "file-as", author.getLastname() + ", " + author.getFirstname());
			writer.writeCharacters(author.getFirstname() + " " + author.getLastname());
			writer.writeEndElement(); // dc:creator
		}

		for(String subject: book.getSubjects()) {
			writer.writeStartElement(NAMESPACE_DUBLIN_CORE, "subject");
			writer.writeCharacters(subject);
			writer.writeEndElement(); // dc:subject
		}

		writer.writeStartElement(NAMESPACE_DUBLIN_CORE, "date");
		writer.writeCharacters((new SimpleDateFormat(dateFormat)).format(book.getDate()));
		writer.writeEndElement(); // dc:date

		if(StringUtils.isNotEmpty(book.getLanguage())) {
			writer.writeStartElement(NAMESPACE_DUBLIN_CORE, "language");
			writer.writeCharacters(book.getLanguage());
			writer.writeEndElement(); // dc:date
		}

		if(StringUtils.isNotEmpty(book.getRights())) {
			writer.writeStartElement(NAMESPACE_DUBLIN_CORE, "rights");
			writer.writeCharacters(book.getRights());
			writer.writeEndElement(); // dc:rights
		}

		if(book.getMetadataProperties() != null) {
			for(Map.Entry<QName, String> mapEntry: book.getMetadataProperties().entrySet()) {
				writer.writeStartElement(mapEntry.getKey().getNamespaceURI(), mapEntry.getKey().getLocalPart());
				writer.writeCharacters(mapEntry.getValue());
				writer.writeEndElement();
				
			}
		}
		writer.writeEndElement(); // dc:metadata
	}

	/**
	 * Recursively list the entire section tree.
	 */
	private static void writeSections(List<Section> sections, XMLStreamWriter writer) throws XMLStreamException {
		for(Section section: sections) {
			if(section.isPartOfPageFlow()) {
				writer.writeEmptyElement(NAMESPACE_OPF, "itemref");
				writer.writeAttribute("idref", section.getItemId());
			}
			if(section.getChildren() != null && ! section.getChildren().isEmpty()) {
				writeSections(section.getChildren(), writer);
			}
		}
	}
}
