package org.hupeng.weixin.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hupeng.weixin.util.SignUtil;
import org.w3c.dom.NodeList;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.xml.xpath.*;

/**
 * 核心请求处理类
 * 
 * @author hupeng
 * @date 2013-05-18
 */
public class CoreServlet extends HttpServlet {
	private static final long serialVersionUID = 4440739483644821986L;
	private static final Logger log = Logger.getLogger(CoreServlet.class);

	/**
	 * 确认请求来自微信服务器
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		// 随机字符串
		String echostr = request.getParameter("echostr");

		PrintWriter out = response.getWriter();
		// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败

		if (signature == null || timestamp == null || nonce == null || echostr == null) {
			out.close();
			out = null;
			return;
		}

		if (SignUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr);
		}
		out.close();
		out = null;
	}

	/**
	 * 处理微信服务器发来的消息
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO 消息的接收、处理、响应
		String content = IOUtils.toString(request.getInputStream(), "utf-8");
		System.out.println(content);
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
			org.w3c.dom.Document doc = builder.parse(bis);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression expr = xpath.compile("//FromUserName/text()");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			XPathExpression expr1 = xpath.compile("//ToUserName/text()");
			Object result1 = expr1.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes1 = (NodeList) result1;

			XPathExpression expr2 = xpath.compile("//Content/text()");
			Object result2 = expr2.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes2 = (NodeList) result2;
			bis.close();
			System.out.println(nodes2.item(0).getNodeValue());

			if (nodes2.item(0).getNodeValue().equals("胡鹏")) {
				Document document = DocumentHelper.createDocument();
				
				String res = "胡鹏\n 9000335\n 13917658422\n 技术平台架构\n";
				String text = "<xml>" + "<ToUserName><![CDATA[" + nodes.item(0).getNodeValue() + "]]></ToUserName>"
				      + "<FromUserName><![CDATA[" + nodes1.item(0).getNodeValue() + "]]></FromUserName>"
				      + "<CreateTime>12345678</CreateTime>" + "<MsgType><![CDATA[text]]></MsgType>" + "<Content><![CDATA["
				      + res + "]]></Content>" + "</xml>";
				
				
				
				document = DocumentHelper.parseText(text);

				// OutputFormat format = OutputFormat.createPrettyPrint();
				// /*// 缩减格式
				// OutputFormat format = OutputFormat.createCompactFormat();*/
				// /*// 指定XML编码
				// format.setEncoding("GBK");*/
				// XMLWriter output = new XMLWriter(new FileWriter("output.xml"), format);
				// output.write(document);
				// output.close();
				response.setCharacterEncoding("UTF-8");
				PrintWriter out = response.getWriter();
				out.print(document.asXML());
				out.close();
				out = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}