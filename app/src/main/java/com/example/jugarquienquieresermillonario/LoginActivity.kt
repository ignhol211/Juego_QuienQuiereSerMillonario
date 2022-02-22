package com.example.jugarquienquieresermillonario

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.jugarquienquieresermillonario.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

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
            registerUser(binding.user.text.toString(),checkUser(binding.user.text.toString(),binding.password.text.toString()))
        }
    }

    private fun checkUser(user: String, password: String):String {
        val sharedPreferences = getSharedPreferences("keys", Context.MODE_PRIVATE)
        val keyChain = sharedPreferences.getString(user,"empty").toString()

        return if (keyChain == "empty"){
            val key = generateRandomKey()
            val codePassword = code(password,key)

            val editSharedPreferences = sharedPreferences.edit()
            editSharedPreferences.putString(user,key)
            editSharedPreferences.apply()

            codePassword
        }else{
            code(password,keyChain)
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
                    CoroutineScope(Dispatchers.Main).launch {
                        if(body.contains("Contraseña incorrecta")){
                            Snackbar.make( binding.root,body,Snackbar.LENGTH_LONG).show()
                        }else {
                            Snackbar.make(binding.root,"El usuario ha sido registrado con el token $body",Snackbar.LENGTH_LONG).show()
                            delay(1000)
                            MainActivity.launch(this@LoginActivity, body)
                        }
                    }
                }
            }
        })
    }

    private fun code(text:String,key:String):String{
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE,getKey(key))
        val codeText = android.util.Base64.encodeToString(cipher.doFinal(text.toByteArray(Charsets.UTF_8)), android.util.Base64.URL_SAFE)
        return codeText
    }

    private fun getKey(key : String): SecretKeySpec {
        var keyUTF8 = key.toByteArray(Charsets.UTF_8)
        val sha = MessageDigest.getInstance("SHA-1")
        keyUTF8 = sha.digest(keyUTF8)
        keyUTF8 = keyUTF8.copyOf(16)
        return SecretKeySpec(keyUTF8, "AES")
    }

    private fun generateRandomKey():String{
        var randomKey = ""
        for (i in 0..10)
            randomKey += (0..9).random()
        return randomKey
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