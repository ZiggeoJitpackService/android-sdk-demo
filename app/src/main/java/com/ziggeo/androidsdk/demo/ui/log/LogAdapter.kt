package com.ziggeo.androidsdk.demo.ui.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ziggeo.androidsdk.demo.databinding.ItemLogBinding
import com.ziggeo.androidsdk.demo.ui.global.FeatureViewHolder
import com.ziggeo.androidsdk.log.LogModel
import com.ziggeo.androidsdk.utils.DateTimeUtils
import java.util.Date

/**
 * Created by Alexander Bedulin on 04-Oct-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class LogAdapter(private val list: List<LogModel>) :
    RecyclerView.Adapter<LogAdapter.LogsViewHolder>() {

    private var _binding: ItemLogBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LogsViewHolder {
        _binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val view = binding.root
        return LogsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class LogsViewHolder(
        private val view: View
    ) : FeatureViewHolder(view) {
        val binding = ItemLogBinding.bind(view)
        fun bind(logModel: LogModel) {
            val format = if (logModel.details == null) {
                "[%s] %s"
            } else {
                "[%s] %s: %s"
            }
            binding.tvLog.text = String.format(
                format,
                DateTimeUtils.formatDate(Date(logModel.timestamp)),
                logModel.name,
                logModel.details
            )
        }
    }

}