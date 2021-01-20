package io.yocli.yo.ui

import android.content.Context
import android.os.Bundle
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Builder
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.yocli.yo.R
import io.yocli.yo.barcode.view.BarcodeReticleGraphic
import io.yocli.yo.barcode.view.CameraReticleAnimator
import io.yocli.yo.databinding.QrScanBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.yocli.yo.util.viewBinding
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ScanFragment : Fragment(R.layout.qr_scan) {
    private val viewModel by viewModels<ScanViewModel>()
    private val binding by viewBinding(QrScanBinding::bind)
    private lateinit var animator: CameraReticleAnimator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animator = CameraReticleAnimator(binding.overlay)

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            setupCamera()
        }

        viewModel.successFlow
            .filterNotNull()
            .onEach { deviceToken ->
                animator.cancel()
                findNavController().navigate(ScanFragmentDirections.scanToPaired(deviceToken))
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private suspend fun setupCamera() {
        val provider = requireContext()
            .getCameraProvider()
            .apply { unbindAll() }

        val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.preview.surfaceProvider) }
        val analysis = setupAnalyzer()

        provider.bindToLifecycle(this,
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build(),
            preview, analysis
        )

    }

    private suspend fun Context.getCameraProvider(): ProcessCameraProvider {
        val ctx = this
        return withContext(Dispatchers.IO) { ProcessCameraProvider.getInstance(ctx).await() }
    }

    private fun setupAnalyzer(): ImageAnalysis {
        return Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also { usecase ->
                val graphicOverlay = binding.overlay
                usecase.setAnalyzer(
                    ContextCompat.getMainExecutor(requireContext()),
                    { proxy: ImageProxy ->
                        graphicOverlay.also {
                            it.clear()
                            it.add(BarcodeReticleGraphic(it, animator))
                            animator.start()
                        }

                        viewModel.analyze(proxy)
                    }
                )
            }
    }

}