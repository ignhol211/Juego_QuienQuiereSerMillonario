package com.example.jugarquienquieresermillonario

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jugarquienquieresermillonario.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.util.regex.Pattern

class LoginActivity :AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("BIEN CREADO 1")

        binding.bContinue.setOnClickListener{
            println("BIEN CREADO 2")
            validateUserAndPassword()
        }

    }

    private fun validateUserAndPassword() {
        val user = binding.user.text.toString()
        val password = binding.password.text.toString()

        println("BIEN CREADO 2 //"+user+"//"+password)

        val userOK = isUserOk(user)
        println(userOK.toString())
        val passwordOK = isPasswordOk(password)
        println(password.toString())

        if (userOK && passwordOK) {
            val token = registerUser(user,password)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("token",token)
            startActivity(intent)
        }else{
            Snackbar.make(binding.root,"Error",Snackbar.LENGTH_LONG).show()
        }

    }

    private fun registerUser(user: String, password: String):String {
        var token = ""

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

                    token = gson.fromJson(body, String::class.java)

                    CoroutineScope(Dispatchers.Main).launch {
                        Snackbar.make(binding.root,"El usuario ha sido registrado con el token $token",Snackbar.LENGTH_LONG).show()
                        delay(2000)
                    }
                }
            }
        })
        return token
    }

    private fun isUserOk(user: String): Boolean {
        val pattern = Pattern.compile("[a-zA-Z]")
        return if(pattern.matcher(user).matches() && user.length >= 3){
            true
        }else{
            Snackbar.make(binding.root,"Usuario incorrecto", Snackbar.LENGTH_LONG).show()
            return false
        }
    }

    private fun isPasswordOk(password: String): Boolean {
        val pattern = Pattern.compile("[a-zA-Z0-9]")
        return if(pattern.matcher(password).matches() && password.length >= 8){
            true
        }else{
            Snackbar.make(binding.root,"Contraseña incorrecto", Snackbar.LENGTH_LONG).show()
            return false
        }
    }

}