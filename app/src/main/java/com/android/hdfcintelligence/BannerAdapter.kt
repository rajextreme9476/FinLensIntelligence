package com.android.hdfcintelligence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class BannerAdapter(private val bannerList: List<BannerModel>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerImage: ImageView = itemView.findViewById(R.id.banner_image)
        val bannerTitle: TextView = itemView.findViewById(R.id.banner_title)
        val bannerDescription: TextView = itemView.findViewById(R.id.banner_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = bannerList[position]
        holder.bannerTitle.text = banner.title
        holder.bannerDescription.text = banner.description
        holder.bannerImage.setImageResource(banner.imageRes)

    }

    override fun getItemCount(): Int = bannerList.size
}
