package io.yocli.yo.ui

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.yocli.yo.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class ScanViewModel : ViewModel() {
    private val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build())
    private val imageFlow: MutableStateFlow<ImageProxy?> = MutableStateFlow(null)
    private val _successFlow = MutableStateFlow<String?>(null)
    val successFlow = _successFlow.asStateFlow()
    private val client = ApiClient()
    private val fcmToken = FirebaseMessaging.getInstance().token

    init {
        imageFlow
            .filterNotNull()
            .mapNotNull mnn@{ proxy ->
                val url = runAnalysis(proxy)
                    ?: return@mnn null.also { Timber.w("url null") }
                val (baseUrl, scanToken) = parseUrl(url)
                    ?: return@mnn null.also { Timber.e("couldn't parse URL: %s", url) }
                val deviceToken = fcmToken.await()
                client.register(baseUrl, scanToken, deviceToken)
                deviceToken
            }
            .apply {
                viewModelScope.launch {
                    _successFlow.value = first()
                }
            }
    }

    fun analyze(proxy: ImageProxy) {
        imageFlow.tryEmit(proxy)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private suspend fun runAnalysis(proxy: ImageProxy): String? = withContext(Dispatchers.Default) {
        proxy.use {
            val input = InputImage.fromMediaImage(proxy.image!!, proxy.imageInfo.rotationDegrees)
            scanner.process(input)
                .await()
                .mapNotNull { it?.url?.url }
                .firstOrNull()
        }
    }

    //When you scan the QR code, it will be a URL of the form:
    //	https://api.yocli.io/?p=<longuuidv4hex>
    private fun parseUrl(url: String): Pair<String, String>? {
        val parsed = url.toUri()
        val scheme = parsed.scheme ?: return null
        val host = parsed.host ?: return null
        val port = parsed.port.takeIf { it > -1 }?.let { ":$it" } ?: ""
        val token = parsed.getQueryParameter("p") ?: return null

        return """$scheme://$host$port""" to token
    }
}