package com.example.jugarquienquieresermillonario

import com.google.gson.Gson
import com.google.gson.annotations.Expose

class Question(

    val id: Int,
    var question:String,
    var possible_answers:List<String>,
    val correct_answer:String) {

    override fun toString(): String {
        var gson = Gson()
        return gson.toJson(this)
    }
}