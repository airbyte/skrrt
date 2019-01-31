package uk.airbyte.skrrt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView


class RapperDetailActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_URL = "url"

        fun newIntent(context: Context, rapper: Rapper): Intent {
            val detailIntent = Intent(context, RapperDetailActivity::class.java)

            detailIntent.putExtra(EXTRA_TITLE, rapper.name)
            detailIntent.putExtra(EXTRA_URL, "https://en.wikipedia.org/wiki/" + rapper.name)

            return detailIntent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rapper_detail)

        val title = intent.extras.getString(EXTRA_TITLE)
        val url = intent.extras.getString(EXTRA_URL)

        setTitle(title)

        webView = findViewById(R.id.detail_web_view)

        webView.loadUrl(url)
    }
}
