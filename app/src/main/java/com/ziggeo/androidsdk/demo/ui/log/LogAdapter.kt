package com.ziggeo.androidsdk.demo.ui.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.ui.global.FeatureViewHolder
import com.ziggeo.androidsdk.log.LogModel
import com.ziggeo.androidsdk.utils.DateTimeUtils
import java.util.*

/**
 * Created by Alexander Bedulin on 04-Oct-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class LogAdapter(private val list: List<LogModel>) :
    RecyclerView.Adapter<LogAdapter.LogsViewHolder>() {

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LogsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return LogsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class LogsViewHolder(
        private val view: View,
        private var tvLog: TextView? = view.findViewById(R.id.tv_log),
    ) : FeatureViewHolder(view) {
        fun bind(logModel: LogModel) {

            val format = if (logModel.details == null) {
                "[%s] %s"
            } else {
                "[%s] %s: %s"
            }
            tvLog?.text = String.format(
                format,
                DateTimeUtils.formatDate(Date(logModel.timestamp)),
                logModel.name,
                logModel.details
            )
        }
    }

}