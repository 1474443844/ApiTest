package cn.wantu.apitest.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import cn.wantu.apitest.R

class DownloadProgressDialogFragment : DialogFragment() {

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_download_progress_dialog, container, false)
        progressBar = view.findViewById(R.id.progress_bar)
        return view
    }

    fun updateProgress(progress: Int) {
        progressBar.progress = progress
    }
}