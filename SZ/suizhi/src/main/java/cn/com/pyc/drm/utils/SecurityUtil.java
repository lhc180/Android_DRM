package cn.com.pyc.drm.utils;

import java.security.MessageDigest;

/**
 * 加密工具类
 *
 * @author hudq
 */
public class SecurityUtil {

    //private static final String KEYGENERATOR_ALGORITHM = "AES";
    //private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
    //private static final String CIPHER_PROVIDER = "BC";
    //private static final String IV = "0102030405060708";
    // 十六进制下数字到字符的映射数组
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 根据策略加盐生成指定的参数，再对结果进行md5处理
     *
     * @param salt      盐值
     * @param originStr 原始字符串String
     * @return String
     */
    public static String getParamByMD5(String salt, String originStr) {
        return encodeByMD5(encodeByMD5(originStr + "{" + salt + "}"));
    }

    /*
     * 根据策略(MD5(originStr+”{”+salt+”}”)加盐生成指定的参数,下载文件用
     *
     * @param originStr 原始字符串String
     * @return String
     */
//    public static String getDownloadMD5BySalt(String originStr) {
//        String username = Constant.getUserName();
//        String password = Constant.getPassWord();
//
//        String pwd1 = encodeByMD5(password + "{" + username + "}");// pwd1
//        String pwd2 = encodeByMD5(pwd1 + "{" + username + "}");
//        return encodeByMD5(originStr + "{" + pwd2 + "}");
//    }

    /*
     * 账号密码MD5加盐加密(2次)
     *
     * @param userName
     * @return
     */
//    public static String getAccountMD5BySalt(String userName) {
//        String password = Constant.getPassWord();
//        String pwd1 = encodeByMD5(password + "{" + userName + "}");// pwd1
//        return encodeByMD5(pwd1 + "{" + userName + "}");
//    }

    /**
     * MD5加盐
     *
     * @param originStr 加密字符串
     * @param salt      盐值
     * @return
     */
    public static String encodeMD5BySalt(String originStr, String salt) {
        return encodeByMD5(originStr + "{" + salt + "}");
    }
    /*public static String encodeMD5BySalt1(String originStr) {
        originStr
        return encodeByMD5(originStr + "{" + "" + "}");
    }*/

    /**
     * 对字符串进行MD5加密
     *
     * @param originString 原始字符串String
     * @return String
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
        return "";
    }

    /*
     * 解密资源文件
     *
     * @param filepath 密文文件存放路径
     * @param enkey    解密密钥
     * @throws Exception
     */
//    @Deprecated
//    public static void dec(String filepath, String enkey) throws Exception {
//
//        BufferedInputStream bis = null;
//        BufferedOutputStream bos = null;
//        try {
//            bis = new BufferedInputStream(new FileInputStream(
//                    FileUtils.createFile(filepath)));
//            // 将密钥从hex字符串还原
//            SecretKeySpec key = new SecretKeySpec(toByte(new String(
//                    enkey.getBytes(DrmPat.UTF_8))), KEYGENERATOR_ALGORITHM);
//            Security.addProvider(new BouncyCastleProvider());
//            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION,
//                    CIPHER_PROVIDER);
//            IvParameterSpec iv = new IvParameterSpec(IV.getBytes(DrmPat.UTF_8));
//            cipher.init(Cipher.DECRYPT_MODE, key, iv);// 初始化
//
//            bos = new BufferedOutputStream(new FileOutputStream(
//                    FileUtils.createFile(filepath.substring(0,
//                            filepath.length()))));
//            int m = -1;
//            byte[] bytIn = new byte[4096 + 16];
//            while ((m = bis.read(bytIn)) != -1) {
//                bos.write(cipher.doFinal(bytIn, 0, m));
//                bos.flush();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            //throw AppException.io(e);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            //throw AppException.cipher(e);
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        } finally {
//            if (bis != null) {
//                try {
//                    bis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (bos != null) {
//                try {
//                    bos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    /**
     * 转换字节数组为十六进制字符串
     *
     * @param b 字节
     * @return 十六进制字符串String
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     *
     * @param b 字节
     * @return 十六进制字符串String
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * hexString 转 byte[]
     *
     * @param hexString 十六进制形式的字符串
     * @return 字节byte
     */
    public static byte[] hexStringtoByteArray(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    /*
     * 获取指定文件的MD5
     *
     * @param file 指定的文件
     * @return String
     */
//    public static String getMD5ByFile(File file) {
//        String value = null;
//        FileInputStream in = null;
//        try {
//            in = new FileInputStream(file);
//            MappedByteBuffer byteBuffer = in.getChannel().map(
//                    FileChannel.MapMode.READ_ONLY, 0, file.length());
//            MessageDigest md5 = MessageDigest.getInstance("MD5");
//            md5.update(byteBuffer);
//            BigInteger bi = new BigInteger(1, md5.digest());
//            value = bi.toString(16);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (null != in) {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return value;
//    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }


    /**
     * * @param hexString
     * * @return 将十六进制转换为字节数组
     */
    public static byte[] hexStringToBinary(String hexString) {
        //hexString的长度对2取整，作为bytes的长度
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;//字节高四位
        byte low = 0;//字节低四位
        for (int i = 0; i < len; i++) {
            //右移四位得到高位
            high = (byte) ((hexString.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexString.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);//高地位做或运算
        }
        return bytes;
    }

}
