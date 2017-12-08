package cn.com.pyc.szpbb.sdk.manager;

import android.util.Log;
import android.util.Xml;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.pyc.szpbb.sdk.bean.FILEFormat;
import cn.com.pyc.szpbb.sdk.models.xml.OEX_Agreement;
import cn.com.pyc.szpbb.sdk.models.xml.OEX_Agreement.OEX_Asset;
import cn.com.pyc.szpbb.sdk.models.xml.OEX_Agreement.OEX_Permission;
import cn.com.pyc.szpbb.sdk.models.xml.OEX_Rights;
import cn.com.pyc.szpbb.sdk.models.xml.XML2JSON_Album;
import cn.com.pyc.szpbb.util.FileUtil;
import cn.com.pyc.szpbb.util.SZLog;

final class ParserEngine {

    static class CommonFile {
        String filename;
        String filepath;
        FILEFormat filetype;
        //public long filestart;
        //public long fileSize;
    }

    private static final String TAG = "parser_tag";
    /* 权限类型 */
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY = "display";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_PLAY = "play";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_PRINT = "print";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE = "execute";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_EXPORT = "export";

    /**
     * drm文件构成 drm分为有头文件和资源文件两部分构成，中间通过32byte的md5做分割 <br/>
     * 前8byte通过一个long类型变量，记录了整个头文件的长度 <br/>
     * 每个头文件又分为3部分: <br/>
     * 40个byte记录文件名， 8个byte记录一个long类型变量，表示文件的起点 <br/>
     * 8个byte记录一个long类型变量，表示文件的终点 <br/>
     * 前8byte的long类型变量 /(40+8+8) = 头文件的个数，每个头文件都对应着一个资源文件，所以也就得到了资源文件的个数 <br/>
     * <br/>
     * <p>
     * 解析文件，得到资源文件
     *
     * @param drmPath    被解析文件路径
     * @param decodePath 解析后保存文件路径
     */
    static List<CommonFile> parserDRMFile(String drmPath, String decodePath) {

        List<CommonFile> files = new ArrayList<CommonFile>();

        File file = new File(drmPath);

        if (!FileUtil.checkFileExist(drmPath)) {
            Log.i(TAG, "drmfile not exist:" + drmPath);
            return null;
        }
        FileOutputStream fos = null;
        FileInputStream fis = null;
        byte[] eightBytes = new byte[8];
        byte[] fileMd5Bytes = new byte[32];
        byte[] headFileNameBytes = new byte[40];
        int bufferLength = 1024 * 1024;
        try {
            // 头文件解析
            fis = new FileInputStream(file);

            fis.read(eightBytes);
            long len1 = bytes2Long(eightBytes);
            int count = (int) (len1 / (headFileNameBytes.length
                    + eightBytes.length + eightBytes.length));

            long[] filestarts = new long[count];
            long[] fileends = new long[count];
            for (int i = 0; i < count; i++) {
                CommonFile f = new CommonFile();
                fis.read(headFileNameBytes);
                String filename = new String(headFileNameBytes).trim();
                f.filetype = getFileType(filename);
                f.filename = filename;
                f.filepath = decodePath + File.separator + filename;
                fis.read(eightBytes);
                filestarts[i] = bytes2Long(eightBytes);
                fis.read(eightBytes);
                fileends[i] = bytes2Long(eightBytes);
                files.add(f);

                SZLog.v(TAG, "filepath: " + f.filepath);
            }

            fis.read(fileMd5Bytes);

            // 资源文件解析
            for (int i = 0; i < count; i++) {
                File f = new File(decodePath, files.get(i).filename);
                if (!f.exists()) {
                    f.createNewFile();
                }
                fos = new FileOutputStream(f);
                int len = -1;
                byte[] buffer = null;
                if (fileends[i] > bufferLength) {
                    int sum = (int) ((fileends[i] - fileMd5Bytes.length) / bufferLength);
                    buffer = new byte[bufferLength];
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        // fos.flush();
                        sum--;
                        if (sum == 0) {
                            buffer = new byte[(int) ((fileends[i] - fileMd5Bytes.length) %
                                    bufferLength)];
                            fis.read(buffer);
                            fos.write(buffer);
                            // fos.flush();
                            fis.read(fileMd5Bytes);
                            break;
                        }
                    }

                } else {
                    buffer = new byte[(int) (fileends[i] - fileMd5Bytes.length)];
                    fis.read(buffer);
                    fos.write(buffer);
                    // fos.flush();
                    fis.read(fileMd5Bytes);
                }

                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return files;
    }

    /**
     * 解析right文件
     *
     * @param name
     * @return
     * @throws Exception
     */
    static OEX_Rights parserRight(File name) throws Exception {
        SZLog.i("parserRight path: " + name.getAbsolutePath());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(name);
            return readXML(fis);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("file not found");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 解析albumInfo.xml实际内容为JSON格式的字符
     *
     * @param albumInfoFile albumInfo文件
     * @param list          CommonFile文件
     * @return
     * @throws Exception
     */
    static XML2JSON_Album parserJSON(File albumInfoFile,
                                     List<CommonFile> list) throws Exception {
        StringBuilder sb = new StringBuilder();
        XML2JSON_Album albumInfo = new XML2JSON_Album();
        // 获得权限文件获得albumInfo.xml文件
        FileInputStream reader = null;
        try {

            reader = new FileInputStream(albumInfoFile);
            byte[] bytes = new byte[1024];
            while ((reader.read(bytes)) != -1) {
                sb.append(new String(bytes));
            }

            // 将字符串转成JSON对象
            JSONObject rootObj = new JSONObject(sb.toString());
            JSONObject contentNames = rootObj.getJSONObject("contentNames");

            ArrayList<String> al = new ArrayList<String>();
            for (int x = 0; x < list.size(); x++) {
                if (x > 1) {
                    String filename = list.get(x).filename.split("\\.")[0];
                    String contentname = contentNames.getString(filename);
                    al.add("\"" + filename + "\"");
                    al.add("\"" + contentname + "\"");
                }
            }
            // 对contentNames只能用字符串方式解析，因为key值不是固定的
            albumInfo.setContentList(al);
            albumInfo.setInfoList(parserJSONToArrayList(rootObj
                    .getString("infos")));
            albumInfo.setInfoObj(rootObj.getJSONObject("infos"));

            // 将字符串转成JSON对象
            // JSONObject rootObj = new JSONObject(sb.toString());
            // 对contentNames只能用字符串方式解析，因为key值不是固定的
            // albumInfo.setContentList(DRMUtil.parserJSONToArrayList(rootObj.getString
            // ("contentNames")));
            // albumInfo.setInfoList(DRMUtil.parserJSONToArrayList(rootObj.getString("infos")));
            // albumInfo.setInfoObj(rootObj.getJSONObject("infos"));

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("file not found");
        } catch (IOException e) {
            throw new IOException("");
        } catch (JSONException e) {
            throw new JSONException("json data is illegal");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return albumInfo;
    }

    /**
     * 获取文件类型
     *
     * @param fileName
     * @return
     */
    private static FILEFormat getFileType(String fileName) {
        String type = FileUtil.getExtNameFromFileName(fileName);
        if ("pdf".equalsIgnoreCase(type)) {
            return FILEFormat.PDF;
        } else if ("xml".equalsIgnoreCase(type)) {
            if (fileName.contains("albumInfo")) {
                return FILEFormat.ALBUMINFO;
            } else {
                return FILEFormat.RIGHT;
            }
        } else if ("mp3".equalsIgnoreCase(type)) {
            return FILEFormat.MP3;
        } else if ("mp4".equalsIgnoreCase(type)) {
            return FILEFormat.MP4;
        } else if ("drm".equalsIgnoreCase(type)) {
            return FILEFormat.DRM;
        }

        return FILEFormat.UNKNOW;
    }

    /**
     * 读取xml文件里的内容并解析
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    private static OEX_Rights readXML(InputStream inputStream)
            throws Exception {
        boolean isFristUID = true;
        boolean isFristAssent = true;

        OEX_Rights right = null;
        OEX_Agreement agreement = null;
        OEX_Asset asset = null;
        OEX_Permission permission = null;
        Map<String, String> map = null;
        String value = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String startTag = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    switch (startTag) {
                        case "rights":
                            right = new OEX_Rights();
                            break;
                        case "version":
                            right.setContextMap("version", parser.nextText());
                            break;
                        case "uid":
                            if (isFristUID) {
                                right.setContextMap("uid", parser.nextText());
                                isFristUID = false;
                            } else {
                                asset.setOdd_uid(parser.nextText());
                            }
                            break;
                        case "agreement":
                            agreement = new OEX_Agreement();
                            break;
                        case "asset":
                            if (isFristAssent) {
                                asset = agreement.new OEX_Asset();
                            } else {
                                permission.setAssent_id(parser
                                        .getAttributeValue(0));
                            }
                            break;
                        case "DigestMethod":
                            asset.setDigest_algorithm_key(parser
                                    .getAttributeValue(0));
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
                            permission.setType(getPermissionType(startTag));
                            break;
                        case "constraint":
                            map = new HashMap<String, String>();
                            break;
                        case "datetime":
                            // <o-dd:datetime o-dd:start="1448872142446"
                            // o-dd:end="1546531199000">1131</o-dd:datetime>
                            // String end = parser.getAttributeValue(0);
                            // String start = parser.getAttributeValue(1);
                            String startTime = parser.getAttributeValue(null,
                                    "start");
                            String endTime = parser.getAttributeValue(null,
                                    "end");
                            SZLog.v("PullXML", "startTime: " + startTime);
                            SZLog.v("PullXML", "endedTime: " + endTime);

                            permission.setStartTime(startTime);
                            permission.setEndTime(endTime);
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
                } else if (eventType == XmlPullParser.END_TAG) {
                    switch (startTag) {
                        case "asset":
                            if (isFristAssent && asset != null) {
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
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return right;
    }

    protected static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    private static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    /**
     * 解析contentNames 将contentNames对应的字符串中的所有双引号之内的内容添加到一个list中
     *
     * @param json
     */
    private static ArrayList<String> parserJSONToArrayList(String json) {

        json.replaceAll("\\{", "");
        json.replaceAll("\\}", "");
        json.replaceAll("\"", "");

        ArrayList<String> contents = new ArrayList<String>();
        Pattern p = Pattern.compile("\"(.*?)\"");
        Matcher m = p.matcher(json);
        while (m.find()) {
            contents.add(m.group().trim());
        }

        return contents;
    }

    // 权限类型
    private static OEX_Agreement.PERMISSION_TYPE getPermissionType(String s) {
        switch (s) {
            case OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY:
                return OEX_Agreement.PERMISSION_TYPE.DISPLAY;
            case OEX_AGREEMENT_PERMISSION_TYPE_PLAY:
                return OEX_Agreement.PERMISSION_TYPE.PLAY;
            case OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE:
                return OEX_Agreement.PERMISSION_TYPE.EXECUTE;
            case OEX_AGREEMENT_PERMISSION_TYPE_EXPORT:
                return OEX_Agreement.PERMISSION_TYPE.EXPORT;
            case OEX_AGREEMENT_PERMISSION_TYPE_PRINT:
                return OEX_Agreement.PERMISSION_TYPE.PRINT;
        }
        return null;
    }

}
