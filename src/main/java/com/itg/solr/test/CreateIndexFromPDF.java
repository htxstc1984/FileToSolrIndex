package com.itg.solr.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * 从PDF创建索引 <功能详细描述>
 * 
 * @author Administrator
 * @version [版本号, 2014年3月18日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class CreateIndexFromPDF {

	public static void main(String[] args) {
		String fileName = "e:/20140418090828163.pdf";
		String solrId = "20140418090828163.pdf";
		try {
			// indexFilesSolrCell(fileName, solrId);
			queryHighlight("公司新战略");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 从文件创建索引 <功能详细描述>
	 * 
	 * @param fileName
	 * @param solrId
	 * @see [类、类#方法、类#成员]
	 */
	public static void indexFilesSolrCell(String fileName, String solrId)
			throws IOException, SolrServerException {
		String urlString = "http://172.16.10.69:8080/solr/collection1";
		SolrServer solr = new HttpSolrServer(urlString);

		solr.deleteByQuery("src:内网文件");

		solr.commit();

		ContentStreamUpdateRequest up = new ContentStreamUpdateRequest(
				"/update/extract");

		String contentType = "application/pdf";
		up.addFile(new File(fileName), contentType);
		up.setParam("literal.id", solrId);
		up.setParam("literal.src", "内网文件");
		up.setParam("uprefix", "attr_");
		up.setParam("fmap.content", "content");

		up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);

		solr.request(up);

		// QueryResponse rsp = solr.query(new SolrQuery("*:*"));
		// System.out.println(rsp);
	}

	public static void queryHighlight(String str) throws Exception {
		String urlString = "http://172.16.10.69:8080/solr";
		SolrServer server = new HttpSolrServer(urlString);
		SolrQuery query = new SolrQuery();
		query.set("q", "text:" + str);// 高亮查询字段
		query.addFilterQuery("src:内网文件");
		query.setHighlight(true);// 开启高亮功能
		// query.setHighlightSnippets(10);
		query.addHighlightField("content");// 高亮字段
		query.setHighlightSimplePre("<font color=\"red\">");// 渲染标签
		query.setHighlightSimplePost("</font>");// 渲染标签
		QueryResponse qr = server.query(query);// 执行查询
		SolrDocumentList dlist = qr.getResults();
		// 第一个Map的键是文档的ID，第二个Map的键是高亮显示的字段名
		Map<String, Map<String, List<String>>> map = qr.getHighlighting();
		for (int i = 0; i < dlist.size(); i++) {
			SolrDocument d = dlist.get(i);// 获取每一个document
			System.out.println(map.get(d.get("id")).get("content"));// 打印高亮的内容
		}
	}
}