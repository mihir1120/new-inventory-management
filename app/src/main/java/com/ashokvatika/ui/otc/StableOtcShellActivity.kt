package com.ashokvatika.ui.otc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.updatePadding
import com.ashokvatika.app.R

class StableOtcShellActivity : ComponentActivity() {

  private lateinit var root: ConstraintLayout
  private lateinit var header: ConstraintLayout
  private lateinit var scrollContent: LinearLayout
  private lateinit var backButton: ImageButton
  private lateinit var cartButton: ImageButton
  private lateinit var closedButton: ImageButton

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContentView(R.layout.activity_otc_stable_shell)

    root = findViewById(R.id.otcRoot)
    header = findViewById(R.id.otcHeader)
    scrollContent = findViewById(R.id.otcScrollContent)
    backButton = findViewById(R.id.actionBack)
    cartButton = findViewById(R.id.actionCart)
    closedButton = findViewById(R.id.actionClosed)

    installInsetsHandling()
    installActions()
    populateDemoContent()
  }

  private fun installInsetsHandling() {
    val headerBasePaddingTop = header.paddingTop
    val scrollBasePaddingBottom = findViewById<View>(R.id.otcScrollView).paddingBottom
    val contentBasePaddingBottom = scrollContent.paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
      val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      header.updatePadding(
        left = 16.dp,
        top = headerBasePaddingTop + bars.top,
        right = 16.dp,
        bottom = 8.dp,
      )
      findViewById<View>(R.id.otcScrollView).updatePadding(
        left = 0,
        top = 0,
        right = 0,
        bottom = scrollBasePaddingBottom + bars.bottom,
      )
      scrollContent.updatePadding(
        left = scrollContent.paddingLeft,
        top = scrollContent.paddingTop,
        right = scrollContent.paddingRight,
        bottom = contentBasePaddingBottom + bars.bottom,
      )
      insets
    }
    ViewCompat.requestApplyInsets(root)
  }

  private fun installActions() {
    backButton.setOnClickListener { finish() }
    cartButton.setOnClickListener {
      setActionVisibility(showCart = true, showClosed = true)
    }
    closedButton.setOnClickListener {
      setActionVisibility(showCart = true, showClosed = true)
    }
  }

  fun setActionVisibility(
    showCart: Boolean,
    showClosed: Boolean,
  ) {
    cartButton.isInvisible = !showCart
    closedButton.isInvisible = !showClosed
  }

  private fun populateDemoContent() {
    val cards = listOf(
      getString(R.string.otc_demo_title_main) to getString(R.string.otc_demo_body_main),
      getString(R.string.otc_demo_title_cart) to getString(R.string.otc_demo_body_cart),
      getString(R.string.otc_demo_title_closed) to getString(R.string.otc_demo_body_closed),
    )
    val inflater = LayoutInflater.from(this)
    cards.forEach { (title, body) ->
      val card = inflater.inflate(R.layout.item_otc_demo_card, scrollContent, false)
      card.findViewById<TextView>(R.id.demoCardTitle).text = title
      card.findViewById<TextView>(R.id.demoCardBody).text = body
      scrollContent.addView(card)
    }
  }

  private val Int.dp: Int
    get() = (this * resources.displayMetrics.density).toInt()
}
