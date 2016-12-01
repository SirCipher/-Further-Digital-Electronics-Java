package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

public class XMLReader extends DefaultHandler {
	VideoFile currentVideo;
	String currentSubElement;
	List<VideoFile> videoList;
	String filename;

	/*
	 * public static void main(String[] args) { DefaultHandler handler = new
	 * XMLReader(); }
	 */

	public List<VideoFile> getList(String filename) {
		this.filename = filename;

		try {
			// use the default parser
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			// parse the input
			saxParser.parse(filename, this); // maybe should be videoList.xml?
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return videoList;
	}

	public void startDocument() throws SAXException {
		System.out.println("Started parsing: " + filename);
		videoList = new ArrayList<VideoFile>();
	}

	// Overridden method for end of document callback - used for reporting in
	// this example
	public void endDocument() throws SAXException {
		System.out.println("Finished parsing, stored " + videoList.size()
				+ " videos.");
		for (VideoFile thisVideo : videoList) {
			System.out.println("ID: " + thisVideo.getID());
			System.out.println("Title: " + thisVideo.getTitle());
			System.out.println("Filename: " + thisVideo.getFilename());
			System.out.println("Poster: " + thisVideo.getPoster());
		}
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException {

		// sort out element name if (no) namespace in use
		String elementName = localName;
		if ("".equals(elementName)) {
			elementName = qName;
		}

		switch (elementName) {
		case "video":
			currentVideo = new VideoFile();
			break;
		case "title":
			currentSubElement = "title";
			break;
		case "filename":
			currentSubElement = "filename";
			break;
		case "poster":
			currentSubElement = "poster";
			break;
		default:
			currentSubElement = "none";
			break;
		}

		if (attrs != null) {
			// Sort out attribute name
			String attributeName = attrs.getLocalName(0);
			if ("".equals(attributeName)) {
				attributeName = attrs.getQName(0);
			}
			// Store value
			String attributeValue = attrs.getValue(0);
			switch (elementName) {
			case "video":
				currentVideo.setId(attributeValue);
				break;
			case "title":
				currentVideo.setTitle(attributeValue);
				break;
			case "filename":
				currentVideo.setFilename(attributeValue);
				break;
			case "poster":
				currentVideo.setImageAddress(attributeValue);
			default:
				break;
			}
		}

	}

	// Overridden method for character data callback
	public void characters(char ch[], int start, int length)
			throws SAXException {
		String newContent = new String(ch, start, length);
		switch (currentSubElement) {
		case "title":
			currentVideo.setTitle(newContent);
			break;
		case "filename":
			currentVideo.setFilename(newContent);
			break;
		case "poster":
			currentVideo.setImageAddress(newContent);
			break;
		default:
			break;
		}
	}

	// Overridden method for end of element callback
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// Finishing an element means we're definitely not in a sub-element
		// anymore
		currentSubElement = "none";
		// Sort out element name if (no) namespace in use
		String elementName = localName;
		if ("".equals(elementName)) {
			elementName = qName;
		}
		if (elementName.equals("video")) {
			videoList.add(currentVideo);
			// We've finished and stored this student, so remove the reference
			currentVideo = null;
		}
	}
}