package Just_Test.weixin;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

public class SendWeChatMessage {
	// 系统properties文件名称
	//    private static final String EMAILCONFIG = "emailAndMsgConfig";
	// 发送消息类型
	private final static String MSGTYPE = "text";
	// 发送消息分组所有成员
	private final static String TOPARTY = "@all";
	// 获取配置文件中的值
	private final static String CORPID = "wwc6de8bfd166cc11e";// 需要自己申请，官网有试用企业号
	// 可以申请试用
	private final static String CORPSECRET = "tpe0WR0D6wGkJT3eMkE4bms7L6zVTToOpvUah6PBeew";
	// 获取访问权限码URL
	private final static String ACCESS_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
	// 创建会话请求URL
	private final static String CREATE_SESSION_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";

	// 获取接口访问权限码
	public String getAccessToken() {
		HttpClient client = new HttpClient();
		//        PostMethod post = new PostMethod(ACCESS_TOKEN_URL);
		GetMethod get = new GetMethod(ACCESS_TOKEN_URL);
		get.releaseConnection();
		get.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		NameValuePair[] param = { new NameValuePair("corpid", CORPID), new NameValuePair("corpsecret", CORPSECRET) };
		// 设置策略，防止报cookie错误
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		// 给post设置参数
		get.setQueryString(param);
		String result = null;
		try {
			client.executeMethod(get);
			result = new String(get.getResponseBodyAsString().getBytes("utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 将数据转换成json

		if (result == null || result == "") {
			System.out.println("*****************");
			System.out.println("result:null");
		} else {
			System.out.println("result is not  null:" + result.toString());
			Map<String,String> json = JSON.parseObject(result,Map.class);
			result = (String) json.get("access_token");
			// System.out.println(result);

			get.releaseConnection();
		}
		return result;

	}

	/**
	 * 企业接口向下属关注用户发送微信消息(实现方式一)
	 *
	 * @param touser
	 *            成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）。特殊情况：指定为@all，
	 *            则向关注该企业应用的全部成员发送
	 * @param toparty
	 *            部门ID列表，多个接收者用‘|’分隔，最多支持100个。当touser为@all时忽略本参数
	 * @param totag
	 *            标签ID列表，多个接收者用‘|’分隔。当touser为@all时忽略本参数
	 * @param content
	 *            消息内容
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String sendWeChatMessage(String touser, String toparty, String totag, String content) {
		HttpClient client = new HttpClient();
		String ACCESS_TOKEN = getAccessToken();
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"touser\":" + "\"" + touser + "\",");
		sb.append("\"toparty\":" + "\"" + toparty + "\",");
		sb.append("\"totag\":" + "\"" + totag + "\",");
		sb.append("\"msgtype\":" + "\"" + "text" + "\",");
		sb.append("\"agentid\":" + "\"" + "1000002" + "\",");
		sb.append("\"text\":" + "{");
		sb.append("\"content\":" + "\"" + content + "\"},");
		sb.append("\"debug\":" + "\"" + "1" + "\"");
		sb.append("}");
		// 请求链接
		String url = CREATE_SESSION_URL + ACCESS_TOKEN;
		PostMethod post = new PostMethod(url);
		post.releaseConnection();
		post.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
		// 设置策略，防止报cookie错误
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		// 给post设置参数
		post.setRequestBody(sb.toString());
		String result = "";
		try {
			client.executeMethod(post);
			result = new String(post.getResponseBodyAsString().getBytes("utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(result);

		post.releaseConnection();

		return result;

	}

	/**
	 * 此方法可以发送任意类型消息
	 *
	 * @param msgType
	 *            text|image|voice|video|file|news
	 * @param touser
	 *            成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）。特殊情况：指定为@all，
	 *            则向关注该企业应用的全部成员发送
	 * @param toparty
	 *            部门ID列表，多个接收者用‘|’分隔，最多支持100个。当touser为@all时忽略本参数
	 * @param totag
	 *            标签ID列表，多个接收者用‘|’分隔。当touser为@all时忽略本参数
	 * @param content
	 *            msgType=text时 ,文本消息内容
	 * @param mediaId
	 *            msgType=image|voice|video时 ,对应消息信息ID（--------）
	 * @param title
	 *            msgType=news|video时，消息标题
	 * @param description
	 *            msgType=news|video时，消息描述
	 * @param url
	 *            msgType=news时，消息链接
	 * @param picurl
	 *            msgType=news时，图片路径
	 * @param safe
	 *            表示是否是保密消息，0表示否，1表示是，默认0
	 */
	public void sendWeChatMsg(String msgType, String touser, String toparty, String totag, String content, String mediaId, String title,
			String description, String url, String picurl, String safe) {

		URL uRl;
		String ACCESS_TOKEN = getAccessToken();
		// 拼接请求串
		String action = CREATE_SESSION_URL + ACCESS_TOKEN;
		// 封装发送消息请求json
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"touser\":" + "\"" + touser + "\",");
		sb.append("\"toparty\":" + "\"" + toparty + "\",");
		sb.append("\"totag\":" + "\"" + totag + "\",");
		if (msgType.equals("text")) {
			sb.append("\"msgtype\":" + "\"" + msgType + "\",");
			sb.append("\"text\":" + "{");
			sb.append("\"content\":" + "\"" + content + "\"");
			sb.append("}");
		} else if (msgType.equals("image")) {
			sb.append("\"msgtype\":" + "\"" + msgType + "\",");
			sb.append("\"image\":" + "{");
			sb.append("\"media_id\":" + "\"" + mediaId + "\"");
			sb.append("}");
		} else if (msgType.equals("voice")) {
			sb.append("\"msgtype\":" + "\"" + msgType + "\",");
			sb.append("\"voice\":" + "{");
			sb.append("\"media_id\":" + "\"" + mediaId + "\"");
			sb.append("}");
		} else if (msgType.equals("video")) {
			sb.append("\"msgtype\":" + "\"" + msgType + "\",");
			sb.append("\"video\":" + "{");
			sb.append("\"media_id\":" + "\"" + mediaId + "\",");
			sb.append("\"title\":" + "\"" + title + "\",");
			sb.append("\"description\":" + "\"" + description + "\"");
			sb.append("}");
		} else if (msgType.equals("file")) {
			sb.append("\"msgtype\":" + "\"" + msgType + "\",");
			sb.append("\"file\":" + "{");
			sb.append("\"media_id\":" + "\"" + mediaId + "\"");
			sb.append("}");
		} else if (msgType.equals("news")) {
			sb.append("\"msgtype\":" + "\"" + msgType + "\",");
			sb.append("\"news\":" + "{");
			sb.append("\"articles\":" + "[");
			sb.append("{");
			sb.append("\"title\":" + "\"" + title + "\",");
			sb.append("\"description\":" + "\"" + description + "\",");
			sb.append("\"url\":" + "\"" + url + "\",");
			sb.append("\"picurl\":" + "\"" + picurl + "\"");
			sb.append("}");
			sb.append("]");
			sb.append("}");
		}
		sb.append(",\"safe\":" + "\"" + safe + "\",");
		sb.append("\"agentid\":" + "\"" + "1000002" + "\",");
		sb.append("\"debug\":" + "\"" + "1" + "\"");
		sb.append("}");
		String json = sb.toString();
		try {

			uRl = new URL(action);

			HttpsURLConnection http = (HttpsURLConnection) uRl.openConnection();

			http.setRequestMethod("POST");

			http.setRequestProperty("Content-Type",

					"application/json;charset=UTF-8");

			http.setDoOutput(true);

			http.setDoInput(true);

			System.setProperty("sun.net.client.defaultConnectTimeout", "30000");//
			// 连接超时30秒

			System.setProperty("sun.net.client.defaultReadTimeout", "30000"); //
			// 读取超时30秒

			http.connect();

			OutputStream os = http.getOutputStream();

			os.write(json.getBytes("UTF-8"));// 传入参数

			InputStream is = http.getInputStream();

			int size = is.available();

			byte[] jsonBytes = new byte[size];

			is.read(jsonBytes);

			String result = new String(jsonBytes, "UTF-8");

			System.out.println("请求返回结果:" + result);

			os.flush();

			os.close();

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	/**
	 * 企业接口向下属关注用户发送微信消息(实现方式二)
	 *
	 * @param touser
	 *            成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）。特殊情况：指定为@all，
	 *            则向关注该企业应用的全部成员发送
	 * @param toparty
	 *            部门ID列表，多个接收者用‘|’分隔，最多支持100个。当touser为@all时忽略本参数
	 * @param totag
	 *            标签ID列表，多个接收者用‘|’分隔。当touser为@all时忽略本参数
	 * @param content
	 *            消息内容
	 * @param safe
	 *            消息是否保密
	 * @return
	 */
	public void sendWeChatMsgText(String touser, String toparty, String totag, String content, String safe) {

		URL uRl;
		String ACCESS_TOKEN = getAccessToken();
		// 拼接请求串
		String action = CREATE_SESSION_URL + ACCESS_TOKEN;
		// 封装发送消息请求json
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"touser\":" + "\"" + touser + "\",");
		sb.append("\"toparty\":" + "\"" + toparty + "\",");
		sb.append("\"totag\":" + "\"" + totag + "\",");

		sb.append("\"msgtype\":" + "\"" + MSGTYPE + "\",");
		sb.append("\"text\":" + "{");
		sb.append("\"content\":" + "\"" + content + "\"");
		sb.append("}");

		sb.append(",\"safe\":" + "\"" + safe + "\",");
		sb.append("\"agentid\":" + "\"" + "1000002" + "\",");
		sb.append("\"debug\":" + "\"" + "1" + "\"");
		sb.append("}");
		String json = sb.toString();
		System.out.println("==========================");
		System.out.println(json);
		System.out.println("==========================");
		try {

			uRl = new URL(action);

			HttpsURLConnection http = (HttpsURLConnection) uRl.openConnection();

			http.setRequestMethod("POST");

			http.setRequestProperty("Content-Type",

					"application/json;charset=UTF-8");

			http.setDoOutput(true);

			http.setDoInput(true);

			System.setProperty("sun.net.client.defaultConnectTimeout", "30000");//
			// 连接超时30秒

			System.setProperty("sun.net.client.defaultReadTimeout", "30000"); //
			// 读取超时30秒

			http.connect();

			OutputStream os = http.getOutputStream();

			os.write(json.getBytes("UTF-8"));// 传入参数

			InputStream is = http.getInputStream();

			int size = is.available();

			byte[] jsonBytes = new byte[size];

			is.read(jsonBytes);

			String result = new String(jsonBytes, "UTF-8");

			System.out.println("请求返回结果:" + result);

			os.flush();

			os.close();

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	public static void main(String[] args) {
		SendWeChatMessage weChat = new SendWeChatMessage();
		weChat.sendWeChatMsgText("@all", "1", "", "微信测试", "0");
		// weChat.sendWeChatMsg("text", "mxlydx", "4", "", "测试senMsg", "", "",
		// "", "", "", "0");
		// weChat.sendWeChatMessage("mxlydq", "2", "", "Hi");
	}
}