package com.dogancankilic.firebasemlkitsample

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_items.view.*

class MyAdapter(private val context: Context, private val model : ArrayList<Model>)  : RecyclerView.Adapter<CustomViewHolder>()  {

    override fun getItemCount(): Int {
        return model.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_items,p0,false)
        return  CustomViewHolder(v)
    }

    override fun onBindViewHolder(p0: CustomViewHolder, p1: Int) {


        p0.itemView.textView.text = model[p1].label
        p0.itemView.confidence.text ="Confidence: " + model[p1].confidence.toString()


    }
}
class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v)