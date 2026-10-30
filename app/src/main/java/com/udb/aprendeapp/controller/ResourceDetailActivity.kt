package com.udb.aprendeapp.controller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.udb.aprendeapp.R
import com.udb.aprendeapp.databinding.ActivityResourceDetailBinding
import com.udb.aprendeapp.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResourceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResourceDetailBinding
    private lateinit var sessionManager: SessionManager
    private var resource: Resource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        resource = intent.getSerializableExtra("RESOURCE") as? Resource

        if (resource == null) {
            finish()
            return
        }

        setupData()
        setupPermissions()
    }

    private fun setupData() {
        resource?.let { res ->
            binding.tvDetailTitle.text = res.titulo
            binding.tvDetailType.text = "Tipo: ${res.tipo}"
            binding.tvDetailDescription.text = res.descripcion
            binding.tvDetailRating.text = String.format("★ %.1f / 5.0 (%d calificaciones)", res.calificacion, res.numCalificaciones)

            Glide.with(this)
                .load(res.imagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivDetailImage)

            binding.btnOpenLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.enlace))
                startActivity(intent)
            }

            binding.btnRate.setOnClickListener { showRatingDialog(res) }
        }
    }

    private fun setupPermissions() {
        val user = sessionManager.getUser()
        if (user?.role == UserRole.DOCENTE) {
            binding.layoutDocenteActions.visibility = View.VISIBLE

            binding.btnEdit.setOnClickListener {
                val intent = Intent(this, AddEditResourceActivity::class.java)
                intent.putExtra("RESOURCE", resource)
                startActivity(intent)
                finish()
            }

            binding.btnDelete.setOnClickListener { confirmDelete() }
        } else {
            binding.layoutDocenteActions.visibility = View.GONE
        }
    }

    private fun showRatingDialog(res: Resource) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.rate_dialog_title))

        val input = EditText(this)
        input.hint = "Ejemplo: 5"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        input.layoutParams = lp
        builder.setView(input)

        builder.setPositiveButton("Enviar") { dialog, _ ->
            val ratingVal = input.text.toString().toDoubleOrNull()
            if (ratingVal != null && ratingVal in 1.0..5.0) {
                val newCount = res.numCalificaciones + 1
                val newAvg = ((res.calificacion * res.numCalificaciones) + ratingVal) / newCount
                res.calificacion = newAvg
                res.numCalificaciones = newCount

                RetrofitClient.instance.updateResource(res.id, res).enqueue(object : Callback<Resource> {
                    override fun onResponse(call: Call<Resource>, response: Response<Resource>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ResourceDetailActivity, "¡Calificación enviada!", Toast.LENGTH_SHORT).show()
                            setupData()
                        }
                    }
                    override fun onFailure(call: Call<Resource>, t: Throwable) {
                        Toast.makeText(this@ResourceDetailActivity, "Error al calificar", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Ingrese un número entre 1 y 5", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_msg))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                resource?.let { res ->
                    RetrofitClient.instance.deleteResource(res.id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Toast.makeText(this@ResourceDetailActivity, "Recurso eliminado correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@ResourceDetailActivity, "Error al eliminar el recurso", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
