package cn.com.pyc.drm.utils;

import android.util.Xml;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.model.xml.OEX_Agreement;
import cn.com.pyc.drm.model.xml.OEX_Agreement.OEX_Asset;
import cn.com.pyc.drm.model.xml.OEX_Agreement.OEX_Permission;
import cn.com.pyc.drm.model.xml.OEX_Rights;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PullXMLReader
{

	public static OEX_Rights readXML(InputStream inputStream) throws AppException
	{
		boolean isFristUID = true;
		boolean isFristAssent = true;

		OEX_Rights right = null;
		OEX_Agreement agreement = null;
		OEX_Asset asset = null;
		OEX_Permission permission = null;
		Map<String, String> map = null;
		String value = null;
		try
		{
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(inputStream, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				String startTag = parser.getName();
				if (eventType == XmlPullParser.START_TAG)
				{
					switch (startTag)
					{
					case "rights":
						right = new OEX_Rights();
						break;
					case "version":
						right.setContextMap("version", parser.nextText());
						break;
					case "uid":
						if (isFristUID)
						{
							right.setContextMap("uid", parser.nextText());
							isFristUID = false;
						} else
						{
							asset.setOdd_uid(parser.nextText());
						}
						break;
					case "agreement":
						agreement = new OEX_Agreement();
						break;
					case "asset":
						if (isFristAssent)
						{
							asset = agreement.new OEX_Asset();
						} else
						{
							permission.setAssent_id(parser.getAttributeValue(0));
						}
						break;
					case "DigestMethod":
						asset.setDigest_algorithm_key(parser.getAttributeValue(0));
						break;
					case "DigestValue":
						asset.setDigest_algorithm_value(parser.nextText());
						break;
					case "EncryptionMethod":
						asset.setEnc_algorithm(parser.getAttributeValue(0));
						break;
					case "RetrievalMethod":
						asset.setRetrieval_url(parser.getAttributeValue(0));
						break;
					case "CipherValue":
						asset.setCipheralue(parser.nextText());
						break;
					case "permission":
						permission = agreement.new OEX_Permission();
						break;
					case "display":
					case "play":
					case "print":
					case "execute":
					case "export":
						permission.setType(DRMUtil.getType(startTag));
						break;
					case "constraint":
						map = new HashMap<String, String>();
						break;
					case "datetime":
						String name=parser.getAttributeName(0);
						String start = null;
						String end = null;
						if("start".equals(name)){
							 start = parser.getAttributeValue(0);
							 end = parser.getAttributeValue(1);
						}else if("end".equals(name)){
							 end = parser.getAttributeValue(0);
							 start = parser.getAttributeValue(1);
							
						}
						permission.setStartTime(start);
						permission.setEndTime(end);
						value = parser.nextText();
						permission.setDays(value);
						map.put(startTag, value);
						break;
					case "individual":
						value = parser.nextText();
						permission.setIndividual(value);
						map.put(startTag, value);
						break;
					case "system":
						value = parser.nextText();
						permission.setSystem(value);
						map.put(startTag, value);
						break;
					case "accumulated":
						value = parser.nextText();
						permission.setAccumulated(value);
						map.put(startTag, value);
						break;
					}
				} else if (eventType == XmlPullParser.END_TAG)
				{
					switch (startTag)
					{
					case "asset":
						if (isFristAssent && asset != null)
						{
							agreement.setAssets(asset);
							isFristAssent = false;
						}
						break;
					case "constraint":
						permission.setAttributes(map);
						map = null;
						break;
					case "permission":
						isFristAssent = true;
						agreement.setPermission(permission);
						break;

					case "agreement":
						right.setAgreement(agreement);
						break;
					}

				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e)
		{
			throw AppException.xml(e);
		} catch (IOException e)
		{
			throw AppException.io(e);
		} finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		return right;
	}
}
