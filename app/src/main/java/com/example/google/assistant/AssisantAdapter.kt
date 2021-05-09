package com.example.google.assistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.google.R
import com.example.google.data.Assistant

class AssisantAdapter : RecyclerView.Adapter<AssisantAdapter.ViewHolder>() {

    var data= listOf<Assistant>()
    set(value) {
        field=value
       notifyDataSetChanged()
    }
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val assistantMessage : TextView = itemView.findViewById(R.id.assistant_msg)
        val humanMessage : TextView = itemView.findViewById(R.id.human_msg)
    }
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int):
            ViewHolder {
         val layoutInflater = LayoutInflater.from(parent.context)
         val view = layoutInflater.inflate(R.layout.assiatnt_item_layout,
         parent,
         false) as ConstraintLayout
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val  item=data[position]
        holder.assistantMessage.text=item.assistant_message
        holder.humanMessage.text=item.human_message

    }
    override fun getItemCount()=data.size
}