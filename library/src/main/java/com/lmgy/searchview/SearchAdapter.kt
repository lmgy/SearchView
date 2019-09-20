package com.lmgy.searchview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

/**
 * @author lmgy
 * @date 2019/9/19
 */
class SearchAdapter : BaseAdapter, Filterable {

    private var data: ArrayList<String>
    private var suggestions: Array<String>? = null
    private var suggestionIcon: Drawable? = null
    private var inflater: LayoutInflater? = null
    private var ellipsize: Boolean = false

    constructor(context: Context, suggestions: Array<String>) {
        inflater = LayoutInflater.from(context)
        data = ArrayList()
        this.suggestions = suggestions
    }

    constructor(
        context: Context,
        suggestions: Array<String>,
        suggestionIcon: Drawable,
        ellipsize: Boolean
    ) {
        inflater = LayoutInflater.from(context)
        data = ArrayList()
        this.suggestions = suggestions
        this.suggestionIcon = suggestionIcon
        this.ellipsize = ellipsize
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            @SuppressLint("DefaultLocale")
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()
                if (!TextUtils.isEmpty(constraint)) {
                    // Retrieve the autocomplete results.
                    val searchData = ArrayList<String>()
                    for (string in suggestions!!) {
                        if (string.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            searchData.add(string)
                        }
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = searchData
                    filterResults.count = searchData.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.values != null) {
                    data = results.values as ArrayList<String>
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getCount() = data.size

    override fun getItem(p0: Int) = data[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val viewHolder: SuggestionsViewHolder
        var tempConvertView: View? = null
        if (convertView == null) {
            tempConvertView = inflater?.inflate(R.layout.suggest_item, parent, false)
            viewHolder = SuggestionsViewHolder(tempConvertView)
            tempConvertView?.tag = viewHolder
        } else {
            viewHolder = convertView.tag as SuggestionsViewHolder
        }

        viewHolder.textView.text = getItem(position)
        if (ellipsize) {
            viewHolder.textView.setSingleLine()
            viewHolder.textView.ellipsize = TextUtils.TruncateAt.END
        }
        return convertView ?: tempConvertView!!

    }

    private inner class SuggestionsViewHolder(convertView: View?) {

        internal var textView: TextView =
            convertView?.findViewById(R.id.suggestion_text) as TextView
        internal var imageView: ImageView? = null

        init {
            if (suggestionIcon != null) {
                imageView = convertView?.findViewById(R.id.suggestion_icon) as ImageView
                imageView?.setImageDrawable(suggestionIcon)
            }
        }
    }

}