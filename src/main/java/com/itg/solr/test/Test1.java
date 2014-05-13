package com.itg.solr.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test1 {

	public Test1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String regEx = "(t[^t])";
		String s = "c:test.txt";
		Pattern pat = Pattern.compile(regEx);
		Matcher mat = pat.matcher(s);
		while (mat.find()) {
			for (int i = 1; i <= mat.groupCount(); i++) {
				System.out.println(mat.group(i));
			}
		}

	}

}
