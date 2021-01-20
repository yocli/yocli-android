package io.yocli.yo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.yocli.yo.R
import io.yocli.yo.databinding.PairedBinding
import io.yocli.yo.util.viewBinding
import com.google.android.material.R as MaterialR

class PairedFragment : Fragment(R.layout.paired) {
    private val viewModel by activityViewModels<MainViewModel>()
    private val binding by viewBinding(PairedBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args by navArgs<PairedFragmentArgs>()
        args.deviceToken?.let { viewModel.persistPairing(it) }

        binding.icon.shapeAppearanceModel = binding.icon.shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(resources.getDimension(MaterialR.dimen.mtrl_btn_corner_radius))
            .build()

        binding.instructionsButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yocli/homebrew-yocli")))
        }

        binding.scanButton.setOnClickListener {
            viewModel.rePairDevice()
            findNavController().navigate(PairedFragmentDirections.pairedToScan())
        }
    }

}