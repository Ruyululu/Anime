package com.lanlinju.animius.presentation.screen.captcha

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lanlinju.animius.data.remote.parse.util.CaptchaCookieManager
import com.lanlinju.animius.presentation.theme.AnimeTheme

class CaptchaWebViewActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_COOKIES = "extra_cookies"

        fun createIntent(context: Context, url: String): Intent {
            return Intent(context, CaptchaWebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val url = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }

        setContent {
            AnimeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("验证码验证") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    CaptchaWebViewContent(
                        url = url,
                        onVerificationComplete = { cookies ->
                            // 保存 Cookie 到本地存储
                            CaptchaCookieManager.saveCookies(
                                CaptchaCookieManager.CUR_KEY_COOKIE,
                                cookies
                            )
                            val resultIntent = Intent().apply {
                                putExtra(EXTRA_COOKIES, cookies)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CaptchaWebViewContent(
    url: String,
    onVerificationComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // 页面加载完成，等待用户手动操作
                        }
                    }

                    loadUrl(url)
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // 用户完成验证码后点击此按钮
        Button(
            onClick = {
                val currentUrl = webView?.url ?: url
                val cookies = CookieManager.getInstance().getCookie(currentUrl) ?: ""
                onVerificationComplete(cookies)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("已完成验证")
        }
    }
}