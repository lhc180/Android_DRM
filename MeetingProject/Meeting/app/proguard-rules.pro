# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
# 是否使用大小写混合
-dontusemixedcaseclassnames

# 指定代码的压缩级别
-optimizationpasses 5

# 是否混淆第三方jar
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify

# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes SourceFile,LineNumberTable


# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService
-keep public class android.webkit.**

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keep public class * implements java.io.Serializable{
  public protected private *;
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

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}



# 保持泛型
-keepattributes Signature
-keepclasseswithmembers class * {
    *** *Callback(...);
}

# 保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 枚举类型
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 引用jar包
#-libraryjars /libs/android-support-v4.jar
#-libraryjars /libs/bcprov-jdk15-143.jar
#-libraryjars /libs/commons-net-3.3.jar
#-libraryjars /libs/fastjson-1.1.26.jar
#-libraryjars /libs/httpmime-4.1.1.jar
#-libraryjars /libs/universal-image-loader-1.9.4-src.jar
#-libraryjars /libs/jpush-sdk-release1.8.0.jar
#-libraryjars /libs/xUtils-2.6.14.jar
#-libraryjars /libs/android-core-3.1.1-SNAPSHOT.jar
#-libraryjars /libs/eventbus-2.4.0.jar
#-libraryjars /libs/ksoap2-android-assembly-3.0.0-jar-with-dependencies.jar
#-libraryjars /libs/nineoldandroids-2.4.0.jar
#-libraryjars /libs/rxjava-1.1.0.jar
#-libraryjars /libs/rxandroid-1.1.0.jar


# 第三方jar包中的类
-keep class android.support.v4.** { *; }
-keep class org.apache.commons.net.** {*;}
-keep class com.lidroid.xutils.** {*;}
-keep class org.bouncycastle.** {*;}
-keep class org.apache.http.** {*;}
-keep class com.nostra13.universalimageloader.** {*;}
-keep class com.alibaba.fastjson.** {*;}
-keep class cn.jpush.android.** {*;}
-keep class de.greenrobot.event.** {*;}
-keep class org.** {*;}
-keep class android.util.Xml.** {*;}



# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn javax.naming.**
-dontwarn junit.textui.**
-dontwarn java.awt.**
-dontwarn org.springframework.http.**
-dontwarn cn.jpush.**
-dontwarn org.**
-dontwarn sun.misc.**


-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# 使用反射类
-keep class * extends java.lang.annotation.Annotation { *; }
-keep class * extends java.lang.annotation.reflect { *; }
-keep class * extends java.lang.reflect { *; }

# 指定内容不混淆
-keep public class * extends net.sqlcipher.**

# eventbus不混淆onEvent**开头的method
-keepclassmembers class ** {
    public void onEvent*(**);
}

# 应用源代码
-keep class cn.com.pyc.drm.bean.** {*;}
-keep class cn.com.pyc.drm.model.** {*;}

# 第三方源代码
-keep class net.** {*;}
-keep class tv.danmaku.ijk.media.player.** {*;}
-keep class com.artifex.mupdfdemo.** {*;}
# -keep class com.google.zxing.** {*;}

# 内部类或者内部接口类的混淆配置
-keep class cn.com.pyc.drm.ui.MuPDFActivity$PdfPageSharedPreference{
        <fields>;
        <methods>;
}
-keep class cn.com.pyc.drm.ui.ScanLoginVerificationActivity$JavaScriptStartDownloadActivity{
        <fields>;
        <methods>;
}
-keep class cn.com.pyc.drm.ui.DownloadMainActivity$JavaScriptStartDownloadActivity{
        <fields>;
        <methods>;
}




# 如果项目用到jar的接口  声明一下
-keep interface com.lidroid.xutils.** {*; }
-keep interface com.nostra13.universalimageloader.** {*;}


