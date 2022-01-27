package com.example.jugarquienquieresermillonario

import com.google.gson.Gson

class QuestionWithoutCorrectAnswer(
    val id: Int,
    var question:String,
    var possible_answers:List<String>) {

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}