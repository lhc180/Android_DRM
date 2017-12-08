package cn.com.pyc.drm.utils.manager;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.Bundle;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.model.SigninReturnModel;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.HttpUtil;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.HttpHandler.State;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

/**
 * 
 * 请求网络工具类
 * 
 */
public class RequestHttpManager {

	private static final String TAG = "RequestHttp";
	private static RequestHttpManager instance;
	private static HttpUtils http;
	static final String SERVICE_NAME_SPACE = "http://microsoft.com/webservices/";

//	static final String SERVICE_HOST = "http://10.10.2.64:8010";
	static final String SERVICE_HOST = "http://kaihui.suizhi.com:8010";
	public static RequestHttpManager init() {
		createHttp();
		if (instance == null) {
			instance = new RequestHttpManager();
		}
		return instance;
	}

	private static void createHttp() {
		if (null == http) {
			http = new HttpUtils();
			// 设置当前HttpUtils实例缓存超时的时间，以毫秒为单位
			http.configCurrentHttpCacheExpiry(1000 * 5);
			// 设置请求超时的时间
			http.configTimeout(1000 * 30000);
		}
	}

	// 登录，注册设备 调用获取数据
	// public static void postInfo(String url, List<NameValuePair> params,
	// DRMNetHelper helper) {
	// helper.requestNetWork(url, params);
	// }

	/**
	 * 通过http路径获取ftp下载路径
	 * 
	 * @param strUrl
	 * @return
	 */
	public String getFTPUrlByHttpUrl(String strUrl) {
		int errorCode = -1;
		HttpGet httpGet = new HttpGet(strUrl);
		HttpClient httpclient = HttpUtil.getHttpClient();
		// HttpStatus.SC_OK表示连接成功
		try {
			HttpResponse httpResponse = httpclient.execute(httpGet);
			// HttpStatus.SC_OK表示连接成功
			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				errorCode = AppException.TYPE_HTTP_ERROR;
			} else {
				// 取得返回的字符串
				return EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			errorCode = AppException.TYPE_HTTP_ERROR;
		}
		return String.valueOf(errorCode);
	}

