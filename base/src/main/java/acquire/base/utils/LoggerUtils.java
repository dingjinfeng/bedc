package acquire.base.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Print log utils
 *
 * @author Janson
 * @date 2020/5/25 11:11
 */
public class LoggerUtils {
    private static String customTagPrefix="";
    private static boolean allowV = false;
    private static boolean allowD = false;
    private static boolean allowI = false;
    private static boolean allowW = false;
    private static boolean allowE = false;

    /**
	 * Open log
	 * @param isDebug open if true,else close
	 */
	public static void configPrint(boolean isDebug){
		allowD = isDebug;
		allowE = isDebug;
		allowI = isDebug;
		allowV = isDebug;
		allowW = isDebug;
    }

    public static void setAllowV(boolean allowV) {
        LoggerUtils.allowV = allowV;
    }

    public static void setAllowD(boolean allowD) {
        LoggerUtils.allowD = allowD;
    }

    public static void setAllowI(boolean allowI) {
        LoggerUtils.allowI = allowI;
    }

    public static void setAllowW(boolean allowW) {
        LoggerUtils.allowW = allowW;
    }

    public static void setAllowE(boolean allowE) {
        LoggerUtils.allowE = allowE;
    }


    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    public static void setCustomTagPrefix(String customTagPrefix) {
        LoggerUtils.customTagPrefix = customTagPrefix;
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(Locale.getDefault(),tag, callerClazzName, dealLamadaMethod(caller.getMethodName()), caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    public static void v(String content) {
        if (!allowV){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        Log.v(tag, content);
    }

    public static void d(String content) {
        if (!allowD) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        Log.d(tag, content);
    }


    public static void i(String content) {
        if (!allowI) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        Log.i(tag, content);
    }

    public static void w(String content) {
        if (!allowW) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        Log.w(tag, content);
    }

    public static void w(String content,Throwable tr ) {
        if (!allowW) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        Log.w(tag, content, tr);
    }
    public static void e(String content) {
        if (!allowE){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        Log.e(tag, content);
    }

    public static void e(String content,Throwable tr) {
        if (!allowE){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        Log.e(tag, content, tr);
    }

    /**
     * Deal lamada method name
     * @param methodName method name
     * @return method name after deal
     */
    private static String dealLamadaMethod(String methodName){
        if (methodName.startsWith("lambda$")){
            String[]strs = methodName.split("\\$");
            if (strs.length>1){
                methodName = strs[1];
            }
        }
        return methodName;
    }
}
