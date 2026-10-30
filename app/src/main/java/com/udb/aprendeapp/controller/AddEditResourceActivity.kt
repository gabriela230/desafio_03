package com.udb.aprendeapp.controller

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.udb.aprendeapp.R
import com.udb.aprendeapp.databinding.ActivityAddEditResourceBinding
import com.udb.aprendeapp.model.Resource
import com.udb.aprendeapp.model.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddEditResourceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditResourceBinding
    private var existingResource: Resource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditResourceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingResource = intent.getSerializableExtra("RESOURCE") as? Resource

        if (existingResource != null) {
            binding.tvFormTitle.text = getString(R.string.edit_resource_title)
            binding.etFormTitle.setText(existingResource!!.titulo)
            binding.etFormType.setText(existingResource!!.tipo)
            binding.etFormDescription.setText(existingResource!!.descripcion)
            binding.etFormLink.setText(existingResource!!.enlace)
            binding.etFormImage.setText(existingResource!!.imagen)
        }

        binding.btnSave.setOnClickListener {
            val titulo = binding.etFormTitle.text.toString().trim()
            val tipo = binding.etFormType.text.toString().trim()
            val descripcion = binding.etFormDescription.text.toString().trim()
            val enlace = binding.etFormLink.text.toString().trim()
            val imagen = binding.etFormImage.text.toString().trim()

            if (titulo.isEmpty() || tipo.isEmpty() || descripcion.isEmpty() || enlace.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos requeridos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imgUrl = if (imagen.isEmpty()) "https://via.placeholder.com/150" else imagen

            if (existingResource == null) {
                // Crear
                val newRes = Resource(
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    enlace = enlace,
                    imagen = imgUrl
                )
                RetrofitClient.instance.createResource(newRes).enqueue(object : Callback<Resource> {
                    override fun onResponse(call: Call<Resource>, response: Response<Resource>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddEditResourceActivity, "Recurso creado exitosamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    override fun onFailure(call: Call<Resource>, t: Throwable) {
                        Toast.makeText(this@AddEditResourceActivity, "Error al crear recurso", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // Editar
                val updatedRes = existingResource!!.copy(
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    enlace = enlace,
                    imagen = imgUrl
                )
                RetrofitClient.instance.updateResource(updatedRes.id, updatedRes).enqueue(object : Callback<Resource> {
                    override fun onResponse(call: Call<Resource>, response: Response<Resource>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddEditResourceActivity, "Recurso actualizado correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    override fun onFailure(call: Call<Resource>, t: Throwable) {
                        Toast.makeText(this@AddEditResourceActivity, "Error al actualizar recurso", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
