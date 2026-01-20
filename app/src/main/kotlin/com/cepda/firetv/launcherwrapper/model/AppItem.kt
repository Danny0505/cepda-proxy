package com.cepda.firetv.launcherwrapper.model

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class AppItem(
    val label: String,
    val icon: Drawable?,
    val componentName: ComponentName
)