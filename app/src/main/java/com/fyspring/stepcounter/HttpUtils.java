package com.fyspring.stepcounter;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class HttpUtils
{
	private static AsyncHttpClient client = new AsyncHttpClient(); // 实例化对象

	private static final String BASE_URL = "http://112.124.120.134/subdomain/8/?url=";
	
	// 用一个完整url获取一个string对象
	public static void get(String urlString, AsyncHttpResponseHandler res)
	{
		client.get(getAbsoluteUrl(urlString), res);
	}

	// url里面带参数
	public static void get(String urlString, RequestParams params, AsyncHttpResponseHandler res)
	{
		client.get(getAbsoluteUrl(urlString), params, res);
	}

	// 不带参数，获取json对象或者数组
	public static void get(String urlString, JsonHttpResponseHandler res)
	{
		client.get(urlString, res);
	}

	// 带参数，获取json对象或者数组
	public static void get(String urlString, RequestParams params, JsonHttpResponseHandler res)
	{
		client.get(getAbsoluteUrl(urlString), params, res);
		client.setConnectTimeout(60000);
		client.setTimeout(60000);
		client.setResponseTimeout(60000);
		String s = client.getUrlWithQueryString(false, getAbsoluteUrl(urlString), params);
	}

	// 下载数据使用，会返回byte数据
	public static void get(String uString, BinaryHttpResponseHandler bHandler)
	{
		client.get(getAbsoluteUrl(uString), bHandler);
	}
	
	/**
	 * post请求
	 */
	// 用一个完整url获取一个string对象
	public static void post(String urlString, AsyncHttpResponseHandler res)
	{
		client.post(getAbsoluteUrl(urlString), res);
	}

	// url里面带参数
	public static void post(String urlString, RequestParams params, AsyncHttpResponseHandler res)
	{
		client.post(getAbsoluteUrl(urlString), params, res);
	}

	public static void post(String urlString, JsonHttpResponseHandler res)
	{
		client.post(getAbsoluteUrl(urlString), res);
	}

	public static void post(String urlString, RequestParams params, JsonHttpResponseHandler res)
	{
		client.post(getAbsoluteUrl(urlString), params, res);
	}

	// 下载数据使用，会返回byte数据
	public static void post(String uString, BinaryHttpResponseHandler bHandler)
	{
		client.post(getAbsoluteUrl(uString), bHandler);
	}
	
	/**
	 * 采用单例设计模式
	 * @return
	 */
	public static AsyncHttpClient getClient()
	{
		client.setConnectTimeout(60000);
		client.setTimeout(60000);
		client.setResponseTimeout(60000);
		return client;
	}

	/**
	 * 下载文件
	 *
	 * @param uString
	 * @param file
	 */
	public static void downFile(String uString, FileAsyncHttpResponseHandler file)
	{
		client.post(uString, file);
	}

	/**
	 * 上传文件
	 *
	 * @param uString
	 * @param params
	 * @param handler
	 */
	public static void uploadFile(String uString, RequestParams params, AsyncHttpResponseHandler handler)
	{
		client.removeAllHeaders();
		client.post(uString, params, handler);
	}
	
	/**
	 * 获取文件绝对路径
	 * @param relativeUrl
	 * @return
	 */
	private static String getAbsoluteUrl(String relativeUrl)
	{
		return BASE_URL + relativeUrl;
	}
	
	/**
	 * 判断参数是否存在
	 * @param object
	 * @param key
	 * @return
	 */
	public static boolean isConantian(JSONObject object , String key)
	{
		return object.has(key);
	}
}