	/**
	 * 访问网络数据GET
	 */
	public HttpHandler<String> getData(String actionUrl, Bundle bundle, RequestCallBack<String> callBack) {
		RequestParams params = new RequestParams();
		DRMLog.w("actionUrl", actionUrl);
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				Object obj = bundle.get(key);
				DRMLog.d("params", key + " = " + obj);
				params.addQueryStringParameter(key, String.valueOf(bundle.get(key)));
			}
		}
		return http.send(HttpMethod.GET, actionUrl, params, callBack);
	}

	/**
	 * 访问网络数据POST
	 */
	public HttpHandler<String> postData(String actionUrl,  String username,  String MeetingId,RequestCallBack<String> callBack) {
		Bundle bundle = new Bundle();
		bundle.putString("username", username);
		bundle.putString("id", MeetingId);
		RequestParams params = new RequestParams();
		DRMLog.d("actionUrl", actionUrl);
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				Object obj = bundle.get(key);
				DRMLog.d("params", key + " = " + obj);
				params.addBodyParameter(key, String.valueOf(bundle.get(key)));
			}
		}
		return http.send(HttpMethod.POST, actionUrl, params, callBack);
	}

	/**
	 * 访问网络数据POST,同步访问<br/>
	 * 一般需要开启子线程,防止阻塞UI thread
	 */
	public String postDataSync(String actionUrl, Bundle bundle) {
		RequestParams params = new RequestParams();
		DRMLog.d("actionUrl", actionUrl);
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				Object obj = bundle.get(key);
				DRMLog.d("params", key + " = " + obj);
				params.addBodyParameter(key, String.valueOf(bundle.get(key)));
			}
		}
		try {
			ResponseStream stream = http.sendSync(HttpMethod.POST, actionUrl, params);
			if (stream != null && stream.getStatusCode() == HttpStatus.SC_OK) {
				DRMLog.d(TAG, "SyncResult: " + stream.readString());
				return stream.readString();
			}
			return AppException.TYPE_HTTP_ERROR + "";
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AppException.TYPE_HTTP_ERROR + "";
	}

	/**
	 * 访问网络数据GET,同步访问<br/>
	 * 一般需要开启子线程,防止阻塞UI thread
	 */
	public String getDataSync(String actionUrl, Bundle bundle) {
		RequestParams params = new RequestParams();
		DRMLog.w("actionUrl", actionUrl);
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				Object obj = bundle.get(key);
				DRMLog.d("params", key + " = " + obj);
				params.addQueryStringParameter(key, String.valueOf(bundle.get(key)));
			}
		}
		try {
			ResponseStream stream = http.sendSync(HttpMethod.GET, actionUrl, params);
			if (stream != null && stream.getStatusCode() == HttpStatus.SC_OK) {
				DRMLog.d(TAG, "SyncResult: " + stream.readString());
				return stream.readString();
			}
			return AppException.TYPE_HTTP_ERROR + "";
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AppException.TYPE_HTTP_ERROR + "";
	}

	public static void cancelHttp(HttpHandler<String> hHandler) {
		if (null != hHandler && hHandler.getState() != State.CANCELLED) {
			hHandler.cancel();
			DRMLog.e(TAG, "state: " + hHandler.getState().name());
			hHandler = null;
		}
	}

	/**
	 * 登陆接口
	 */
	public SigninReturnModel GetUserState(String strUser, String strPwd, String id, String SERVICE_URL) {

		final String methodName = "LoginChecking";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;

		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);

		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("id", id);
		soapObject.addProperty("username", strUser);
		soapObject.addProperty("password", strPwd);

		FutureTask<SigninReturnModel> task = new FutureTask<SigninReturnModel>(new Callable<SigninReturnModel>() {
			public SigninReturnModel call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					DRMLog.d("webservice-result: " + results.toString());
					String strjson = results.toString().split("=")[1];
					String newstrjson = strjson.substring(0, strjson.length() - 3);
					SigninReturnModel mNearDynamic = JSON.parseObject(newstrjson, SigninReturnModel.class);
					return mNearDynamic;
				}
				return null;
			}
		});
		new Thread(task).start();

		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 文件下载成功，调用服务器接口
	 */
	public static String setDownloadsuccess(String meetingid, String fileidjson, String devicesid, String downloadname) {

		String SERVICE_URL = "http://kaihui.suizhi.com/WebService.asmx?op=RecordsConsumption";
		final String methodName = "RecordsConsumption";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;

		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);

		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("meetingid", meetingid);
		soapObject.addProperty("fileidjson", fileidjson);
		soapObject.addProperty("devicesid", devicesid);
		soapObject.addProperty("downloadname", downloadname);

		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					DRMLog.d("webservice-result: " + results.toString());
					String strjson = results.toString().split("=")[1];
					String newstrjson = strjson.substring(0, strjson.length() - 3);
					return "newstrjson";
				}
				return null;
			}
		});
		new Thread(task).start();

		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取验证码接口
	 */
	public static String getGetPhoneCode(String phone) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WebPhoneCode.asmx?op=GetPhoneCode";
		final String methodName = "GetPhoneCode";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;

		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("phone", phone);
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();
					return str;
				}
				return null;
			}
		});
		new Thread(task).start();

		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 注册接口接口
	 */
	public static String getRegisterChecking(String phone, String username, String password, String phonecode) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WSUserRegister.asmx?op=RegisterChecking";
		final String methodName = "RegisterChecking";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;

		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("phone", phone);
		soapObject.addProperty("username", username);
		soapObject.addProperty("password", password);
		soapObject.addProperty("phonecode", phonecode);
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();
					return str;
				}
				return null;
			}
		});
		new Thread(task).start();

		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 找回密码接口
	 */
	public static String getRetrievePassword(String phone, String username, String password, String phonecode) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WSRetrievePassword.asmx?op=RegisterChecking";
		final String methodName = "GetRetrievePassword";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;

		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("phone", phone);
		soapObject.addProperty("username", username);
		soapObject.addProperty("password", password);
		soapObject.addProperty("phonecode", phonecode);
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();
					return str;
				}
				return null;
			}
		});
		new Thread(task).start();
		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 登陆接口
	 */
	public static String getLoginChecking(String username, String password) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WSUserLogin.asmx?op=LoginChecking";
		final String methodName = "LoginChecking";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("username", username);
		soapObject.addProperty("password", password);
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();
					return str;
				}
				return null;
			}
		});
		new Thread(task).start();
		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 机构列表接口
	 */
	public static String getWSOrganizationName(String phone) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WSOrganizationName.asmx";
		final String methodName = "GetOrganizationNameByPhone";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("phone", phone);
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();

					return str;
				}
				return null;
			}
		});
		new Thread(task).start();
		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 添加机构 phone 手机号 serveraddress 机构地址url servername 机构名称 szusername 绥知用户名
	 */
	public static String getWSOrganizationName(String phone, String serveraddress, String servername, String szusername) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WSAddOrganization.asmx";
		final String methodName = "GetAddOrganization";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("phone", phone);
		soapObject.addProperty("serveraddress", serveraddress);
		soapObject.addProperty("servername", servername);
		soapObject.addProperty("szusername", szusername);

		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();

					return str;
				}
				return null;
			}
		});
		new Thread(task).start();
		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	


	/**
	 * 获取机构信息
	 */
	public static String getCompany(String url) {
		final String methodName = url.split("=")[1];
		final HttpTransportSE htse = new HttpTransportSE(url, 15000);
		htse.debug = true;
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;


		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();

					return str;
				}
				return null;
			}
		});
		new Thread(task).start();
		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * 我的会议接口
	 */
	public static String getMeetingByOrganization(String phone, String serveraddress, String szusername) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WSMeeting.asmx";
		final String methodName = "GetMeetingByOrganization";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("phone", phone);
		soapObject.addProperty("serveraddress", serveraddress);
		soapObject.addProperty("szusername", szusername);

		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();

					return str;
				}
				return null;
			}
		});
		new Thread(task).start();
		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 删除机构
	 */
	public static String GetDeleteOrganization(String phone, String serveraddress) {
		String SERVICE_URL = SERVICE_HOST + "/WebService/WSDeleteOrganization.asmx";
		final String methodName = "GetDeleteOrganization";
		final HttpTransportSE htse = new HttpTransportSE(SERVICE_URL, 15000);
		htse.debug = true;
		final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject(SERVICE_NAME_SPACE, methodName);
		envelope.bodyOut = soapObject;
		envelope.dotNet = true;
		soapObject.addProperty("phone", phone);
		soapObject.addProperty("serveraddress", serveraddress);

		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			public String call() throws Exception {
				htse.call(SERVICE_NAME_SPACE + methodName, envelope);
				if (envelope.getResponse() != null) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					String str = results.getProperty(0).toString();

					return str;
				}
				return null;
			}
		});
		new Thread(task).start();
		try {
			return task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
