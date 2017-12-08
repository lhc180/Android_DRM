package cn.com.pyc.drm.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理字符串句类
 *
 * @author qd
 */
public class StringUtil {

    /**
     * unicode编码转化成汉字 eg:   \u9ED1\u9F99\u6C5F ------> 黑龙江
     */
    public static String unicodeToChinese(String unicodeStr) {
        if (!unicodeStr.contains("\\u")) {
            return unicodeStr;
        }
        StringBuilder builder = new StringBuilder();
        Matcher m = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(unicodeStr);
        while (m.find()) {
            builder.append((char) Integer.parseInt(m.group(1), 16));
        }
        return builder.toString();
    }

    /**
     * 作者 eg: 周大侠;周大王;
     *
     * @param authors
     * @return
     */
    @Deprecated
    public static String formatAuthors(String authors) {
        if (TextUtils.isEmpty(authors)) return "DRM";
        try {
            String[] strs = authors.split(";");
            if (strs.length < 3) {
                return authors.replace(";", "");
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < strs.length; i++) {
                if (i != strs.length - 1) {
                    sb.append(strs[i]);
                    sb.append("，");
                } else {
                    sb.append(strs[i]);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authors;
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
        /*
         * ss 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
        String telRegex = "[1][34578]\\d{9}";//
        // "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }

    /**
     * 根据固定地址result得到用户名
     * <p>
     * eg:http://video.suizhi.net:8654/Cloud/client/content/getProductInfo?
     * username=s001&id=4268697e-4a7a-4605-a2bb-92b2a91a67dd
     * <p>
     * <br>
     * <p>
     * eg:http://video.suizhi.net:8654/Cloud/client/content/getProductInfo?
     * SystemType=Meeting&username=cpecc&id=540d4057-cad7-41a7-99fb-0d8295e433a4
     *
     * @param result
     * @param offsetStr 取出的字符串，通过此字符串在result中的index，取出value。
     *                  <p>
     *                  return value
     */
    @Deprecated
    public static String getStringByResult(String result, String offsetStr) {
        if (TextUtils.isEmpty(result)) {
            return "";
        }
        // String offsetStr = "username=";
        try {
            int start = result.indexOf(offsetStr);
            DRMLog.d("start[offsetStr]: " + start);

            String newResult = result.substring(start);
            // DRMLog.d("newResult: " + newResult);

            String subResult = newResult.substring(offsetStr.length());
            // DRMLog.d("subResult: " + subResult);
            if (subResult.contains("&")) {
                return subResult.split("&")[0];
            }
            return subResult;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getString4Result(String result, String offsetStr) {
        String valueString = "";
        String regex = "(?i)" + offsetStr; // 忽略大小写
        try {
            String[] array = result.split(regex);
            valueString = array[1];
            if (valueString.contains("&")) {
                String _valueString = valueString.split("&")[0];
                valueString = _valueString;
            }
            DRMLog.d(offsetStr, valueString);
        } catch (Exception e) {
            e.printStackTrace();
            DRMLog.w(offsetStr + ":" + valueString);
        }
        return valueString;
    }

    /**
     * 截取字符串
     *
     * @param search       待搜索的字符串
     * @param start        起始字符串 例如：<title>
     * @param end          结束字符串 例如：</title>
     * @param defaultValue
     * @return
     */
    public static String substring(String search, String start, String end, String defaultValue) {
        int start_len = start.length();
        int start_pos = isEmpty(start) ? 0 : search.indexOf(start);
        if (start_pos > -1) {
            int end_pos = isEmpty(end) ? -1 : search.indexOf(end, start_pos + start_len);
            if (end_pos > -1)
                return search.substring(start_pos + start.length(), end_pos);
            else return search.substring(start_pos + start.length());
        }
        return defaultValue;
    }

    /**
     * 截取字符串
     *
     * @param search 待搜索的字符串
     * @param start  起始字符串 例如：<title>
     * @param end    结束字符串 例如：</title>
     * @return
     */
    public static String substring(String search, String start, String end) {
        return substring(search, start, end, "");
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input)) return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 汉字返回拼音，字母原样返回，都转换为小写
     *
     * @param input
     * @return
     */
    public static String getPinYin(String input) {
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(input);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 字符串text为空或者为“null”
     *
     * @param text
     * @return
     */
    public static boolean isEmptyOrNull(String text) {
        return TextUtils.isEmpty(text) || "null".equalsIgnoreCase(text);
    }

}
