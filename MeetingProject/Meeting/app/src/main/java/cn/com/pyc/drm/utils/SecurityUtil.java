package cn.com.pyc.drm.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.common.DrmPat;

/**
 * 安全加密工具类
 * 
 * @author hudq
 * 
 */
public class SecurityUtil
{
	private static final String KEYGENERATOR_ALGORITHM = "AES";
	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
	private static final String CIPHER_PROVIDER = "BC";
	private static final String IV = "0102030405060708";
	// 十六进制下数字到字符的映射数组
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/**
	 * 解密资源文件
	 * 
	 * @param filepath
	 *            密文文件存放路径
	 * @param enkey
	 *            解密密钥
	 * @throws cn.com.pyc.drm.common.AppException
	 * @throws IOException
	 */
	public static void dec(String filepath, String enkey) throws AppException
	{

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try
		{
			bis = new BufferedInputStream(new FileInputStream(FileUtils.createFile(filepath)));
			// 将密钥从hex字符串还原
			SecretKeySpec key = new SecretKeySpec(toByte(new String(enkey.getBytes(DrmPat.UTF_8))), KEYGENERATOR_ALGORITHM);
			Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION, CIPHER_PROVIDER);
			IvParameterSpec iv = new IvParameterSpec(IV.getBytes(DrmPat.UTF_8));
			cipher.init(Cipher.DECRYPT_MODE, key, iv);// 初始化

			bos = new BufferedOutputStream(new FileOutputStream(FileUtils.createFile(filepath.substring(0, filepath.length()))));
			int m = -1;
			byte[] bytIn = new byte[4096 + 16];
			while ((m = bis.read(bytIn)) != -1)
			{
				bos.write(cipher.doFinal(bytIn, 0, m));
				bos.flush();
			}
		} catch (IOException e)
		{
			throw AppException.io(e);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			throw AppException.cipher(e);
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		} catch (NoSuchProviderException e)
		{
			e.printStackTrace();
		} catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e)
		{
			e.printStackTrace();
		} finally
		{
			if (bis != null)
			{
				try
				{
					bis.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (bos != null)
			{
				try
				{
					bos.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 对字符串进行MD5加密
	 */
	public static String encodeByMD5(String originString)
	{
		if (originString != null)
		{
			try
			{
				// 创建具有指定算法名称的信息摘要
				// 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
				// 将得到的字节数组变成字符串返回
				return byteArrayToHexString(MessageDigest.getInstance("MD5").digest(originString.getBytes()));
			} catch (Exception ex)
			{
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
	private static String byteArrayToHexString(byte[] b)
	{
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++)
		{
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}

	/**
	 * 将一个字节转化成十六进制形式的字符串
	 */
	private static String byteToHexString(byte b)
	{
		int n = b;
		if (n < 0)
			n = 256 + n;
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
	public static byte[] toByte(String hexString)
	{
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
		return result;

	}

}
