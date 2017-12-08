# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
# 不使用大小写混合
-dontusemixedcaseclassnames

# 指定代码的压缩级别
-optimizationpasses 5
# 不去忽略非公共类库
-dontskipnonpubliclibraryclasses
# 混淆时是否记录日志
-verbose

# 不优化输入的类文件
-dontoptimize
# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).

# 混淆时不做预校验
-dontpreverify
# 忽略警告，避免打包时某些警告出现
# -ignorewarnings

# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes Signature,*Annotation*
-keepattributes Exceptions,InnerClasses
-keepattributes SourceFile,LineNumberTable,Deprecated
-keepattributes JavascriptInterface


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
-keep public class * extends android.support.**
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService
-keep public class android.webkit.**

-keep public class * extends java.lang.Throwable {*;}
-keep public class * extends java.lang.Exception {*;}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
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

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
# 忽略警告
-dontwarn android.support.**
-dontwarn javax.naming.**
-dontwarn junit.textui.**
-dontwarn java.awt.**
-dontwarn org.springframework.http.**
-dontwarn cn.jpush.**

# 使用反射类
-keep class * extends java.lang.annotation.Annotation { *; }
-keep class * extends java.lang.annotation.reflect { *; }
-keep class * extends java.lang.reflect { *; }

# 不混淆第三方jar包中的类
-keep class android.support.** { *; }
-keep class cn.jpush.** {*;}
-keep class org.apache.** {*;}
-keep class org.bouncycastle.** {*;}
-keep class com.alibaba.fastjson.** {*;}
-keep class net.sqlcipher.** {*;}
-keep class com.google.** {*;}

# EventBus,不混淆onEvent**开头的method
-keep class de.greenrobot.event.** {*;}
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}

# 指定内容不混淆
-keep public class * extends net.sqlcipher.**
-keep class cn.com.pyc.drm.model.** {*;}
-keep class cn.com.pyc.drm.bean.event.** {*;}
-keep class tv.danmaku.ijk.media.player.** {*;}
-keep class com.artifex.mupdfdemo.** {*;}
-keep class cn.com.pyc.loger.** {*;}
-keep class cn.com.pyc.drm.utils.manager.ShareMomentEngine {*;}
-keep class cn.com.pyc.drm.ui.BrowserActivity$* {*;}

# 微信
-keep class com.tencent.mm.opensdk.** {*;}
-keep class com.tencent.wxop.** {*;}
-keep class com.tencent.mm.sdk.** {*;}

# QQ
-keep class com.tencent.connect** {*;}
-keep class com.tencent.open** {*;}
-keep class com.tencent.tauth** {*;}
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
#-keep class com.tencent.open.PKDialog
#-keep class com.tencent.open.PKDialog {*;}
#-keep class com.tencent.open.PKDialog$*
#-keep class com.tencent.open.PKDialog$* {*;}

# xUtils3
-keep public class org.xutils.** {
    public protected *;
}
-keep public interface org.xutils.** {
    public protected *;
}
-keepclassmembers class * extends org.xutils.** {
    public protected *;
}
-keepclassmembers @org.xutils.db.annotation.* class * {*;}
-keepclassmembers @org.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @org.xutils.view.annotation.Event <methods>;
}

# AVLoading
-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

# ButterKnife
-dontwarn butterknife.**
-keep class butterknife.*
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# AliPay
-dontwarn com.alipay.**
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.sdk.app.H5PayCallback {
    <fields>;
    <methods>;
}
-keep class com.alipay.android.phone.mrpc.core.** { *; }
-keep class com.alipay.apmobilesecuritysdk.** { *; }
-keep class com.alipay.mobile.framework.service.annotation.** { *; }
-keep class com.alipay.mobilesecuritysdk.face.** { *; }
-keep class com.alipay.tscenter.biz.rpc.** { *; }
-keep class org.json.alipay.** { *; }
-keep class com.alipay.tscenter.** { *; }
-keep class com.ta.utdid2.** { *;}
-keep class com.ut.device.** { *;}

# sina weibo
-dontwarn com.sina.weibo.sdk.**
-keep class com.sina.weibo.sdk.** { *; }