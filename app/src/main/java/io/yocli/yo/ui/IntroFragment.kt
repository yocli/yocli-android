package io.yocli.yo.ui

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.yocli.yo.R
import io.yocli.yo.databinding.IntroBinding
import io.yocli.yo.util.viewBinding
import com.google.android.material.R as materialR

class IntroFragment : Fragment(R.layout.intro) {

    private val viewModel by activityViewModels<MainViewModel>()
    private val binding by viewBinding(IntroBinding::bind)
    private lateinit var permissionRequester: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionRequester = registerForActivityResult(RequestPermission()) { granted ->
            if (!granted) return@registerForActivityResult

            findNavController().navigate(IntroFragmentDirections.introToScan())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.runIfPaired() {
            findNavController().navigate(IntroFragmentDirections.introToPaired(null))
        }

        binding.icon.shapeAppearanceModel = binding.icon.shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(resources.getDimension(materialR.dimen.mtrl_btn_corner_radius))
            .build()

        binding.instructionsButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yocli/homebrew-yocli")))
        }

        binding.scanButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission.CAMERA) == PERMISSION_GRANTED -> {
                    findNavController().navigate(IntroFragmentDirections.introToScan())
                }
                else -> permissionRequester.launch(permission.CAMERA)
            }

        }
    }

}