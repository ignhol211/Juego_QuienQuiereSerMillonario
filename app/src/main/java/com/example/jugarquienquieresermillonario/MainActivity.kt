package com.example.jugarquienquieresermillonario

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.jugarquienquieresermillonario.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    var totalQuestions = 0
    var rightQuestions = 0

    companion object{
        const val TOKEN = "TOKEN"
        fun launch(context: Context, token: String){
            val intent = Intent(context,MainActivity::class.java)
            intent.putExtra(TOKEN,token)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra(TOKEN)

        token?.let {
            getQuestion(token)
        }
    }

    private fun checkAnswer(user_answer: String, id: Int, token:String){

        val client = OkHttpClient()
        val request = Request.Builder()
        request.url("http://10.0.2.2:8082/question/${id}/${user_answer}")

        val call = client.newCall(request.build())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                CoroutineScope(Dispatchers.Main).launch {
                    Snackbar.make(binding.root,"Algo ha ido mal 2",Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                println(response.toString())
                response.body?.let { responseBody ->
                    val body = responseBody.string()
                    //println(body)
                    val gson = Gson()

                    val result = gson.fromJson(body, String::class.java)

                    if (result.equals("correcto",ignoreCase = true)){
                        totalQuestions+=1
                        rightQuestions+=1
                    }else{
                        totalQuestions+=1
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        Snackbar.make(binding.root,result,Snackbar.LENGTH_LONG).show()
                        delay(2000)
                        getQuestion(token)
                    }
                }
            }
        })
    }

    fun getQuestion(token:String){

        binding.tvPreguntasAcertadas.text = getString(R.string.acertadas, rightQuestions,totalQuestions)

        val client = OkHttpClient()
        val request = Request.Builder()
        request.url("http://10.0.2.2:8082/${token}/question")


        val call = client.newCall(request.build())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                CoroutineScope(Dispatchers.Main).launch {
                    Snackbar.make(binding.root,"Algo ha ido mal 1",Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                println(response.toString())
                response.body?.let { responseBody ->
                    val body = responseBody.string()
                    println(body)
                    val gson = Gson()

                    val question = gson.fromJson(body, Question::class.java)

                    question?.let{
                        CoroutineScope(Dispatchers.Main).launch {
                            hideProgressBar()
                            binding.tvPregunta.text = it.question
                            binding.bOpcion1.text = it.possible_answers[0]
                            binding.bOpcion2.text = it.possible_answers[1]
                            binding.bOpcion3.text = it.possible_answers[2]
                            binding.bOpcion4.text = it.possible_answers[3]
                        }
                    }?: run {
                        println("EL USUARIO YA HA RESPONDIDO TODAS LAS PREGUNTAS")
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    binding.bOpcion1.setOnClickListener {
                        showProgressBar()
                        showAlert(binding.bOpcion1.text.toString(), question.id,token)
                    }
                    binding.bOpcion2.setOnClickListener {
                        showProgressBar()
                        showAlert(binding.bOpcion2.text.toString(),question.id,token)
                    }
                    binding.bOpcion3.setOnClickListener {
                        showProgressBar()
                        showAlert(binding.bOpcion3.text.toString(),question.id,token)
                    }
                    binding.bOpcion4.setOnClickListener {
                        showProgressBar()
                        showAlert(binding.bOpcion4.text.toString(),question.id,token)
                    }
                }
            }
        })
    }

    fun showProgressBar(){
        binding.pbCargando.visibility = View.VISIBLE
    }

    fun hideProgressBar(){
        binding.pbCargando.visibility = View.GONE
    }

    fun showAlert(user_answer: String,id_question:Int, token:String){

        val builder = AlertDialog.Builder(this)
        builder.setMessage("??Quieres responder $user_answer ?")
        builder.setPositiveButton("S??") { _, _ -> checkAnswer(user_answer, id_question,token)}
        builder.setNegativeButton("No") { _, _ ->
                                                Snackbar.make(binding.root,"Selecciona otra opci??n",Snackbar.LENGTH_LONG).show()
                                                hideProgressBar()
                                            }
        builder.create()
        builder.show()

    }
}
