package uk.airbyte.skrrt

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso


class RapperAdapter(
    private val context: Context,
    private val dataSource: MutableList<Rapper>
) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    companion object {
        private val LABEL_COLORS = hashMapOf(
            "alive" to R.color.colorAlive,
            "prison" to R.color.colorPrison,
            "dead" to R.color.colorDead
        )
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }var rapperList: MutableList<Rapper>? = null

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {

            view = inflater.inflate(R.layout.list_item_rapper, parent, false)

            holder = ViewHolder()
            holder.thumbnailImageView = view.findViewById(R.id.rapper_list_thumbnail) as ImageView
            holder.titleTextView = view.findViewById(R.id.rapper_list_title) as TextView
            holder.subtitleTextView = view.findViewById(R.id.rapper_list_subtitle) as TextView
            holder.detailTextView = view.findViewById(R.id.rapper_list_detail) as TextView
            holder.upvoteButton = view.findViewById(R.id.upvote) as Button
            holder.downvoteButton = view.findViewById(R.id.downvote) as Button

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val titleTextView = holder.titleTextView
        val subtitleTextView = holder.subtitleTextView
        val detailTextView = holder.detailTextView
        val thumbnailImageView = holder.thumbnailImageView

        val upvoteButton = holder.upvoteButton
        val downvoteButton = holder.downvoteButton

        val rapper = getItem(position) as Rapper

        titleTextView.text = rapper.name
        subtitleTextView.text = rapper.birthplace

        var rating = rapper.rating
        if (rating == null) {
            rating = 0
        }

        detailTextView.text = rating.toString()

        if (rapper.imageUrl != "") {
            Picasso.with(context).load(rapper.imageUrl).placeholder(R.mipmap.ic_launcher).into(thumbnailImageView)
        } else {
            Picasso.with(context).load("https://res.cloudinary.com/teepublic/image/private/s--BIZer3QX--/t_Preview/b_rgb:191919,c_limit,f_jpg,h_630,q_90,w_630/v1483191015/production/designs/1009554_1.jpg").placeholder(R.mipmap.ic_launcher).into(thumbnailImageView)
        }

        titleTextView.setTextColor(
            ContextCompat.getColor(context, LABEL_COLORS[rapper.status] ?: R.color.colorPrimary)
        )

        when {
            rating.toInt() == 0 -> detailTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrison))
            rating < 0 -> detailTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDead))
            else -> detailTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAlive))
        }

        upvoteButton.setOnClickListener { view ->
            var rating = rapper.rating
            if (rating == null) {
                rating = 0
            }
            rapper.rating = rating!!.plus(1)
            detailTextView.text = (rapper.rating!!.toString())

            val rowListener: ItemRowListener = context as MainActivity
            rowListener.changeCountItemState(rapper.objectId!!, rapper)

        }

        downvoteButton.setOnClickListener { view ->
            var rating = rapper.rating
            if (rating == null) {
                rating = 0
            }
            rapper.rating = rating!!.minus(1)
            detailTextView.text = (rapper.rating!!.toString())

            val rowListener: ItemRowListener = context as MainActivity
            rowListener.changeCountItemState(rapper.objectId!!, rapper)
        }

        return view
    }

    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var subtitleTextView: TextView
        lateinit var detailTextView: TextView
        lateinit var thumbnailImageView: ImageView
        lateinit var upvoteButton: Button
        lateinit var downvoteButton: Button
    }
}
