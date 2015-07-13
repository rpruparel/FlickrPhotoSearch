package com.example.inclass;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import android.util.Xml;

public class PhotoUtil {

	public static class PhotoSaxParser extends DefaultHandler {
		ArrayList < Photo > photosList;

		public ArrayList < Photo > getPhotosList() {
			return photosList;
		}

		Photo photo;
		StringBuilder xmlInnerText;

		static public ArrayList < Photo > parsePhotos(InputStream is)
		throws IOException, SAXException {
			Log.d("demo", "SAX parser is called");
			PhotoSaxParser parser = new PhotoSaxParser();
			Xml.parse(is, Xml.Encoding.UTF_8, parser);
			return parser.getPhotosList();
		}

		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.startDocument();
			photosList = new ArrayList < Photo > ();
			xmlInnerText = new StringBuilder();
		}

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.endDocument();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			super.startElement(uri, localName, qName, attributes);

			if ("photo".equals(localName)) {
				photo = new Photo();
				photo.setUrl(attributes.getValue("url_m"));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			// TODO Auto-generated method stub
			super.endElement(uri, localName, qName);
			if ("photo".equals(localName)) {
				photosList.add(photo);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			// TODO Auto-generated method stub
			super.characters(ch, start, length);
		}

	}

	public static class PhotoPullParser {
		static ArrayList < Photo > parsePhotos(InputStream in )
		throws XmlPullParserException, IOException {
			Log.d("demo", "PULL parser is called");

			XmlPullParser parser = XmlPullParserFactory.newInstance()
				.newPullParser();
			parser.setInput( in , "UTF-8");
			ArrayList < Photo > photosList = new ArrayList < Photo > ();
			Photo photo = null;

			int event = parser.getEventType();

			while (event != XmlPullParser.END_DOCUMENT) {

				switch (event) {
					case XmlPullParser.START_TAG:
						if (parser.getName().equals("photo")) {
							photo = new Photo();
							photo.setUrl(parser.getAttributeValue(null, "url_m"));
						}
						break;

					case XmlPullParser.END_TAG:
						if (parser.getName().equals("photo")) {
							photosList.add(photo);
						}
						break;

					default:
						break;
				}

				event = parser.next();
			}

			return photosList;
		}
	}

}