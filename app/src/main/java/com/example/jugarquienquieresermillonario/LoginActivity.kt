package com.example.jugarquienquieresermillonario

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.jugarquienquieresermillonario.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class LoginActivity :AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.user.doAfterTextChanged {
            validateUserAndPassword()
        }

        binding.password.doAfterTextChanged {
            validateUserAndPassword()
        }
    }

    private fun validateUserAndPassword() {

        if(isUserOk() && isPasswordOk()){
            binding.bContinue.visibility = View.VISIBLE
        }else{
            binding.bContinue.visibility = View.GONE
        }

        binding.bContinue.setOnClickListener{
            registerUser(binding.user.text.toString(),binding.password.text.toString())
        }
    }

    private fun registerUser(user: String, password: String){

        val client = OkHttpClient()
        val request = Request.Builder()
        request.url("http://10.0.2.2:8082/registro/${user}/${password}")

        val call = client.newCall(request.build())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                CoroutineScope(Dispatchers.Main).launch {
                    Snackbar.make(binding.root,"No se ha podido registrar al usuario",Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                println(response.toString())
                response.body?.let { responseBody ->
                    val body = responseBody.string()
                    //println(body)
                    val gson = Gson()

                    val token = gson.fromJson(body, String::class.java)

                    CoroutineScope(Dispatchers.Main).launch {
                        Snackbar.make(binding.root,"El usuario ha sido registrado con el token $token",Snackbar.LENGTH_LONG).show()
                        delay(2000) //PONEMOS UN DELAY PARA VER EL FUNCIONAMIENTO DEL SNACKBAR
                        token?.let {MainActivity.launch(this@LoginActivity,token)}
                    }
                }
            }
        })
    }

    private fun isUserOk() : Boolean {
        val regex = Regex("[a-z]{5}$")
        return regex.matches(binding.user.text.toString())
    }

    private fun isPasswordOk() : Boolean{
        val regex = Regex("[a-zA-Z0-9]{8}$")
        return  regex.matches(binding.password.text.toString())
    }

}