package cn.com.pyc.drm.utils;

import java.util.ArrayList;
import java.util.Locale;

import cn.com.pyc.drm.bean.MechanismBean;

import android.text.TextUtils;

/**
 * 处理字符串句类
 * 
 * @author qd
 * 
 */
public class StringUtil
{

	/**
	 * 作者 eg: 周大侠;周大王;
	 * 
	 * @param authors
	 * @return
	 */
	public static String formatAuthors(String authors)
	{
		if (TextUtils.isEmpty(authors))
			return "";
//			return "DRM";
		try
		{
			String[] strs = authors.split(";");
			if (strs.length < 3)
			{
				return authors.replace(";", "");
			}
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < strs.length; i++)
			{
				if (i != strs.length - 1)
				{
					sb.append(strs[i]);
					sb.append("，");
				} else
				{
					sb.append(strs[i]);
				}
			}
			return sb.toString();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return authors;
	}

	/**
	 * 验证手机格式
	 */
	public static boolean isMobileNO(String mobiles)
	{
		/*
		 * ss 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
		String telRegex = "[1][34578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(mobiles))
			return false;
		else
			return mobiles.matches(telRegex);
	}

	/**
	 * 根据固定地址result得到用户名
	 * 
	 * eg:http://video.suizhi.net:8654/Cloud/client/content/getProductInfo?
	 * username=s001&id=4268697e-4a7a-4605-a2bb-92b2a91a67dd
	 * 
	 * <br>
	 * 
	 * eg:http://video.suizhi.net:8654/Cloud/client/content/getProductInfo?
	 * SystemType=Meeting&username=cpecc&id=540d4057-cad7-41a7-99fb-0d8295e433a4
	 * 
	 * @param result
	 * @param offsetStr
	 *            取出的字符串，通过此字符串在result中的index，取出value。
	 * 
	 *            return value
	 */
	public static String getStringByResult(String result, String offsetStr)
	{
		if (TextUtils.isEmpty(result))
		{
			return "";
		}
		// String offsetStr = "username=";
		try
		{
			int start = result.indexOf(offsetStr);
			DRMLog.d("start[offsetStr]: " + start);

			String newResult = result.substring(start);
			// DRMLog.d("newResult: " + newResult);

			String subResult = newResult.substring(offsetStr.length());
			// DRMLog.d("subResult: " + subResult);
			if (subResult.contains("&"))
			{
				return subResult.split("&")[0];
			}
			return subResult;
		} catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 根据固定地址result得到用户名  根据;
	 * 
	 */
	public static String getStringByColon(String result, String offsetStr,String Symbol)
	{
		if (TextUtils.isEmpty(result))
		{
			return "";
		}
		// String offsetStr = "username=";
		try
		{
			int start = result.indexOf(offsetStr);
			DRMLog.d("start[offsetStr]: " + start);

			String newResult = result.substring(start);
			// DRMLog.d("newResult: " + newResult);

			String subResult = newResult.substring(offsetStr.length());
			// DRMLog.d("subResult: " + subResult);
			if (subResult.contains(Symbol))
			{
				return subResult.split(Symbol)[0];
			}
			return subResult;
		} catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	
	/**
	 * 根据固定地址result得到用户名  根据;
	 * 
	 */
	public static String getStringByColonHalf(String result, String offsetStr,String Symbol)
	{
		if (TextUtils.isEmpty(result))
		{
			return "";
		}
		// String offsetStr = "username=";
		try
		{
			int start = result.indexOf(offsetStr);
			DRMLog.d("start[offsetStr]: " + start);

			String newResult = result.substring(start);
			// DRMLog.d("newResult: " + newResult);

			String subResult = newResult.substring(offsetStr.length());
			// DRMLog.d("subResult: " + subResult);
			if (subResult.contains(Symbol))
			{
				return subResult.split(Symbol)[1];
			}
			return subResult;
		} catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}


	/**
	 * 
	 * 根据给定的url地址（如下），获取对应的主机host和端口号port
	 * 
	 * eg1:http://video.suizhi.net:8657/Cloud/client/content/getProductInfo?
	 * SystemType
	 * =Meeting&username=weiqingfeng&id=48eb7526-dc0b-4d94-ad43-0296db32b804
	 * 
	 * <br/>
	 * <br/>
	 * 
	 * eg2: http://video.suizhi.net:8657/Cloud/client/content/getProductInfo?
	 * SystemType
	 * =Meeting&username=weiqingfeng&id=3b73c89c-fd2c-4b6f-b811-059e1899743d
	 * &verify=true&isverify_url=http://115.28.59.184/MT/WebService.asmx?op=
	 * LoginChecking
	 * 
	 * 
	 * @param resultUrl
	 * @return returnStrs <br/>
	 *         returnStrs[0] = host主机 <br/>
	 *         returnStrs[1] = port端口 <br/>
	 */
	public static String[] getHostAndPortByResult(String resultUrl)
	{
		if (TextUtils.isEmpty(resultUrl))
		{
			return null;
		}
		String preOffset = "http://";
		if (resultUrl.startsWith("http://"))
		{
			preOffset = "http://";
		} else if (resultUrl.startsWith("https://"))
		{
			preOffset = "https://";
		}
		String[] returnStrs = new String[2];
		try
		{
			DRMLog.i("--------------------------------------");
			String newResult = resultUrl.substring(preOffset.length());
			DRMLog.d("newResult=" + newResult);
			int middleOffset = newResult.indexOf(":");
			DRMLog.d("middleOffset:=" + middleOffset);

			String hostStr = newResult.substring(0, middleOffset);
			DRMLog.e("getStringHostAndPort", "hostStr=" + hostStr);
			returnStrs[0] = hostStr;

			// String endStr = resultUrl.substring(middleOffset);
			// DRMLog.i("endStr=" + endStr);
			int endOffset = newResult.indexOf("/");
			DRMLog.d("endOffset/=" + endOffset);
			String portStr = newResult.substring(middleOffset + 1, endOffset);
			DRMLog.e("getStringHostAndPort", "portStr=" + portStr);
			returnStrs[1] = portStr;
			DRMLog.i("--------------------------------------");

			return returnStrs;
		} catch (Exception e)
		{
			return null;
		}

	}

	

	
	public static MechanismBean getMechanismBeanByStr(String morganization){
		MechanismBean mmechanis_data =new MechanismBean();
		String[] MechanismArray=morganization.split(",");
		mmechanis_data.setServerAddress(MechanismArray[0]);
		mmechanis_data.setServerName(MechanismArray[1]);
		mmechanis_data.setSZUserName(MechanismArray[2]);
		return mmechanis_data;
	}
	
	
	
	
	
	/**
	 * 截取字符串
	 * 
	 * @param search
	 *            待搜索的字符串
	 * @param start
	 *            起始字符串 例如：<title>
	 * @param end
	 *            结束字符串 例如：</title>
	 * @param defaultValue
	 * @return
	 */
	public static String substring(String search, String start, String end, String defaultValue)
	{
		int start_len = start.length();
		int start_pos = isEmpty(start) ? 0 : search.indexOf(start);
		if (start_pos > -1)
		{
			int end_pos = isEmpty(end) ? -1 : search.indexOf(end, start_pos + start_len);
			if (end_pos > -1)
				return search.substring(start_pos + start.length(), end_pos);
			else
				return search.substring(start_pos + start.length());
		}
		return defaultValue;
	}

	/**
	 * 截取字符串
	 * 
	 * @param search
	 *            待搜索的字符串
	 * @param start
	 *            起始字符串 例如：<title>
	 * @param end
	 *            结束字符串 例如：</title>
	 * @return
	 */
	public static String substring(String search, String start, String end)
	{
		return substring(search, start, end, "");
	}

	/**
	 * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty(String input)
	{
		if (input == null || "".equals(input))
			return true;

		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
			{
				return false;
			}
		}
		return true;
	}
	/**
	 * false : 参数等于null 或者等于""
	 * true : 参数为正常字符串
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean StrisEmpty(String str)
	{
		if (str == null || "".equals(str))
			return false;
		return true;

		
	}

	
	
	
	
	
	/**
	 * 汉字返回拼音，字母原样返回，都转换为小写
	 * 
	 * @param input
	 * @return
	 */
	public static String getPinYin(String input)
	{
		ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(input);
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0)
		{
			for (HanziToPinyin.Token token : tokens)
			{
				if (HanziToPinyin.Token.PINYIN == token.type)
				{
					sb.append(token.target);
				} else
				{
					sb.append(token.source);
				}
			}
		}
		return sb.toString().toLowerCase(Locale.getDefault());
	}

}
