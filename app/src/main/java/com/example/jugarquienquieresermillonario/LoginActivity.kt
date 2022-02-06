package com.example.jugarquienquieresermillonario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jugarquienquieresermillonario.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class LoginActivity :AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bContinue.setOnClickListener{
            validateUserAndPassword()
        }

    }

    private fun validateUserAndPassword() {
        val user = binding.user.text.toString()
        val password = binding.password.text.toString()

        val userOK = isUserOk(user)
        println("CONTROL USER "+userOK)
        val passwordOK = isPasswordOk(password)
        println("CONTROL PASSWORD "+passwordOK)

        if (userOK && passwordOK) {
            val token = registerUser(user,password)
            println ("CONTROL "+token)
            MainActivity.launch(this,token)
        }else{
            Snackbar.make(binding.root,"Error",Snackbar.LENGTH_LONG).show()
        }

    }

    private fun registerUser(user: String, password: String):String {
        var token = ""

        val client = OkHttpClient()
        val request = Request.Builder()
        request.url("http://10.0.2.2:8082/registro/${user}/${password}")

        println("MY CONTROL")

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

                    token = gson.fromJson(body, String::class.java)

                    CoroutineScope(Dispatchers.Main).launch {
                        Snackbar.make(binding.root,"El usuario ha sido registrado con el token $token",Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        })
        println("MY CONTROL " + token)
        return token
    }

    private fun isUserOk(user: String): Boolean {
        val regex = Regex.fromLiteral("abc")
        return if (regex.matches(user) && user.length >= 3){
            true
        }else{
            Snackbar.make(binding.root,"Usuario incorrecto", Snackbar.LENGTH_LONG).show()
            return false
        }
    }

    private fun isPasswordOk(password: String): Boolean {
        val regex = Regex.fromLiteral("abc123456")
        return if(regex.matches(password) && password.length >= 8){
            true
        }else{
            Snackbar.make(binding.root,"Contraseña incorrecto", Snackbar.LENGTH_LONG).show()
            return false
        }
    }

}