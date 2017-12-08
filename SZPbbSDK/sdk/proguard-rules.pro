-dontusemixedcaseclassnames
-optimizationpasses 5
-dontskipnonpubliclibraryclasses
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-dontpreverify
-verbose

-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes SourceFile,LineNumberTable,Deprecated
-keepattributes JavascriptInterface

-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver { *; }
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService
-keep public class android.webkit.**

-keep class * extends java.lang.annotation.Annotation { *; }
-keep class * extends java.lang.annotation.reflect { *; }
-keep class * extends java.lang.reflect { *; }

-dontwarn android.support.**
-dontwarn javax.naming.**
-dontwarn junit.textui.**
-dontwarn java.awt.**
-dontwarn org.springframework.http.**
-dontwarn com.alibaba.fastjson.**

# 不混淆工具jar的内容
# start region
-keep class net.sqlcipher.** {*;}
-keep class com.artifex.** {*;}
#-keep class * extends net.sqlcipher.**

-keep class com.alibaba.** {*;}
-keep class org.apache.** {*;}
-keep class org.xutils.** {*;}
-keep public class org.xutils.** {public protected *;}
-keep public interface org.xutils.** {public protected *;}
-keepclassmembers class * extends org.xutils.** {public protected *;}
-keepclassmembers @org.xutils.db.annotation.* class * {*;}
-keepclassmembers @org.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {@org.xutils.view.annotation.Event <methods>;}
# end region

#sdk中不混淆的内容
-keep class cn.com.pyc.szpbb.sdk.bean.SZBuildParams {*;}
-keep class cn.com.pyc.szpbb.sdk.bean.SZBuildParams$* {*;}
-keep class cn.com.pyc.szpbb.sdk.authentication.SZContentPermission {*;}
-keep class cn.com.pyc.szpbb.sdk.database.bean.* {*;}
-keep class cn.com.pyc.szpbb.sdk.response.** {*;}
-keep class cn.com.pyc.szpbb.sdk.models.* {*;}
-keep class cn.com.pyc.szpbb.sdk.callback.ISZCallBack{*;}
-keep class cn.com.pyc.szpbb.sdk.callback.ISZCallBackArray{*;}
#-keep class cn.com.pyc.szpbb.sdk.callback.SZCommonCallbackEngine{*;}
-keep class cn.com.pyc.szpbb.sdk.SZInitInterface {*;}
-keep class cn.com.pyc.szpbb.sdk.SZDBInterface {*;}
-keep class cn.com.pyc.szpbb.sdk.SZHttpInterface {*;}
-keep class cn.com.pyc.szpbb.sdk.SZFileInterface {*;}
-keep class cn.com.pyc.szpbb.sdk.SZDownloadInterface {*;}

-keep class cn.com.pyc.szpbb.util.SZLog {*;}
-keep class cn.com.pyc.szpbb.util.SecurityUtil {*;}
-keep class cn.com.pyc.szpbb.util.GenericsUtil {*;}
-keep class cn.com.pyc.szpbb.util.FormatUtil {*;}

-keep class cn.com.pyc.szpbb.common.Fields{*;}
-keep class cn.com.pyc.szpbb.common.Code{*;}
-keep class cn.com.pyc.szpbb.common.DownloadState{*;}
#-keep class cn.com.pyc.szpbb.common.Constant{public static <fields>;}

-keep class cn.com.pyc.szpbb.coreui.**{*;}


-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keep public class * implements java.io.Serializable{
  public protected private *;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
-keepclassmembers public interface * extends android.view.View {
   void set*(***);
   *** get*();
}
-keepclassmembers public class * extends android.content.BroadcastReceiver{
	protected abstract void *(***);
	public void on*(***);
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
