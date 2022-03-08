package edu.umich.imagician

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import edu.umich.imagician.databinding.ListitemRequestBinding

class RequestListAdapter(context: Context, imgRequests: ArrayList<ImgRequest?>) :
    ArrayAdapter<ImgRequest?>(context, 0, imgRequests) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context)
                .inflate(R.layout.listitem_request, parent, false)
            rowView.tag = ListitemRequestBinding.bind(rowView) // cache binding
            rowView.tag
        }) as ListitemRequestBinding

        getItem(position)?.run {
            val color = when (status) {
                "Granted" -> R.color.granted
                "Pending" -> R.color.pending
                else -> R.color.rejected
            }
            listItemView.textView.text = imgPost?.filename
            listItemView.textView2.text = status
            listItemView.textView2.setTextColor(ContextCompat.getColor(context, color))
            listItemView.timestamp.text = "Usage right requested at ".plus(timestamp)
        }

        return listItemView.root
    }


}
