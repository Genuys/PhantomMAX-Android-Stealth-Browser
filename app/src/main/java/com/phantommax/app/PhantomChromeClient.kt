package com.phantommax.app

import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.GeolocationPermissions

class PhantomChromeClient : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return true
    }

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        result?.confirm()
        return true
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        if (request == null) return

        val blockedResources = mutableListOf<String>()

        for (resource in request.resources) {
            when {
                resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE && PhantomApp.blockCamera -> {
                    blockedResources.add(resource)
                }
                resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE && PhantomApp.blockMicrophone -> {
                    blockedResources.add(resource)
                }
                resource == PermissionRequest.RESOURCE_MIDI_SYSEX -> {
                    blockedResources.add(resource)
                }
                resource == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> {
                    blockedResources.add(resource)
                }
            }
        }

        if (blockedResources.size == request.resources.size) {
            request.deny()
        } else if (blockedResources.isNotEmpty()) {
            val allowed = request.resources.filter { it !in blockedResources }.toTypedArray()
            request.grant(allowed)
        } else {
            request.deny()
        }
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback?.invoke(origin, false, false)
    }

    override fun onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt()
    }
}
