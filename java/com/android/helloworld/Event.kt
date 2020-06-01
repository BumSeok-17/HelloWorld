package com.android.helloworld

data class Event(
    var eventNumber:Int = 0,
    var thumbnail:String = "",  //이벤트 썸네일
    var eventName:String = "",  //이벤트 명
    var personnel:String = "",      //인원 제한
    var address:String = "",    //이벤트 장소
    var date : String = "",     //이벤트 날짜
    var attendP: ArrayList<Profile> = arrayListOf<Profile>(),  //참석인원
    var organizer : Profile = Profile(),    //주최자
    var greeting:String = ""         //이벤트 소개 글
)
