package com.cepda.firetv.launcherwrapper

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.Presenter
import com.cepda.firetv.launcherwrapper.model.AppItem

class MainLauncherActivity : BrowseSupportFragment() {

    private lateinit var appList: List<AppItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appRepository = AppRepository(requireContext())
        appList = appRepository.getLeanbackUserApps()

        setupUI()
        addSupportOverlay()
    }

    private fun setupUI() {
        title = getString(R.string.app_name)
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false

        val cardPresenter = AppCardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter).apply {
            appList.forEach { add(it) }
        }

        adapter = ArrayObjectAdapter(ListRowPresenter()).apply {
            add(ListRow(HeaderItem("Mis Aplicaciones"), listRowAdapter))
        }
    }

    private fun addSupportOverlay() {
        val overlay = TextView(requireContext()).apply {
            text = getString(R.string.support_email)
            setTextColor(ContextCompat.getColor(context, R.color.db_text_secondary))
            textSize = 12f
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            setPadding(0, 0, 48, 24)
            setBackgroundColor(Color.TRANSPARENT)
        }

        activity?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)?.addView(overlay)
    }

    inner class AppCardPresenter : Presenter() {

        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val cardView = ImageCardView(parent.context).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                infoAreaBackgroundColor = ContextCompat.getColor(context, R.color.db_accent_blue)
            }
            return ViewHolder(cardView)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val app = item as AppItem
            val cardView = viewHolder.view as ImageCardView
            cardView.titleText = app.label
            cardView.mainImage = app.icon
            cardView.setMainImageDimensions(320, 180)

            cardView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    cardView.setMainImageDimensions(340, 200)
                } else {
                    cardView.setMainImageDimensions(320, 180)
                }
            }
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {
            val cardView = viewHolder.view as ImageCardView
            cardView.badgeImage = null
            cardView.mainImage = null
        }
    }

    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder?, item: Any?, rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {
        if (item is AppItem) {
            AppLauncher.launchApp(requireContext(), item.componentName)
        }
    }
}