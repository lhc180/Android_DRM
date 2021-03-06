package cn.com.pyc.drm.model.db;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Map;

public abstract class DatabaseColumn implements BaseColumns
{

	/**
	 * Classes's name extends from this class.
	 */
	public static final String[] SUBCLASSES = new String[] { 
		AccountColumn.class.getName(), 
		AlbumInfoColumn.class.getName(), 
		BookMarkColumn.class.getName(),
		ContentColumn.class.getName() };

	// AssetColumn.class.getName(),
	// Per_AttributeColumn.class.getName(),
	// Per_ConstraintColumn.class.getName(),
	// PermissionColumn.class.getName(),
	// RightsColumn.class.getName()};

	public String getTableCreateor()
	{
		return getTableCreator(getTableName(), getTableMap());
	}

	/**
	 * Get sub-classes of this class.
	 * 
	 * @return Array of sub-classes.
	 */
	@SuppressWarnings("unchecked")
	public static final Class<DatabaseColumn>[] getSubClasses()
	{
		ArrayList<Class<DatabaseColumn>> classes = new ArrayList<Class<DatabaseColumn>>();
		Class<DatabaseColumn> subClass = null;
		for (int i = 0; i < SUBCLASSES.length; i++)
		{
			try
			{
				subClass = (Class<DatabaseColumn>) Class.forName(SUBCLASSES[i]);
				classes.add(subClass);
			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
				continue;
			}
		}
		return classes.toArray(new Class[0]);
	}

	/**
	 * Create a sentence to create a table by using a hash-map.
	 * 
	 * @param tableName
	 *            The table's name to create.
	 * @param map
	 *            A map to store table columns info.
	 * @return
	 */
	private static final String getTableCreator(String tableName, Map<String, String> map)
	{
		String[] keys = map.keySet().toArray(new String[0]);
		String value = null;
		StringBuilder creator = new StringBuilder();
		creator.append("CREATE TABLE ").append(tableName).append("( ");
		int length = keys.length;
		for (int i = 0; i < length; i++)
		{
			value = map.get(keys[i]);
			creator.append(keys[i]).append(" ");
			creator.append(value);
			if (i < length - 1)
			{
				creator.append(",");
			}
		}
		creator.append(")");
		return creator.toString();
	}

	abstract public String getTableName();

	abstract protected Map<String, String> getTableMap();

}
