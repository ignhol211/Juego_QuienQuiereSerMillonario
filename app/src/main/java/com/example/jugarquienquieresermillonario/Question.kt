package com.example.jugarquienquieresermillonario

import com.google.gson.Gson

class Question(
    val id: Int,
    var question:String,
    var possible_answers:List<String>,
    var correct_answer:String) {

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

}