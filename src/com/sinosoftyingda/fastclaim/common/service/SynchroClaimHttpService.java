package com.sinosoftyingda.fastclaim.common.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import com.sinosoftyingda.fastclaim.common.model.CommonResponse;
import com.sinosoftyingda.fastclaim.common.model.SynchroClaimRequest;
import com.sinosoftyingda.fastclaim.common.model.SynchroClaimResponse;
import com.sinosoftyingda.fastclaim.common.utils.HttpUtils;

public class SynchroClaimHttpService {
	/**
	 * 同步理赔撤回接口方法
	 * @param responseDuty
	 * @return
	 */
	public static CommonResponse synchroClaimService(
			SynchroClaimRequest requestSynchro, String url) {
		CommonResponse responseDuty = new CommonResponse();
		try {
			String requestXML = createSynchroClaimRequest(requestSynchro);
			Log.i("mCliam", "同步理赔撤回------------->requestXML:" + requestXML);

			HttpUtils httpUtils = new HttpUtils();
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("content", requestXML);
			String responseXML = httpUtils.doPost(url, params);
			Log.i("mCliam", "同步理赔撤回------------->responseXML:" + responseXML);
			responseDuty = synchroClaimResponseXml(responseXML);
//			/**
//			 * 保存请求报文
//			 */
//			InfoBuffer infoBuffer = new InfoBuffer();
//			infoBuffer.setType("SYNCHROCLAIM");
//			infoBuffer.setXmlContent(requestXML);
////			TblInfoBuffer.addinfobuffer(infoBuffer);
//			/**
//			 * 保存响应报文
//			 */
//			infoBuffer.setType("SYNCHROCLAIMBACK");
//			infoBuffer.setXmlContent(responseXML);
////			TblInfoBuffer.addinfobuffer(infoBuffer);
		} catch (Exception e) {
			e.printStackTrace();
			responseDuty.setResponseMessage("终端与快赔交互失败:"+e.getMessage());
		}
		return responseDuty;
	}

	/**
	 * 报文创建方法
	 * 
	 * 
	 * @throws Exception
	 */
	private static String createSynchroClaimRequest(
			SynchroClaimRequest synchroClaimRequest) throws Exception {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", true);

		// 创建根标记packet
		serializer.startTag("", "PACKET");
		serializer.attribute("", "type", "REQUEST");
		serializer.attribute("", "version", "1.0");
		/**
		 * HEAD
		 */
		serializer.startTag("", "HEAD");
		// 请求类型
		serializer.startTag("", "REQUESTTYPE");
		serializer.text("SynchroClaim");
		serializer.endTag("", "REQUESTTYPE");
		// 接口用户名
		serializer.startTag("", "INTERFACEUSERCODE");
		serializer.text(synchroClaimRequest.getInterfaceUserCode());
		serializer.endTag("", "INTERFACEUSERCODE");
		// 接口密码
		serializer.startTag("", "INTERFACEPASSWORD");
		serializer.text(synchroClaimRequest.getInterfacePassWord());
		serializer.endTag("", "INTERFACEPASSWORD");

		serializer.endTag("", "HEAD");
		/**
		 * BODY
		 */
		serializer.startTag("", "BODY");

		// 报案号
		serializer.startTag("", "REGISTNO");
		serializer.text(synchroClaimRequest.getRegistNo());
		serializer.endTag("", "REGISTNO");
		// 节点类型
		serializer.startTag("", "NODETYPE");
		serializer.text(synchroClaimRequest.getNodeType());
		serializer.endTag("", "NODETYPE");
		// 损失编号
		serializer.startTag("", "LOSSNO");
		serializer.text(synchroClaimRequest.getLossNo());
		serializer.endTag("", "LOSSNO");
		// 撤回类型
				serializer.startTag("", "CANCLETYPE");
				serializer.text(synchroClaimRequest.getCancleType());
				serializer.endTag("", "CANCLETYPE");
		serializer.endTag("", "BODY");
		serializer.endTag("", "PACKET");
		serializer.endDocument();
		return writer.toString();
	}

	/**
	 * 响应xml进行解析
	 * 
	 */
	private static SynchroClaimResponse synchroClaimResponseXml(
			String responseXml) throws Exception {

		/**
		 * 用于存放响应头和响应体
		 */

		SynchroClaimResponse responseCommon = new SynchroClaimResponse();
		/**
		 * 响应头
		 */
		if (responseXml == null) {
			responseCommon.setResponseMessage("服务器返回数据错误!请联系管理员!(终端)");
		} else {
			if (responseXml.equals("Timeout")) {
				responseCommon.setResponseMessage("移动端与快赔系统连接超时,请检查网络后重新连接!(终端)");

			} else if (responseXml.equals("Post") || responseXml.equals("")) {
				responseCommon.setResponseMessage("移动端与快赔连接失败,请联系管理员!(终端)");
			} else {
				/**
				 * 创建解析xml的解析器工厂XmlPullParserFactory
				 */
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				/**
				 * 生成xml解析器XmlPullParser
				 */
				XmlPullParser parser = factory.newPullParser();
				/**
				 * 设置要解析的xml串,若果解析出现乱码,可以尝试使用 parser.setInput(InputStream
				 * inputStream, String inputEncoding)
				 */
				parser.setInput(new StringReader(responseXml));
				/**
				 * 开始解析事件
				 */
				int eventType = parser.getEventType();
				String startTag = null;
				String endTag = null;
				String value = null;
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_DOCUMENT) {// 开头
						eventType = parser.next();
					} else if (eventType == XmlPullParser.END_DOCUMENT) {// 结尾
						break;
					} else if (eventType == XmlPullParser.START_TAG) {// 标签头
						startTag = parser.getName();

						if (!startTag.equals("PACKET")
								&& !startTag.equals("HEAD")
								&& !startTag.equals("BODY")) {
							value = parser.nextText().trim();
							if (startTag.equalsIgnoreCase("RESPONSETYPE")) {
								responseCommon.setReaponseType(value);
							} else if (startTag
									.equalsIgnoreCase("RESPONSECODE")) {
								responseCommon.setResponseCode(value);
							} else if (startTag
									.equalsIgnoreCase("RESPONSEMESSAGE")) {
								responseCommon.setResponseMessage(value);
							} else if (startTag
									.equalsIgnoreCase("REGISTNO")) {
								responseCommon.setRegistNo(value);
							} else if (startTag
									.equalsIgnoreCase("NODETYPE")) {
								responseCommon.setNodeType(value);
							} else if (startTag
									.equalsIgnoreCase("LOSSNO")) {
								responseCommon.setLossNo(value);
							}
							eventType = parser.next();
						} else {
							eventType = parser.next();
						}

					} else if (eventType == XmlPullParser.END_TAG) {
						endTag = parser.getName();
						eventType = parser.next();
					} else {
						eventType = parser.next();
					}
				}

			}
		}

		return responseCommon;
	}
}
