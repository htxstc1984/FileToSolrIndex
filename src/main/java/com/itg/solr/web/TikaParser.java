package com.itg.solr.web;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TikaParser {

	public TikaParser() {
		// TODO Auto-generated constructor stub
	}

	public static String parser(String filePath) {
		File indexFile = new File(filePath);
		if (!indexFile.exists()) {
			return null;
		}
		int dot = indexFile.getName().lastIndexOf(".");
		String extention = "";
		if (dot > -1 && dot < indexFile.getName().length()) {
			extention = indexFile.getName().substring(dot + 1); // --扩展名
		}
		Parser parser;
		if (extention.equalsIgnoreCase("doc")) {
			parser = new OfficeParser();
		} else if (extention.equalsIgnoreCase("docx")) {
			parser = new OOXMLParser();
		} else if (extention.equalsIgnoreCase("pdf")) {
			parser = new PDFParser();
		} else {
			return null;
		}
		/**
		 * */
		// InputStream iStream = new BufferedInputStream(new FileInputStream(
		// new File(PATH)));
		// OutputStream oStream = new BufferedOutputStream(new FileOutputStream(
		// new File(OUTPATH)));
		// ContentHandler iHandler = new BodyContentHandler(oStream);
		// parser.parse(iStream, iHandler, new Metadata(), new ParseContext());
		/**
		 * 处理指定编码的html.
		 */
		InputStream iStream = null;
		Writer writer = null;
		StringWriter stringWriter = null;
		String content;
		try {
			iStream = new BufferedInputStream(new FileInputStream(indexFile));
			stringWriter = new StringWriter();
			writer = new BufferedWriter(stringWriter);

			ContentHandler iHandler = new BodyContentHandler(writer);
			Metadata meta = new Metadata();
			meta.add(Metadata.CONTENT_ENCODING, "utf-8");
			parser.parse(iStream, iHandler, meta, new ParseContext());
			content = stringWriter.toString().replaceAll("\\n", "")
					.replaceAll("\\s{5,}", "");
			iStream.close();
			stringWriter.close();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			content = null;
			try {
				iStream.close();
				stringWriter.close();
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		return content;
	}
}
