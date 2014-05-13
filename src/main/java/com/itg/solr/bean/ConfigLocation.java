package com.itg.solr.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ConfigLocation implements Serializable {

	private String baseDir;
	private String sourceName;
	private String serverUrl;
	private Map<String, String> contentTypes;

	public ConfigLocation() {
		// TODO Auto-generated constructor stub
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public Map<String, String> getContentTypes() {
		return contentTypes;
	}

	public void setContentTypes(Map<String, String> contentTypes) {
		this.contentTypes = contentTypes;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

}
