package com.appfactory.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.appfactory.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "App Factory",
    ) {
        App()
    }
}
