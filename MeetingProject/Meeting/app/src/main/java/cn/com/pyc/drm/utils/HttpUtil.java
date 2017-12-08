package cn.com.pyc.drm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.common.DrmPat;

/**
 * http
 */

public class HttpUtil
{
	private final static int RETRY_TIME = 3;
	private static HttpClient customerHttpClient;
	
	public static synchronized HttpClient getHttpClient()
	{
		if (null == customerHttpClient)
		{
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, DrmPat.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, getUserAgent());
			// 超时设置
			// 从连接池中获取连接超时
			ConnManagerParams.setTimeout(params, 1000000);
			// 连接超时
			int connectionTimeOut = 300000;
			if (!CommonUtil.isWifi(App.getInstance()))
			{// 手机网络，连接超时15秒
				connectionTimeOut = 15 * 1000;
			}
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
			// 请求超时
			HttpConnectionParams.setSoTimeout(params, 3000000);
			// 设置我们的httpclient支持http和https两种连接方式
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", PlainSocketFactory.getSocketFactory(), 443));
			ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
			customerHttpClient = new DefaultHttpClient(connectionManager, params);
		}

		return customerHttpClient;
	}

	/**
	 * 请求服务器
	 * 
	 * @param strUrl
	 *            地址
	 * @param params
	 *            参数
	 * 
	 * @return jsonString
	 */
	@Deprecated
	public static String postByHttpClient(String strUrl, List<NameValuePair> params)
	{
		DRMLog.i("url = " + strUrl);
		int errorCode = -1;
		try
		{
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, DrmPat.UTF_8);
			HttpPost httpPost = new HttpPost(strUrl);
			httpPost.setEntity(entity);
			HttpClient httpClient = getHttpClient();
			HttpResponse response = httpClient.execute(httpPost);
			int stateCode = response.getStatusLine().getStatusCode();
			if (stateCode != HttpStatus.SC_OK)
			{
				errorCode = AppException.TYPE_HTTP_ERROR;
			} else
			{
				HttpEntity resEntity = response.getEntity();
				// 如果成功，返回。否则返回-1
				return EntityUtils.toString(resEntity, DrmPat.UTF_8);
			}

		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			errorCode = AppException.TYPE_RUN;
		} catch (ClientProtocolException e)
		{
			e.printStackTrace();
			errorCode = AppException.TYPE_HTTP_ERROR;
		} catch (IOException e)
		{
			e.printStackTrace();
			errorCode = AppException.TYPE_IO;
		}
		return String.valueOf(errorCode);
	}


	/**
	 * 获取网络图片
	 * 
	 * @param url
	 * @return
	 */
	@Deprecated
	public static Bitmap getNetBitmap(String url) throws AppException
	{
		HttpClient httpClient = null;
		HttpGet httpGet = null;
		Bitmap bitmap = null;
		int time = 0;
		do
		{
			try
			{
				httpClient = getHttpClient();
				httpGet = new HttpGet(url);
				HttpResponse httpResponse = httpClient.execute(httpGet);
				// HttpStatus.SC_OK表示连接成功
				if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				{
					throw AppException.http(httpResponse.getStatusLine().getStatusCode());
				}
				InputStream inStream = httpResponse.getEntity().getContent();
				bitmap = BitmapFactory.decodeStream(inStream);
				inStream.close();
				break;
			} catch (IOException e)
			{
				time++;
				if (time < RETRY_TIME)
				{
					try
					{
						Thread.sleep(1000);
					} catch (InterruptedException e1)
					{
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally
			{
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		return bitmap;
	}

	private static String getUserAgent()
	{
		return "";
	}

}
