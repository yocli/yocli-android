package io.yocli.yo.ui

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.yocli.yo.R
import io.yocli.yo.databinding.StartBinding
import io.yocli.yo.util.viewBinding
import com.google.android.material.R as materialR

class StartFragment : Fragment(R.layout.start) {

    private val viewModel by activityViewModels<MainViewModel>()
    private val binding by viewBinding(StartBinding::bind)
    private lateinit var permissionRequester: ActivityResultLauncher<String>
    private val args by navArgs<StartFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionRequester = registerForActivityResult(RequestPermission()) { granted ->
            if (!granted) return@registerForActivityResult
            findNavController().navigate(StartFragmentDirections.startToScan())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.deviceToken?.let { viewModel.persistPairing(it) }

        binding.icon.shapeAppearanceModel = binding.icon.shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(resources.getDimension(materialR.dimen.mtrl_btn_corner_radius))
            .build()

        binding.scanButton.setOnClickListener {
            if (args.deviceToken != null) viewModel.rePairDevice()
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission.CAMERA) == PERMISSION_GRANTED -> {
                    findNavController().navigate(StartFragmentDirections.startToScan())
                }
                else -> permissionRequester.launch(permission.CAMERA)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bindState(viewModel.getAppState(NotificationManagerCompat.from(requireContext())))
    }

    private fun bindState(state: AppUiState) {
        val ctx = requireContext()

        binding.icon.setImageResource(state.icon)
        binding.title.setText(state.title)
        binding.body1.setText(state.subtitle)
        binding.body2.setText(state.body)
        state.infoButtonBlurb
            ?.let {
                binding.instructionsInstructions.isVisible = true
                binding.instructionsInstructions.setText(it)
            }
            ?: run { binding.instructionsInstructions.isVisible = false }
        binding.instructionsButton.icon = ContextCompat.getDrawable(ctx, state.infoButtonIcon)
        binding.instructionsButton.setText(state.infoButton)
        binding.instructionsButton.setOnClickListener {
            val intent = if (state.infoButtonBlurb != null) {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yocli/homebrew-yocli"))
            } else {
                with(Intent()) {
                    if (VERSION.SDK_INT >= VERSION_CODES.O) {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                    } else {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", ctx.packageName)
                        putExtra("app_uid", ctx.applicationInfo.uid)
                    }
                }
            }
            startActivity(intent)
        }

        state.scanButtonBlurb
            ?.let {
                binding.scanInstructions.isVisible = true
                binding.scanInstructions.setText(it)
            }
            ?: run { binding.scanInstructions.isVisible = false }
        state.scanButton
            ?.let {
                binding.scanButton.isVisible = true
                binding.scanButton.setText(it)
            }
            ?: run { binding.scanButton.isVisible = false }
    }
}