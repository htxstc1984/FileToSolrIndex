package com.itg.solr.test;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.itg.solr.web.TikaParser;

public class TikaDemo {
	// public static String PATH = "E:\\test.docx";
	// public static String PATH = "g:\\丁聪生前访谈：画漫画有个屁用！_夏冬红_新浪博客.htm";
	public static String PATH = "e:/20140429093040760.pdf";

	// public static String PATH = "E:\\summerbell的博客文章(32).pdf";

	public static String OUTPATH = PATH + ".OUT";

	/**
	 * @param args
	 * @throws TikaException
	 * @throws SAXException
	 * @throws IOException
	 */
	// public static void main(String[] args) throws IOException, SAXException,
	// TikaException {
	// // Parser parser = new OOXMLParser();
	// Parser parser = new OfficeParser();
	// // Parser parser = new PDFParser();
	// // Parser parser = new HtmlParser();
	// // Parser parser = new PDFParser();
	// /**
	// * */
	// // InputStream iStream = new BufferedInputStream(new FileInputStream(
	// // new File(PATH)));
	// // OutputStream oStream = new BufferedOutputStream(new FileOutputStream(
	// // new File(OUTPATH)));
	// // ContentHandler iHandler = new BodyContentHandler(oStream);
	// // parser.parse(iStream, iHandler, new Metadata(), new ParseContext());
	// /**
	// * 处理指定编码的html.
	// */
	// InputStream iStream = new BufferedInputStream(new FileInputStream(
	// new File(PATH)));
	// StringWriter stringWriter = new StringWriter();
	// Writer writer = new BufferedWriter(stringWriter);
	//
	// ContentHandler iHandler = new BodyContentHandler(writer);
	// Metadata meta = new Metadata();
	// meta.add(Metadata.CONTENT_ENCODING, "utf-8");
	// parser.parse(iStream, iHandler, meta, new ParseContext());
	// // String content = stringWriter.toString().replaceAll("\\n", "")
	// // .replaceAll("\\s{5,}", "");
	// String content = stringWriter.toString();
	// System.out.println(content);
	//
	// }

	public static void main(String[] args) throws IOException, SAXException,
			TikaException {
		System.out.println(TikaParser.parser(PATH));
	}
}
