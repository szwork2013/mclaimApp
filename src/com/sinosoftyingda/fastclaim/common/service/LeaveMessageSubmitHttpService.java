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

import com.sinosoftyingda.fastclaim.common.config.SystemConfig;
import com.sinosoftyingda.fastclaim.common.db.dao.TblInfoBuffer;
import com.sinosoftyingda.fastclaim.common.db.dao.TblPolicyMsg;
import com.sinosoftyingda.fastclaim.common.model.AppointModel;
import com.sinosoftyingda.fastclaim.common.model.CommonResponse;
import com.sinosoftyingda.fastclaim.common.model.LeaveMsgRequest;
import com.sinosoftyingda.fastclaim.common.model.PolicyMsgInsuranceType;
import com.sinosoftyingda.fastclaim.common.model.PolicyMsgRequest;
import com.sinosoftyingda.fastclaim.common.model.PolicyMsgResponse;
import com.sinosoftyingda.fastclaim.common.utils.HttpUtils;
import com.sinosoftyingda.fastclaim.survey.utils.UtilIsNotNull;

/**
 * 保单详细信息接口类
 * 
 * @author 郝运 20130225
 * 
 */
public class LeaveMessageSubmitHttpService {
	/**
	 * 保单详细信息接口方法
	 * 
	 * @param responseDuty
	 * @return
	 */
	public static CommonResponse leaveMsgService(LeaveMsgRequest leaveMsgData, String url) {
		CommonResponse responseDuty = new CommonResponse();
			try{
				String requestXML = createLeaveMsgRequest(leaveMsgData);
				
				Log.i("mclaim", "接受任务------------->requestXML:" + requestXML);
				HttpUtils httpUtils = new HttpUtils();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("content", requestXML);
				String responseXML = httpUtils.doPost(url, params);
				Log.i("mclaim", "接受任务------------->responseXML:" + responseXML);
				responseDuty = leaveMsgResponseXml(responseXML);
				
				
				
				
				
			}catch(Exception e){
				e.printStackTrace();
				responseDuty.setResponseMessage("终端与快赔交互失败:" + e.getMessage());
			}
		return responseDuty;
	}

	/**
	 * 报文创建方法
	 * 
	 * @throws Exception
	 */
	private static String createLeaveMsgRequest(LeaveMsgRequest leaveMsgData) throws Exception {
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
		serializer.text("MessageSubmit");
		serializer.endTag("", "REQUESTTYPE");
		// 接口用户名
		serializer.startTag("", "INTERFACEUSERCODE");
		serializer.text(leaveMsgData.getInterfaceUserCode());
		serializer.endTag("", "INTERFACEUSERCODE");
		// 接口密码
		serializer.startTag("", "INTERFACEPASSWORD");
		serializer.text(leaveMsgData.getInterfacePassWord());
		serializer.endTag("", "INTERFACEPASSWORD");

		serializer.endTag("", "HEAD");
		/**
		 * BODY
		 */
		serializer.startTag("", "BODY");
		// 报案号
		serializer.startTag("", "REGISTNO");
		serializer.text(leaveMsgData.getRegistNo());
		serializer.endTag("", "REGISTNO");
		// 撰写留言日期
		serializer.startTag("", "INPUTDATE");
		serializer.text(leaveMsgData.getLeavemsgtime());
		serializer.endTag("", "INPUTDATE");

		// 节点类型
		serializer.startTag("", "NODETYPE");
		serializer.text(leaveMsgData.getLeavemsgnodetype());
		serializer.endTag("", "NODETYPE");

		// 操作人员姓名
		serializer.startTag("", "OPERATORNAME");
		serializer.text(leaveMsgData.getLeavemsgperson());
		serializer.endTag("", "OPERATORNAME");
		//操作人员代码
		serializer.startTag("", "OPERATORCODE");
		serializer.text(leaveMsgData.getLeavemsgpersoncode());
		serializer.endTag("", "OPERATORCODE");

		// 报案号
		serializer.startTag("", "CONTEXT");
		serializer.text(leaveMsgData.getLeavemsgContent());
		serializer.endTag("", "CONTEXT");

		serializer.endTag("", "BODY");
		serializer.endTag("", "PACKET");
		serializer.endDocument();
		return writer.toString();
	}

	/**
	 * 响应xml进行解析
	 * 
	 */
	private static CommonResponse leaveMsgResponseXml(String responseXml) throws Exception {

		/**
		 * 用于存放响应头和响应体
		 */
		HashMap<String, Object> response = new HashMap<String, Object>();
		CommonResponse responseDuty = new CommonResponse();
		/**
		 * 响应头
		 */
		if (responseXml == null) {
			responseDuty.setResponseMessage("服务器返回数据错误!请联系管理员!(终端)");
		} else {
			if (responseXml.equals("Timeout")) {
				responseDuty.setResponseMessage("移动端与快赔系统连接超时,请检查网络后重新连接!(终端)");

			} else if (responseXml.equals("Post") || responseXml.equals("")) {
				responseDuty.setResponseMessage("移动端与快赔连接失败,请联系管理员!(终端)");
			} else {
				/**
				 * 创建解析xml的解析器工厂XmlPullParserFactory
				 */
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
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
				AppointModel appointModel = null;
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_DOCUMENT) {// 开头
						eventType = parser.next();
					} else if (eventType == XmlPullParser.END_DOCUMENT) {// 结尾
						break;
					} else if (eventType == XmlPullParser.START_TAG) {// 标签头
						startTag = parser.getName();

						if (!startTag.equals("PACKET") && !startTag.equals("HEAD") && !startTag.equals("BODY")
								&& !startTag.equals("INSURANCETYPELIST") && !startTag.equals("INSURANCETYPE") && !startTag.equals("ITEMKIND")
								&& !startTag.equals("ITEMKINDLIST") && !startTag.equals("CENGAGELIST")) {
							if (!startTag.equalsIgnoreCase("CENGAGE"))
								value = parser.nextText().trim();
							if (startTag.equalsIgnoreCase("RESPONSETYPE")) {
								responseDuty.setReaponseType(value);
							} else if (startTag.equalsIgnoreCase("RESPONSECODE")) {
								responseDuty.setResponseCode(value);
							} else if (startTag.equalsIgnoreCase("RESPONSEMESSAGE")) {
								responseDuty.setResponseMessage(value);
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

		return responseDuty;
	}
}
