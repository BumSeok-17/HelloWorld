package com.android.helloworld

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_event.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var database:DatabaseReference
    lateinit var organUser:Profile
    lateinit var allEv : ArrayList<Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        init()
    }

    fun init(){
        database = FirebaseDatabase.getInstance().reference
        if(intent.hasExtra("loginprofile")){
            organUser = intent.getSerializableExtra("loginprofile") as Profile
        }
        if(intent.hasExtra("eventlist")){
            //allEv = intent.getParcelableArrayListExtra("eventlist")
            allEv = global_eventList
        }
        eventSaveBtn.setOnClickListener{
            val enum = allEv.size
            val epic = addEventImg.resources.toString()
            val ename = addEvNameEdit.text.toString()
            val eaddr = addEvAddrEdit.text.toString()
            val edate = addEvDateEdit.text.toString()
            val eper = addEvPeopleEdit.text.toString()
            val egreet = addEvGreetEdit.text.toString()
            val eorgan = organUser

            val EventDB = database.child("events").push()
            EventDB.child("eventNumber").setValue(enum)
            EventDB.child("thumbnail").setValue(epic)
            EventDB.child("eventName").setValue(ename)
            EventDB.child("personnel").setValue(eper)
            EventDB.child("address").setValue(eaddr)
            EventDB.child("date").setValue(edate)
            EventDB.child("attendP")
            EventDB.child("organizer").setValue(organUser)
            EventDB.child("greeting").setValue(egreet)
            EventDB.child("key").setValue(EventDB.key)

            finish()
        }
    }

}