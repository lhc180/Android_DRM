package cn.com.pyc.szpbb.sdk.models.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储证书信息的类 oex-rights
 */
public class OEX_Rights {
    private String assent_id;
    private Map<String, String> contextMap = new HashMap<String, String>();
    private OEX_Agreement agreement;

    public String getAssent_id() {
        return assent_id;
    }

    public void setAssent_id(String assent_id) {
        this.assent_id = assent_id;
    }

    public Map<String, String> getContextMap() {
        return contextMap;
    }

    public void setContextMap(String key, String value) {
        if (!isEmpty(key) && !isEmpty(value)) {
            contextMap.put(key, value);
        }
    }

    public OEX_Agreement getAgreement() {
        return agreement;
    }

    public void setAgreement(OEX_Agreement agreement) {
        this.agreement = agreement;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    private static boolean isEmpty(String input) {
        if (input == null || input.length() == 0) return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }
}
