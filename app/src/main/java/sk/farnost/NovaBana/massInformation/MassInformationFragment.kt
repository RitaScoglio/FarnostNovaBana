package sk.farnost.NovaBana.massInformation

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import sk.farnost.NovaBana.MainViewModel
import sk.farnost.NovaBana.databinding.MassInformationFragmentBinding
import java.io.File

class MassInformationFragment : Fragment() {

    companion object {
        fun newInstance() = MassInformationFragment()
    }
    private lateinit var viewModel: MassInformationViewModel
    private lateinit var binding: MassInformationFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MassInformationFragmentBinding.inflate(inflater, container, false)
        return binding.getRoot();
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MassInformationViewModel::class.java)

        showPDF()
        viewModel.retrieveFilePath(requireContext())
    }


    private fun showPDF(){
        viewModel.filePath.observe(viewLifecycleOwner, Observer { path ->
            if(path != "")
                binding.pdfView.fromFile(File(path)).load()
        })
    }

}