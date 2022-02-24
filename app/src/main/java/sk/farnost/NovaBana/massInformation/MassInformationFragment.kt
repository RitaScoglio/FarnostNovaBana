package sk.farnost.NovaBana.massInformation

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import sk.farnost.NovaBana.MainViewModel
import sk.farnost.NovaBana.databinding.MassInformationFragmentBinding
import java.io.File

class MassInformationFragment : Fragment() {

    companion object {
        fun newInstance() = MassInformationFragment()
    }
    private lateinit var viewModel: MassInformationViewModel
    private lateinit var download: MainViewModel
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
        binding.pdfView.visibility = View.INVISIBLE

        viewModel = ViewModelProvider(this).get(MassInformationViewModel::class.java)
        download = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        showDownloadInfo()
        showPDF()
        downloadOption()
        //viewModel.retrieveFilePath(requireContext(), download.filePath)
    }


    private fun showPDF(){
        download.filePath.observe(viewLifecycleOwner, Observer { path ->
            if(File(path).exists()) {
                binding.pdfView.fromFile(File(path)).load()
                binding.pdfView.visibility = View.VISIBLE
                binding.downloadInfo.visibility = View.INVISIBLE
                binding.downloadButton.visibility = View.INVISIBLE
            } else {
                binding.downloadInfo.setText("Oznamy na tento týždeň nie sú k dispozícií.")
            }
        })
    }

    private fun downloadOption() {
        binding.downloadButton.setOnClickListener {
            if (download.isConnectedToInternet(requireContext()))
                download.getAvailableMassInformation(requireContext())
            else
                Toast.makeText(
                    requireContext(),
                    "Nie ste pripojený na internet.",
                    Toast.LENGTH_LONG
                ).show();
        }
    }

    private fun showDownloadInfo(){
        download.status.observe(viewLifecycleOwner, Observer { status ->
            binding.downloadInfo.setText(status)
        })
    }

}