package com.example.drawerexample.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.drawerexample.Advertisement
import com.example.drawerexample.R

class AdvertisementsAdapter(val parentFragment : Fragment) : RecyclerView.Adapter<AdvertisementsAdapter.AdvViewHolder>() {

    var data = mutableListOf<Advertisement>()

    class AdvViewHolder(view: View, parentFragment: Fragment) : RecyclerView.ViewHolder(view) {
        val advTitleTv : TextView = view.findViewById<TextView>(R.id.adv_title)
        val advDateTv : TextView = view.findViewById<TextView>(R.id.adv_date)
        val advLocationTv : TextView = view.findViewById<TextView>(R.id.adv_location)

        init {
            view.findViewById<ImageButton>(R.id.adv_edit).setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("adv_index", adapterPosition)
                parentFragment.findNavController().navigate(R.id.action_nav_adv_list_to_nav_edit_adv, bundle)
            }

            view.findViewById<ConstraintLayout>(R.id.adv_card).setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("adv_index", adapterPosition)
                parentFragment.findNavController().navigate(R.id.action_nav_adv_list_to_nav_show_adv, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.adv_list_item, parent, false)

        return AdvViewHolder(view, parentFragment)
    }


    override fun onBindViewHolder(holder: AdvViewHolder, position: Int) {
        holder.advTitleTv.text = data[position].title
        holder.advDateTv.text = data[position].date
        holder.advLocationTv.text = data[position].location
    }

    override fun getItemCount(): Int {
        return data.size
    }

}