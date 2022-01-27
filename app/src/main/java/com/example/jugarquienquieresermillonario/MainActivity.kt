package com.example.jugarquienquieresermillonario

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jugarquienquieresermillonario.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAnotherQuestion()
    }

    fun checkAnswer(user_answer:String,correct_answer:String){
        if(user_answer.equals(correct_answer))
            Toast.makeText(this@MainActivity, "Correcto", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this@MainActivity, "Incorrecto", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Main).launch{
            delay(3000)
            getAnotherQuestion()
        }
    }

    fun getAnotherQuestion(){
        val client = OkHttpClient()

        val request = Request.Builder()
        request.url("http://10.0.2.2:8082/GetQuestion")


        val call = client.newCall(request.build())
        call.enqueue( object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@MainActivity, "Algo ha ido mal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                println(response.toString())
                response.body?.let { responseBody ->
                    val body = responseBody.string()
                    println(body)
                    val gson = Gson()

                    val question = gson.fromJson(body, Question::class.java)


                    println("CONTROL")
                    println(question)

                    CoroutineScope(Dispatchers.Main).launch {
                        binding.tvPregunta.text = question.question
                        binding.bOpcion1.text = question.possible_answers[0]
                        binding.bOpcion2.text = question.possible_answers[1]
                        binding.bOpcion3.text = question.possible_answers[2]
                        binding.bOpcion4.text = question.possible_answers[3]
                    }
                    binding.bOpcion1.setOnClickListener(){
                        checkAnswer(binding.bOpcion1.text as String,question.correct_answer)
                    }
                    binding.bOpcion2.setOnClickListener(){
                        checkAnswer(binding.bOpcion2.text as String,question.correct_answer)
                    }
                    binding.bOpcion3.setOnClickListener(){
                        checkAnswer(binding.bOpcion3.text as String,question.correct_answer)
                    }
                    binding.bOpcion4.setOnClickListener(){
                        checkAnswer(binding.bOpcion4.text as String,question.correct_answer)
                    }
                }
            }
        })
    }
}