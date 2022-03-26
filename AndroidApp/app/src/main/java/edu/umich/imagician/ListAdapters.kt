package edu.umich.imagician

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import edu.umich.imagician.databinding.ListitemHistoryBinding
import edu.umich.imagician.databinding.ListitemRequestBinding
import edu.umich.imagician.databinding.ListitemUploadBinding

class RequestListAdapter(context: Context, watermarkRequests: ArrayList<WatermarkRequest?>) :
    ArrayAdapter<WatermarkRequest?>(context, 0, watermarkRequests) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context)
                .inflate(R.layout.listitem_request, parent, false)
            rowView.tag = ListitemRequestBinding.bind(rowView) // cache binding
            rowView.tag
        }) as ListitemRequestBinding

        getItem(position)?.run {
            val color = when (status) {
                "GRANTED" -> R.color.granted
                "PENDING" -> R.color.pending
                else -> R.color.rejected
            }
            listItemView.textView.text = watermarkPost?.title
            listItemView.textView2.text = status
            listItemView.textView2.setTextColor(ContextCompat.getColor(context, color))
            listItemView.timestamp.text = "Usage right requested at ".plus(timestamp)
        }

        return listItemView.root
    }
}

class PostListAdapter(context: Context, watermarkPosts: ArrayList<WatermarkPost?>) :
    ArrayAdapter<WatermarkPost?>(context, 0, watermarkPosts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context)
                .inflate(R.layout.listitem_upload, parent, false)
            rowView.tag = ListitemUploadBinding.bind(rowView)
            rowView.tag
        }) as ListitemUploadBinding

        getItem(position)?.run {
            val color = when (numPending) {
                0 -> R.color.pending
                else -> R.color.rejected
            }
            listItemView.textView.text = title
            listItemView.textView2.text = numPending.toString().plus(" pending(s)")
            listItemView.textView2.setTextColor(ContextCompat.getColor(context, color))
            listItemView.timestamp.text = "Watermark embedded at ".plus(timestamp)
        }

        return listItemView.root
    }
}

class HistoryListAdapter(
    context: Context,
    watermarkRequests: ArrayList<WatermarkRequest?>,
    val seeMore: (index: Int) -> Unit
) :
    ArrayAdapter<WatermarkRequest?>(context, 0, watermarkRequests) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context)
                .inflate(R.layout.listitem_history, parent, false)
            rowView.tag = ListitemHistoryBinding.bind(rowView)
            rowView.tag
        }) as ListitemHistoryBinding

        listItemView.button.setOnClickListener {
            seeMore(position)
        }

        getItem(position)?.run {
            listItemView.requester.text = sender
            listItemView.msg.text = message
        }

        return listItemView.root
    }
}

