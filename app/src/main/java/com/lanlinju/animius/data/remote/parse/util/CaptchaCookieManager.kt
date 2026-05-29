package com.lanlinju.animius.data.remote.parse.util

import android.content.Context
import android.webkit.CookieManager
import androidx.core.content.edit
import com.lanlinju.animius.application.AnimeApplication
import com.lanlinju.animius.util.SourceHolder

/**
 * 验证码 Cookie 管理器
 * 用于存储和获取验证码验证后的 Cookie
 */
object CaptchaCookieManager {
    val CUR_KEY_COOKIE: String
        get() {
            return SourceHolder.currentSourceMode.name + "_Cookie"
        }

    private const val PREF_NAME = "captcha_cookies"

    /**
     * 检测到需要验证码时的 URL，供 ViewModel 读取
     */
    var captchaUrl: String = ""

    private val prefs by lazy {
        AnimeApplication.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存验证码 Cookie（以 URL 为 key）
     */
    fun saveCookies(key: String, cookies: String) {
        prefs.edit { putString(key, cookies) }
    }

    /**
     * 获取保存的验证码 Cookie
     */
    fun getCookies(key: String): String {
        return prefs.getString(key, "") ?: ""
    }

    /**
     * 清除指定 URL 的验证码 Cookie
     */
    fun clearCookies(key: String) {
        prefs.edit { remove(key) }
    }

    /**
     * 从 WebView CookieManager 同步 Cookie 到本地存储
     */
    fun syncFromWebView(url: String) {
        val cookies = CookieManager.getInstance().getCookie(url) ?: ""
        if (cookies.isNotEmpty()) {
            saveCookies(url, cookies)
        }
    }

}