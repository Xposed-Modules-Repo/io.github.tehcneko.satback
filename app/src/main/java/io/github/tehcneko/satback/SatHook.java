package io.github.tehcneko.satback;

import android.graphics.ColorMatrix;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SatHook implements IXposedHookLoadPackage {
    private final ArrayList<Class<?>> hookedClient = new ArrayList<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(ColorMatrix.class, "setSaturation", float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Objects.equals(0.0f, param.args[0])) {
                    param.setResult(null);
                }
            }
        });
        XposedHelpers.findAndHookMethod(WebView.class, "setWebViewClient", WebViewClient.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (BuildConfig.DEBUG) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
                Class<?> clazz = param.args[0].getClass();
                if (hookedClient.contains(clazz)) return;
                XposedHelpers.findAndHookMethod(clazz, "onPageFinished", WebView.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        WebView webView = (WebView) param.args[0];
                        webView.evaluateJavascript("(function(){" +
                                "var style = document.createElement('style');" +
                                "style.innerHTML = '* {filter: none !important}';" +
                                "document.head.appendChild(style);" +
                                "})();", null);
                    }
                });
                hookedClient.add(clazz);
            }
        });
    }
}
