package cn.com.pyc.drm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import cn.com.pyc.drm.common.App;

/**
 * sharedPreference保存数据
 *
 * @author hudq
 */
public class SPUtils {

	/*
     * 存储数据的的key值
	 */
    // <br/>
    // <br/>

    public SPUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 将数据保存到SharedPreferences
     *
     * @return
     */
    public static boolean save(String key, Object obj) {

        Context context = App.getInstance();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        if (obj instanceof String) {
            editor.putString(key, String.valueOf(obj));
        }
        if (obj instanceof Integer) {
            editor.putInt(key, Integer.valueOf(String.valueOf(obj)));
        }
        if (obj instanceof Boolean) {
            editor.putBoolean(key, Boolean.valueOf(String.valueOf(obj)));
        }
        if (obj instanceof Float) {
            editor.putFloat(key, Float.valueOf(String.valueOf(obj)));
        }
        if (obj instanceof Long) {
            editor.putLong(key, Long.valueOf(String.valueOf(obj)));
        }
        if (obj instanceof Set) {
            editor.putStringSet(key, (Set<String>) (obj));
        }
        return SharedPreferencesCompat.apply(editor);
    }


//    public static boolean saveSync(Context context, String key, Object obj) {
//        Context ctx = App.getInstance();
//        if (ctx == null) ctx = context;
//        Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
//
//        if (obj instanceof String) {
//            editor.putString(key, String.valueOf(obj));
//        }
//        if (obj instanceof Integer) {
//            editor.putInt(key, Integer.valueOf(String.valueOf(obj)));
//        }
//        if (obj instanceof Boolean) {
//            editor.putBoolean(key, Boolean.valueOf(String.valueOf(obj)));
//        }
//        if (obj instanceof Float) {
//            editor.putFloat(key, Float.valueOf(String.valueOf(obj)));
//        }
//        if (obj instanceof Long) {
//            editor.putLong(key, Long.valueOf(String.valueOf(obj)));
//        }
//        return editor.commit();
//    }

    /**
     * 从SharedPreferences中取出数据
     *
     * @param key
     * @return
     */
//	public static Object get(String key)
//	{
//		Context context = App.getInstance();
//		return PreferenceManager.getDefaultSharedPreferences(context).getAll().get(key);
//	}
    public static Object get(String key, Object defaultObject) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        } else if (defaultObject instanceof Set) {
            return sp.getStringSet(key, (Set<String>) defaultObject);
        }
        return defaultObject;
    }

//    public static Object getSync(Context context, String key, Object defaultObject) {
//        Context ctx = App.getInstance();
//        if (ctx == null) ctx = context;
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
//
//        if (defaultObject instanceof String) {
//            return sp.getString(key, (String) defaultObject);
//        } else if (defaultObject instanceof Integer) {
//            return sp.getInt(key, (Integer) defaultObject);
//        } else if (defaultObject instanceof Boolean) {
//            return sp.getBoolean(key, (Boolean) defaultObject);
//        } else if (defaultObject instanceof Float) {
//            return sp.getFloat(key, (Float) defaultObject);
//        } else if (defaultObject instanceof Long) {
//            return sp.getLong(key, (Long) defaultObject);
//        } else if (defaultObject instanceof Set) {
//            return sp.getStringSet(key, (Set<String>) defaultObject);
//        }
//        return defaultObject;
//    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    public static boolean remove(String key) {
        Context context = App.getInstance();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(key);
        return SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     */
    public static boolean clear() {
        Context context = App.getInstance();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.clear();
        return SharedPreferencesCompat.apply(editor);
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     */
    public static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static boolean apply(Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return true;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            return editor.commit();
        }
    }

}