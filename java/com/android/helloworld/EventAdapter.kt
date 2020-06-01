package com.android.helloworld

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class EventAdapter(val eventList:ArrayList<Event>) : RecyclerView.Adapter<EventAdapter.ViewHolder>(){

    var itemClickListener : OnItemClickListener? = null

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var name:TextView
        var person:TextView
        var date : TextView
        init {
            name = itemView.findViewById(R.id.eventName)
            person = itemView.findViewById(R.id.eventPerson)
            date = itemView.findViewById(R.id.eventDate)
            itemView.setOnClickListener {
                val position = adapterPosition
                itemClickListener?.OnItemClick(this, it, eventList[position], position)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val v = LayoutInflater.from(p0.context).inflate(R.layout.event_row, p0, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return eventList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        p0.name.text = eventList.get(p1).eventName.toString()
        p0.person.text = eventList.get(p1).personnel.toString()
        p0.date.text = eventList.get(p1).date.toString()

    }

    interface OnItemClickListener{
        fun OnItemClick(holder: RecyclerView.ViewHolder, view: View, event: Event, position : Int)
    }

}