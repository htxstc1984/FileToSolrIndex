package com.itg.solr.web;

import java.io.IOException;

import java.io.UnsupportedEncodingException;

import java.util.Enumeration;

import javax.servlet.Filter;

import javax.servlet.FilterChain;

import javax.servlet.FilterConfig;

import javax.servlet.ServletException;

import javax.servlet.ServletRequest;

import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

public class ProFilter implements Filter

{

	protected FilterConfig filterConfig;

	/**
	 * 
	 * 初始�?
	 */

	public void init(FilterConfig filterConfig) throws ServletException

	{

		this.filterConfig = filterConfig;

	}

	/**
	 * 
	 * 将inStr转为UTF-8的编码形�?
	 * 
	 * @param inStr
	 *            输入字符�?
	 * 
	 * @return UTF-8的编码形式的字符�?
	 * 
	 * @throws UnsupportedEncodingException
	 */

	private String toUTF(String inStr) throws UnsupportedEncodingException

	{

		String outStr = "";

		if (inStr != null)

		{

			// outStr=java.net.URLDecoder.decode(inStr);//不用decode�?到这的时候就已经自动decode过了

			// 将字符串转为UTF-8编码形式

			outStr = new String(inStr.getBytes("iso-8859-1"), "UTF-8");

		}

		return outStr;

	}

	/**
	 * 
	 * 中文乱码过滤处理
	 */

	public void doFilter(ServletRequest svlrequest,
			ServletResponse svlresponse,

			FilterChain chain) throws IOException, ServletException

	{

		// 将Servlet请求与响应对象转换成HttpServlet请求与响应对�?

		HttpServletRequest request = (HttpServletRequest) svlrequest;

		HttpServletResponse response = (HttpServletResponse) svlresponse;

		// 获得请求的方�?1.post or 2.get),根据不同请求方式进行不同处理

		String method = request.getMethod();

		// 1.以post方式提交的请�?直接设置编码为UTF-8

		if (method.equalsIgnoreCase("post"))

		{

			try

			{

				request.setCharacterEncoding("UTF-8");

			} catch (UnsupportedEncodingException e)

			{

				e.printStackTrace();

			}

		}

		// 2.以get方式提交的请�?

		else

		{

			// 取出客户提交的参数集

			Enumeration<String> paramNames = request.getParameterNames();

			// 遍历参数集取出每个参数的名称及�?

			while (paramNames.hasMoreElements())

			{

				String name = paramNames.nextElement();// 取出参数名称

				String values[] = request.getParameterValues(name);// 根据参数名称取出其�?

				// 如果参数值集不为�?

				if (values != null)

				{

					// 如果参数值集中只有一个�?

					if (values.length == 1)

					{

						try

						{

							// 调用toUTF(values[0])函数,(values[0]即第�?��参数�?方法转换参数值的字元编码

							String vlustr = toUTF(values[0]);

							// 并将该�?以属性的形式藏在request

							request.setAttribute(name, vlustr);

						} catch (UnsupportedEncodingException e)

						{

							e.printStackTrace();

						}

					}

					// 如果参数值集中有多个�?

					else

					{

						// 遍历参数值集

						for (int i = 0; i < values.length; i++)

						{

							try

							{

								// 回圈依次将每个�?调用toUTF(values[i])方法转换参数值的字元编码

								String vlustr = toUTF(values[i]);

								values[i] = vlustr;

							} catch (UnsupportedEncodingException e)

							{

								e.printStackTrace();

							}

						}

						// 将该值以属�?的形式藏在request

						request.setAttribute(name, values);

					}

				}

			}

		}
		// if (!request.getRequestURI().startsWith("./index.html")
		// && !request.getRequestURI().endsWith("/login.html")) {
		// if (request.getAttribute("loginUser") == null) {
		// request.setAttribute("message", "对不起，您还没登�?);
		// request.getRequestDispatcher("./index.html").forward(
		// svlrequest, svlresponse);
		// return;
		// }
		// }

		// 设置响应方式和支持中文的字元�?

		response.setContentType("text/html;charset=UTF-8");

		// 继续执行下一个filter,无一下个filter则执行请�?

		chain.doFilter(request, response);

	}

	/**
	 * 
	 * �?��方法
	 */

	public void destroy()

	{

	}

}
