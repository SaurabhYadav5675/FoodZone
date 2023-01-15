package com.skycore.foodplace.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.skycore.foodplace.R
import com.skycore.foodplace.databinding.AdapterRestaurentBinding
import com.skycore.foodplace.models.Businesse


class RestaurantAdapter(private val context: Context, private val restaurantList: List<Businesse>) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AdapterRestaurentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restData = restaurantList[position]
        holder.bind(restData)
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }

    inner class ViewHolder(itemView: AdapterRestaurentBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private val name = itemView.tvName
        private val address = itemView.tvAddress
        private val status = itemView.tvStatus
        private val rating = itemView.tvRating
        private val imageView = itemView.ivFoods


        @SuppressLint("SetTextI18n")
        fun bind(restData: Businesse) {
            name.text = restData.name
            address.text =
                "${restData.distance.toInt()}, ${restData.location.display_address.joinToString()}"
            rating.text = restData.rating.toString()

            val statusText = if (restData.is_closed) {
                "<font color='#8B0000'> ${context.getText(R.string.currentlyClose)}</font>"
            } else {
                "<font color='#006400'> ${context.getText(R.string.currentlyOpen)}</font>"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                status.text = HtmlCompat.fromHtml(statusText, HtmlCompat.FROM_HTML_MODE_COMPACT)
            } else {
                status.text = Html.fromHtml(statusText)
            }

            try {
                val requestOptions = RequestOptions().placeholder(R.drawable.progress_animation)
                    .error(R.drawable.ic_broken_image)
                Glide.with(context).setDefaultRequestOptions(requestOptions)
                    .load(restData.image_url).into(imageView)
            } catch (e: Exception) {
                Log.e("Glide error", e.message.toString())
            }

        }
    }
}