package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

/**
 * 专辑 权限表
 * 
 * @author lixiangyang
 * 
 */
public class AssetColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "asset";

	public static final String RIGHTSOBJECT_ID = "rightsobject_id";
	public static final String ASSET_UID = "asset_uid ";
	public static final String DIGEST_METHOD = "digest_method ";
	public static final String DIGEST_VALUE = "digest_value ";
	public static final String CEK_ENCRYPT_METHOD = "cek_encrypt_method ";
	public static final String CEK_RETRIEVAL_KEY = "cek_retrieval_key ";
	public static final String CEK_CIPHER_VALUE = "cek_cipher_value ";
	public static final String USERNAME = "username";
	public static final String CREATE_TIME = "create_time";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(_ID, "integer  ");
		mColumnMap.put(USERNAME, "varchar");
		mColumnMap.put(RIGHTSOBJECT_ID, "varchar ");
		mColumnMap.put(ASSET_UID, "varchar");
		mColumnMap.put(DIGEST_METHOD, "varchar ");
		mColumnMap.put(DIGEST_VALUE, "varchar ");
		mColumnMap.put(CEK_ENCRYPT_METHOD, "varchar ");
		mColumnMap.put(CEK_RETRIEVAL_KEY, "varchar ");
		mColumnMap.put(CEK_CIPHER_VALUE, "varchar ");
		mColumnMap.put(USERNAME, "varchar ");
		mColumnMap.put(CREATE_TIME, "DOUBLE");
	}

	@Override
	public String getTableName()
	{
		return TABLE_NAME;
	}

	@Override
	protected Map<String, String> getTableMap()
	{
		return mColumnMap;
	}

}
