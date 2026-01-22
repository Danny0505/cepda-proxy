package com.cepda.firetv.launcherwrapper

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.cepda.firetv.launcherwrapper.model.AppItem

class MainLauncherFragment : BrowseSupportFragment() {

    companion object {
        private const val TAG = "MainLauncherFragment"
    }

    private var appList: List<AppItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando fragment")
        
        // Configurar título y encabezados en onCreate
        title = getString(R.string.app_name)
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        
        Log.d(TAG, "onCreate: Título y headers configurados")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated: Cargando apps")

        // Cargar apps
        val appRepository = AppRepository(requireContext())
        appList = appRepository.getLeanbackUserApps()
        
        Log.d(TAG, "onActivityCreated: Apps cargadas = ${appList.size}")
        appList.forEachIndexed { index, app ->
            Log.d(TAG, "  [$index] ${app.label}")
        }

        setupRowAdapter()
        setupEventListeners()
        addSupportOverlay()
        
        Log.d(TAG, "onActivityCreated: UI configurada")
    }

    private fun setupRowAdapter() {
        Log.d(TAG, "setupRowAdapter: Creando adapter con ${appList.size} apps")
        
        val cardPresenter = AppCardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        
        // Agregar cada app al adapter
        appList.forEach { app ->
            Log.d(TAG, "setupRowAdapter: Agregando ${app.label}")
            listRowAdapter.add(app)
        }
        
        Log.d(TAG, "setupRowAdapter: ListRowAdapter tiene ${listRowAdapter.size()} items")

        // Crear el row adapter principal
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val header = HeaderItem(0, "Mis Aplicaciones")
        rowsAdapter.add(ListRow(header, listRowAdapter))
        
        // Asignar al fragment
        adapter = rowsAdapter
        
        Log.d(TAG, "setupRowAdapter: Adapter asignado al fragment")
    }

    private fun setupEventListeners() {
        Log.d(TAG, "setupEventListeners: Configurando listener de clicks")
        
        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            Log.d(TAG, "onItemClicked: Click detectado en item: $item")
            if (item is AppItem) {
                Log.d(TAG, "onItemClicked: Lanzando app ${item.label}")
                AppLauncher.launchApp(requireContext(), item.componentName)
            }
        }
    }

    private fun addSupportOverlay() {
        Log.d(TAG, "addSupportOverlay: Agregando overlay de soporte")
        
        try {
            val overlay = TextView(requireContext()).apply {
                text = getString(R.string.support_email)
                setTextColor(ContextCompat.getColor(context, R.color.db_text_secondary))
                textSize = 12f
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                setPadding(0, 0, 48, 24)
                setBackgroundColor(Color.TRANSPARENT)
            }

            activity?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)?.addView(overlay)
            Log.d(TAG, "addSupportOverlay: Overlay agregado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "addSupportOverlay: Error al agregar overlay", e)
        }
    }

    inner class AppCardPresenter : Presenter() {

        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            Log.d(TAG, "AppCardPresenter.onCreateViewHolder: Creando card view")
            
            val cardView = ImageCardView(parent.context).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                setInfoAreaBackgroundColor(ContextCompat.getColor(context, R.color.db_accent_blue))
                // Dimensiones de la card
                setMainImageDimensions(320, 180)
            }
            return ViewHolder(cardView)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val app = item as AppItem
            Log.d(TAG, "AppCardPresenter.onBindViewHolder: Binding ${app.label}")
            
            val cardView = viewHolder.view as ImageCardView
            cardView.titleText = app.label
            cardView.mainImage = app.icon
            cardView.setMainImageDimensions(320, 180)

            cardView.setOnFocusChangeListener { _, hasFocus ->
                Log.v(TAG, "Card ${app.label} focus: $hasFocus")
                if (hasFocus) {
                    cardView.setMainImageDimensions(340, 200)
                } else {
                    cardView.setMainImageDimensions(320, 180)
                }
            }
            
            Log.d(TAG, "AppCardPresenter.onBindViewHolder: ${app.label} binding completado")
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {
            Log.v(TAG, "AppCardPresenter.onUnbindViewHolder")
            val cardView = viewHolder.view as ImageCardView
            cardView.badgeImage = null
            cardView.mainImage = null
        }
    }
}