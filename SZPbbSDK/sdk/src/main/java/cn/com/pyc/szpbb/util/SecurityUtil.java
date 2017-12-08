package cn.com.pyc.szpbb.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * 安全加密工具类
 *
 * @author hudq
 */
public class SecurityUtil {

//    private static final String KEYGENERATOR_ALGORITHM = "AES";
//    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
//    private static final String CIPHER_PROVIDER = "BC";
//    private static final String IV = "0102030405060708";
    // 十六进制下数字到字符的映射数组
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 根据策略加盐生成指定的参数，再对结果进行md5处理
     *
     * @param salt
     * @param originStr
     * @return
     */
    public static String getEncodeMD5ByMD5(String salt, String originStr) {
        return encodeByMD5(encodeByMD5(originStr + "{" + salt + "}"));
    }

    /*
     * 根据策略(MD5(originStr+”{”+salt+”}”)加盐生成指定的参数
     *
     * @param originStr
     * @return
     */
//	public static String getDownloadMD5BySalt(String originStr)
//	{
//		String username = SZInitInterface.getUserName("");
//		String password = SZInitInterface.getPassWord("");
//
//		String pwd1 = encodeByMD5(password + "{" + username + "}");// pwd1
//		String pwd2 = encodeByMD5(pwd1 + "{" + username + "}");
//		return encodeByMD5(originStr + "{" + pwd2 + "}");
//	}

    /**
     * 对字符串进行MD5加密
     */
    public static String encodeByMD5(String originString) {
        if (originString != null) {
            try {
                // 创建具有指定算法名称的信息摘要
                // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                // 将得到的字节数组变成字符串返回
                return byteArrayToHexString(MessageDigest.getInstance("MD5")
                        .digest(originString.getBytes()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 转换字节数组为十六进制字符串
     *
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] bs) {
        StringBuilder resultSb = new StringBuilder();
        for (byte b : bs) {
            resultSb.append(byteToHexString(b));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * hexString 转 byte
     *
     * @param hexString
     * @return
     */
    public static byte[] toByteArray(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;

    }

    /**
     * BASE64 加密
     *
     * @param str
     * @return
     */
    public static String encryptBASE64(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        try {
            byte[] encode = str.getBytes("UTF-8");
            // base64 加密
            return new String(Base64.encode(encode, 0, encode.length,
                    Base64.DEFAULT), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * BASE64 解密
     *
     * @param str
     * @return
     */
    public static String decryptBASE64(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        try {
            byte[] encode = str.getBytes("UTF-8");
            // base64 解密
            return new String(Base64.decode(encode, 0, encode.length,
                    Base64.DEFAULT), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 解密资源文件
     *
     * @param filePath
     *            密文文件存放路径
     * @param enkey
     *            解密密钥
     * @throws cn.com.pyc.drm.common.AppException
     * @throws IOException
     */
//	public static OutputStream decFile(String filePath, String enkey)
//			throws Exception
//	{
//
//		BufferedInputStream bis = null;
//		BufferedOutputStream bos = null;
//		try
//		{
//			bis = new BufferedInputStream(new FileInputStream(
//					FileUtil.createFile(filePath)));
//			// 将密钥从hex字符串还原
//			SecretKeySpec key = new SecretKeySpec(toByte(new String(
//					enkey.getBytes(Fields.UTF_8))), KEYGENERATOR_ALGORITHM);
//			Security.addProvider(new BouncyCastleProvider());
//			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION,
//					CIPHER_PROVIDER);
//			IvParameterSpec iv = new IvParameterSpec(IV.getBytes(Fields.UTF_8));
//			cipher.init(Cipher.DECRYPT_MODE, key, iv);// 初始化
//
//			bos = new BufferedOutputStream(
//					new FileOutputStream(FileUtil.createFile(filePath
//							.substring(0, filePath.length()))));
//			int m = -1;
//			byte[] bytIn = new byte[4096 + 16];
//			while ((m = bis.read(bytIn)) != -1)
//			{
//				bos.write(cipher.doFinal(bytIn, 0, m));
//				bos.flush();
//			}
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e)
//		{
//			e.printStackTrace();
//		} catch (IllegalBlockSizeException e)
//		{
//			e.printStackTrace();
//		} catch (BadPaddingException e)
//		{
//			e.printStackTrace();
//		} catch (NoSuchProviderException e)
//		{
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e)
//		{
//			e.printStackTrace();
//		} catch (InvalidKeyException e)
//		{
//			e.printStackTrace();
//		} catch (InvalidAlgorithmParameterException e)
//		{
//			e.printStackTrace();
//		} finally
//		{
//			if (bis != null)
//			{
//				try
//				{
//					bis.close();
//				} catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			}
//			if (bos != null)
//			{
//				try
//				{
//					bos.close();
//				} catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			}
//		}
//
//		return bos;
//	}

}
