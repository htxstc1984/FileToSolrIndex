package com.itg.solr.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Holder;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itg.solr.bean.ConfigLocation;
import com.itg.solr.bean.IndexVO;
import com.mchange.v2.c3p0.ComboPooledDataSource;

@Controller
public class IndexService {

	@Autowired(required = true)
	ConfigLocation config;

	@Autowired(required = true)
	@Qualifier("itgnetDS")
	private ComboPooledDataSource itgnetDS;

	private static Logger log = Logger.getLogger(IndexService.class);

	public IndexService() {
		// TODO Auto-generated constructor stub
	}

	@RequestMapping(value = "/search.html")
	public String search() {
		return "search";
	}

	@RequestMapping(value = "/clearArticleIndex.html")
	@ResponseBody
	public void clearArticleIndex() throws SolrServerException, IOException {
		String urlString = config.getServerUrl();
		SolrServer solr = new HttpSolrServer(urlString);
		solr.deleteByQuery("src:内网信息");
		solr.commit();
		System.out.println("清除内网信息索引");
	}

	@RequestMapping(value = "/createArticleIdx.html")
	@ResponseBody
	public void createArticleIndex() {
		List<IndexVO> idxList = new ArrayList<IndexVO>();
		String urlString = config.getServerUrl();
		SolrServer solr = new HttpSolrServer(urlString);
		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Connection dbConn;
		try {

			solr.deleteByQuery("src:内网信息");
			solr.commit();
			System.out.println("清除内网信息索引");

			int recnums = 0;
			dbConn = itgnetDS.getConnection();
			ResultSet rs = dbConn
					.createStatement()
					.executeQuery(
							"select id,title,content,('http://www.itg.net/Article.asp?infoId='+convert(char(10),id)) as url,'内网信息' as src from kfile where showflag='Y'");
			while (rs.next()) {
				SolrInputDocument doc = new SolrInputDocument();
				// 在这里请注意date的格式，要进行适当的转化，上文已提到
				doc.addField("id", rs.getString("id"));
				doc.addField("title", rs.getString("title"));
				doc.addField(
						"content",
						rs.getString("content")
								.replaceAll("\\&[a-zA-Z]{1,10};", "")
								.replaceAll("<[^>]*>", "")
								.replaceAll("\\n", "")
								.replaceAll("\\s{5,}", ""));
				doc.addField("url", rs.getString("url"));
				doc.addField("src", rs.getString("src"));
				docs.add(doc);
				if (docs.size() > 500) {
					recnums += docs.size();
					System.out.println("已添加" + recnums + "条索引");
					solr.add(docs);
					// 对索引进行优化
					solr.optimize();
					solr.commit();
					docs = null;
					docs = new ArrayList<SolrInputDocument>();
					solr.shutdown();
					solr = null;
					solr = new HttpSolrServer(urlString);
				}
			}

			rs.close();
			dbConn.close();

			if (docs.size() > 0) {
				solr.add(docs);
				// 对索引进行优化
				solr.optimize();
				solr.commit();
			}

			solr.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/createArticleIdx2.html")
	@ResponseBody
	public void createArticleIndex2() {
		List<IndexVO> idxList = new ArrayList<IndexVO>();
		String urlString = config.getServerUrl();
		SolrServer solr = new HttpSolrServer(urlString);
		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Connection dbConn;
		try {

			solr.deleteByQuery("src:内网信息");
			solr.commit();
			System.out.println("清除内网信息索引");

			int recnums = 0;
			dbConn = itgnetDS.getConnection();
			ResultSet rs = dbConn
					.createStatement()
					.executeQuery(
							"select id,title,foundtime,content,('http://www.itg.net/Article.asp?infoId='+convert(char(10),id)) as url,'内网信息' as src from kfile where showflag='Y' order by id desc");
			while (rs.next()) {
				IndexVO vo = new IndexVO();

				vo.setId(rs.getString("id"));
				vo.setTitle(rs.getString("title"));
				vo.setUrl(rs.getString("url"));
				vo.setContent(rs.getString("content")
						.replaceAll("\\&[a-zA-Z]{1,10};", "")
						.replaceAll("<[^>]*>", "").replaceAll("\\n", "")
						.replaceAll("\\s{5,}", ""));
				vo.setSrc(rs.getString("src"));
				String content = rs.getString("content");

				String regEx = "/admin/eEdit/uploadfile(/\\d{6}/\\d{17}(.docx|.doc|.pdf){1})";
				Pattern pat = Pattern.compile(regEx);
				Matcher mat = pat.matcher(content);
				Map<String, String> attachment = new HashMap<String, String>();
				int i = 0;
				while (mat.find()) {
					attachment.put("atta_" + i,
							config.getBaseDir() + mat.group(1));
					i++;
					System.out.println(vo.getId());
					System.out.println(mat.group(1));
				}
				if (attachment.size() > 0) {
					vo.setAttachment(attachment);
				}
				idxList.add(vo);

			}

			rs.close();
			dbConn.close();

			for (int i = 0; i < idxList.size(); i++) {
				IndexVO vo = idxList.get(i);
				SolrInputDocument doc = new SolrInputDocument();
				// 在这里请注意date的格式，要进行适当的转化，上文已提到
				doc.addField("id", vo.getId());
				doc.addField("title", vo.getTitle());
				doc.addField("content", vo.getContent());
				doc.addField("url", vo.getUrl());
				doc.addField("src", vo.getSrc());

				if (vo.getAttachment() != null) {
					Iterator<Entry<String, String>> it = vo.getAttachment()
							.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						String content = TikaParser.parser(entry.getValue());
						if (content != null && !content.equalsIgnoreCase("")) {
							doc.addField(entry.getKey(), content);
						}
					}
				}

				docs.add(doc);
				if (docs.size() > 500) {
					recnums += docs.size();
					System.out.println("已添加" + recnums + "条索引");
					solr.add(docs);
					// 对索引进行优化
					solr.optimize();
					solr.commit();
					docs = null;
					docs = new ArrayList<SolrInputDocument>();
					solr.shutdown();
					solr = null;
					solr = new HttpSolrServer(urlString);
				}
			}

			if (docs.size() > 0) {
				solr.add(docs);
				// 对索引进行优化
				solr.optimize();
				solr.commit();
			}

			solr.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/createFileIdx.html")
	@ResponseBody
	public void createFileIndex(HttpServletRequest request,
			HttpServletResponse response) {
		long start_all = System.currentTimeMillis();

		String urlString = config.getServerUrl();
		SolrServer solr = new HttpSolrServer(urlString);

		File file = new File(config.getBaseDir());
		if (!file.exists()) {
			try {
				response.getOutputStream().write(
						("找不到指定文件夹" + config.getBaseDir()).getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Holder<List<File>> h_list = new Holder<List<File>>(
				new ArrayList<File>());
		traversal(file, h_list);
		List<File> list = h_list.value;

		Map indexMap = new HashMap<String, String>();
		Properties pro = new Properties();
		File proFile = new File(config.getBaseDir() + "/"
				+ "solrIdx.properties");

		try {
			if (proFile.exists()) {
				pro.load(new InputStreamReader(new FileInputStream(config
						.getBaseDir() + "/" + "solrIdx.properties"), "utf-8"));
			} else {
				solr.deleteByQuery("src:" + config.getSourceName());
				solr.commit();
				System.out.println("清除" + config.getSourceName() + "索引");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Iterator<Entry<Object, Object>> it = pro.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			indexMap.put(key, value);
		}

		for (int i = 0; i < list.size(); i++) {
			File indexFile = list.get(i);
			String extention = "";
			int dot = indexFile.getName().lastIndexOf(".");
			if (dot > -1 && dot < indexFile.getName().length()) {
				extention = indexFile.getName().substring(dot + 1); // --扩展名
			}

			String md5 = getFileMD5(indexFile);
			if (indexMap.containsKey(indexFile.getName())) {
				if (indexMap.get(indexFile.getName()).equals(md5)) {
					continue;
				}
			}
			long start = System.currentTimeMillis();
			ContentStreamUpdateRequest up = new ContentStreamUpdateRequest(
					"/update/extract");
			try {
				up.addFile(indexFile, config.getContentTypes().get(extention));
				up.setParam("literal.id",
						config.getSourceName() + indexFile.getName());
				up.setParam(
						"literal.url",
						indexFile
								.getAbsolutePath()
								.replaceAll("\\\\", "/")
								.replace("D:/webfolder/ItgNewNew",
										"http://www.itg.net"));
				up.setParam("literal.src", config.getSourceName());
				up.setParam("uprefix", "attr_");
				up.setParam("fmap.content", "content");
				up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);

				indexMap.put(indexFile.getName(), md5);

				solr.request(up);
				up = null;
				long end = System.currentTimeMillis();
				System.out.println("索引文件：" + indexFile.getName() + ",共耗时"
						+ (start - end) + "ms");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				log.error(e.getMessage());
				continue;
			}

		}
		solr.shutdown();

		Properties newIndex = new Properties();
		Iterator<Entry<Object, Object>> itNew = indexMap.entrySet().iterator();
		while (itNew.hasNext()) {
			Entry<Object, Object> entry = itNew.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			newIndex.put(key, value);
		}
		long end_all = System.currentTimeMillis();
		// newIndex.put("总花费时间", (end_all - start_all) / 1000 + "秒");
		log.info("总花费时间:" + (end_all - start_all) / 1000 + "秒");
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(config.getBaseDir() + "/"
					+ "solrIdx.properties");
			newIndex.store(new OutputStreamWriter(outputStream, "utf-8"),
					new Date().toString());
			outputStream.close();
			System.gc();
			response.getOutputStream().write("创建索引成功".getBytes("utf-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@RequestMapping(value = "/query.html")
	@ResponseBody
	public void testQuery(HttpServletRequest request,
			HttpServletResponse response) throws SolrServerException,
			UnsupportedEncodingException, IOException {
		String jsonStr = "";
		String q = new String(request.getParameter("q").getBytes("ISO-8859-1"),
				"utf-8");
		if (q == null || q.equalsIgnoreCase("")) {
			jsonStr = "{\"code\":999,\"msg\":\"没有查询条件!\"}";
			response.getOutputStream().write(jsonStr.getBytes("utf-8"));
		} else {

			String urlString = "http://172.16.10.69:8080/solr";
			SolrServer server = new HttpSolrServer(urlString);
			SolrQuery query = new SolrQuery();
			query.set("q", "text:" + q);// 高亮查询字段
			// query.addFilterQuery("src:内网信息  OR src:内网文件");
			// query.addFilterQuery("src:内网文件");
			query.setHighlight(true);// 开启高亮功能
			query.setHighlightSnippets(1);
			query.addHighlightField("content");// 高亮字段
			query.setHighlightSimplePre("<font color=\"red\">");// 渲染标签
			query.setHighlightSimplePost("</font>");// 渲染标签
			QueryResponse qr = server.query(query);// 执行查询
			SolrDocumentList dlist = qr.getResults();
			// 第一个Map的键是文档的ID，第二个Map的键是高亮显示的字段名
			Map<String, Map<String, List<String>>> map = qr.getHighlighting();
			for (int i = 0; i < dlist.size(); i++) {
				SolrDocument d = dlist.get(i);// 获取每一个document

				// String aa = "" + map.get(d.get("id")).get("content");
				// aa = aa.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
				// "<[^>]*>", "");// 去掉网页中带有html语言的标签

				jsonStr += i
						+ ":   "
						+ map.get(d.get("id")).get("content").toString()
								.replaceAll("\\n", "")
								.replaceAll("\\s{5,}", "") + "<br/>";
				// System.out.println(map.get(d.get("id")).get("content"));//
				// 打印高亮的内容
			}
			response.getOutputStream().write(jsonStr.getBytes("utf-8"));
		}
	}

	public void traversal(File file, Holder<List<File>> holder) {
		File[] files = file.listFiles();
		for (File f : files) {
			// 判断是否为文件夹
			if (f.isDirectory()) {
				traversal(f, holder); // 如果是文件夹，重新遍历
			} else { // 如果是文件 就打印文件的路径
				int dot = f.getName().lastIndexOf(".");
				if (dot > -1 && dot < f.getName().length()) {
					String extention = f.getName().substring(dot + 1); // --扩展名
					if (config.getContentTypes().keySet().contains(extention)) {
						holder.value.add(f);
					}
				}

			}
		}
	}

	public String getFileMD5(File f) {
		// 计算MD5
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, f.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			String md5Value = bi.toString(16);
			return md5Value;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public static void main(String[] args) {
		String regEx = "(t)";
		String s = "c:\test.txt";
		Pattern pat = Pattern.compile(regEx);
		Matcher mat = pat.matcher(s);
		boolean rs = mat.find();
		for (int i = 1; i <= mat.groupCount(); i++) {
			System.out.println(mat.group(i));
		}

	}

}
