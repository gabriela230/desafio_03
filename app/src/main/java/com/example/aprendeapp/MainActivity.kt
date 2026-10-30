package com.example.aprendeapp

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.controller.ResourceController
import com.example.aprendeapp.model.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var auth: AuthController
    private val controller = ResourceController()
    private var currentEmail = ""
    private var currentRole = "Estudiante"
    private var allResources: List<Resource> = emptyList()
    private val favorites = mutableSetOf<Int>()
    private lateinit var root: LinearLayout
    private lateinit var listArea: LinearLayout
    private var showingFavorites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = AuthController(this)
        favorites.addAll(getSharedPreferences("favorites", MODE_PRIVATE).getStringSet("ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList())
        val saved = getSharedPreferences("session", MODE_PRIVATE)
        currentEmail = saved.getString("email", "") ?: ""
        currentRole = saved.getString("role", "Estudiante") ?: "Estudiante"
        if (currentEmail.isNotBlank()) showHome() else showLogin()
    }

    private fun base(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 22, 24, 16)
            setBackgroundColor(0xFFF5F7FB.toInt())
        }
        val heading = TextView(this).apply {
            text = title; textSize = 24f; setTextColor(0xFF173B75.toInt())
            typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 18)
        }
        root.addView(heading)
        setContentView(root)
    }

    private fun field(hint: String, secret: Boolean = false): EditText =
        TextInputEditText(this).apply {
            this.hint = hint; textSize = 16f; setSingleLine(true)
            if (secret) inputType = 129
            setPadding(14, 12, 14, 12)
            background = getDrawable(android.R.drawable.edit_text)
        }

    private fun button(label: String, action: () -> Unit) = MaterialButton(this).apply {
        text = label; isAllCaps = false; setOnClickListener { action() }
    }

    private fun showLogin() {
        base(getString(R.string.login_title))
        root.addView(TextView(this).apply { text = getString(R.string.welcome_message); textSize = 17f })
        val email = field(getString(R.string.email))
        val password = field(getString(R.string.password), true)
        root.addView(email); root.addView(password)
        root.addView(button(getString(R.string.login)) {
            val user = auth.login(email.text.toString().trim(), password.text.toString())
            if (user == null) toast(getString(R.string.invalid_credentials)) else {
                currentEmail = user.email; currentRole = user.role
                getSharedPreferences("session", MODE_PRIVATE).edit()
                    .putString("email", currentEmail).putString("role", currentRole).apply()
                showHome()
            }
        })
        root.addView(button(getString(R.string.register)) { showRegister() })
    }

    private fun showRegister() {
        base(getString(R.string.register_title))
        val email = field(getString(R.string.email))
        val password = field(getString(R.string.password), true)
        val role = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf(getString(R.string.student), getString(R.string.teacher)))
        }
        root.addView(email); root.addView(password); root.addView(role)
        root.addView(TextView(this).apply { text = getString(R.string.password_rules); textSize = 12f })
        root.addView(button(getString(R.string.register)) {
            val selected = role.selectedItem.toString()
            val error = auth.register(email.text.toString().trim(), password.text.toString(), selected)
            when (error) {
                "email" -> toast(getString(R.string.invalid_email))
                "password" -> toast(getString(R.string.password_rules))
                "exists" -> toast(getString(R.string.account_exists))
                else -> { toast(getString(R.string.register)); showLogin() }
            }
        })
        root.addView(button(getString(R.string.cancel)) { showLogin() })
    }

    private fun showHome() {
        base(getString(R.string.resources))
        root.addView(TextView(this).apply {
            text = "$currentEmail • $currentRole"; textSize = 13f; setTextColor(0xFF555555.toInt())
        })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(button(getString(R.string.resources)) { showingFavorites = false; renderList() }, LinearLayout.LayoutParams(0, -2, 1f))
        actions.addView(button(getString(R.string.favorites)) { showingFavorites = true; renderList() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(actions)
        val search = field(getString(R.string.search_hint))
        root.addView(search)
        val filter = field(getString(R.string.filter_hint))
        root.addView(filter)
        search.addTextChangedListener(SimpleWatcher { renderList(search.text.toString(), filter.text.toString()) })
        filter.addTextChangedListener(SimpleWatcher { renderList(search.text.toString(), filter.text.toString()) })
        root.addView(button(getString(R.string.sort_title)) {
            allResources = allResources.sortedBy { it.titulo.lowercase() }; renderList()
        })
        if (currentRole == getString(R.string.teacher)) {
            root.addView(button(getString(R.string.add_resource)) { resourceDialog(null) })
        }
        root.addView(button(getString(R.string.logout)) { logout() })
        listArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listArea) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        loadResources()
    }

    private fun loadResources() {
        toast(getString(R.string.loading))
        lifecycleScope.launch {
            try { allResources = controller.list(); renderList() }
            catch (_: Exception) { toast(getString(R.string.api_error)); renderList() }
        }
    }

    private fun renderList(query: String = "", type: String = "") {
        if (!::listArea.isInitialized) return
        listArea.removeAllViews()
        val q = query.trim().lowercase()
        val items = allResources.filter {
            (!showingFavorites || favorites.contains(it.id)) &&
            (q.isBlank() || it.id.toString().contains(q) || it.titulo.lowercase().contains(q) || it.tipo.lowercase().contains(q)) &&
            (type.isBlank() || it.tipo.contains(type, true))
        }
        if (items.isEmpty()) {
            listArea.addView(TextView(this).apply { text = if (showingFavorites) getString(R.string.no_favorites) else getString(R.string.empty); setPadding(8, 24, 8, 24) })
        }
        items.forEach { item ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(16, 14, 16, 14)
                setBackgroundColor(0xFFFFFFFF.toInt())
            }
            card.addView(TextView(this).apply {
                text = "${item.titulo}  (#${item.id})"; textSize = 18f; typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF173B75.toInt())
            })
            card.addView(TextView(this).apply { text = "${item.tipo} • ${item.descripcion}"; textSize = 14f; setPadding(0, 5, 0, 5) })
            card.addView(TextView(this).apply { text = "${getString(R.string.average_rating)}: %.1f/5".format(item.average()); textSize = 13f })
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(button(getString(R.string.open_link)) {
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.enlace))) } catch (_: Exception) { toast(item.enlace) }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(button(if (favorites.contains(item.id)) "★" else "☆") {
                if (!favorites.add(item.id)) favorites.remove(item.id)
                getSharedPreferences("favorites", MODE_PRIVATE).edit().putStringSet("ids", favorites.map { it.toString() }.toSet()).apply()
                renderList(query, type)
            })
            row.addView(button(getString(R.string.rate)) { ratingDialog(item) })
            card.addView(row)
            if (currentRole == getString(R.string.teacher)) {
                val admin = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                admin.addView(button(getString(R.string.edit_resource)) { resourceDialog(item) }, LinearLayout.LayoutParams(0, -2, 1f))
                admin.addView(button(getString(R.string.delete_resource)) { confirmDelete(item) }, LinearLayout.LayoutParams(0, -2, 1f))
                card.addView(admin)
            }
            val params = LinearLayout.LayoutParams(-1, -2); params.setMargins(0, 0, 0, 14)
            listArea.addView(card, params)
        }
    }

    private fun resourceDialog(item: Resource?) {
        if (currentRole != getString(R.string.teacher)) { toast(getString(R.string.teacher_only)); return }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 0) }
        val title = field(getString(R.string.title)); val desc = field(getString(R.string.description))
        val type = field(getString(R.string.type)); val link = field(getString(R.string.link)); val image = field(getString(R.string.image))
        listOf(title, desc, type, link, image).forEach { box.addView(it) }
        item?.let { title.setText(it.titulo); desc.setText(it.descripcion); type.setText(it.tipo); link.setText(it.enlace); image.setText(it.imagen) }
        AlertDialog.Builder(this).setTitle(if (item == null) getString(R.string.add_resource) else getString(R.string.edit_resource))
            .setView(box).setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                if (listOf(title, desc, type, link).any { it.text.isNullOrBlank() } ||
                    !android.util.Patterns.WEB_URL.matcher(link.text.toString()).matches()) {
                    toast(getString(R.string.resource_form)); return@setPositiveButton
                }
                val resource = Resource(item?.id ?: 0, title.text.toString(), desc.text.toString(),
                    type.text.toString(), link.text.toString(), image.text.toString())
                lifecycleScope.launch {
                    try {
                        if (item == null) controller.create(resource) else controller.update(resource)
                        toast(getString(R.string.resource_saved)); loadResources()
                    } catch (_: Exception) { toast(getString(R.string.api_error)) }
                }
            }.show()
    }

    private fun confirmDelete(item: Resource) {
        AlertDialog.Builder(this).setMessage(getString(R.string.confirm_delete))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete_resource)) { _, _ ->
                lifecycleScope.launch {
                    try { controller.delete(item.id); loadResources() }
                    catch (_: Exception) { toast(getString(R.string.api_error)) }
                }
            }.show()
    }

    private fun ratingDialog(item: Resource) {
        val choices = arrayOf("1 ★", "2 ★", "3 ★", "4 ★", "5 ★")
        AlertDialog.Builder(this).setTitle(getString(R.string.rate)).setItems(choices) { _, which ->
            lifecycleScope.launch {
                try { controller.rate(item, which + 1); toast(getString(R.string.rating_saved)); loadResources() }
                catch (_: Exception) { toast(getString(R.string.api_error)) }
            }
        }.show()
    }

    private fun logout() {
        AlertDialog.Builder(this).setMessage(getString(R.string.logout_confirm))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply()
                currentEmail = ""; showLogin()
            }.show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

private class SimpleWatcher(val changed: () -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { changed() }
    override fun afterTextChanged(s: android.text.Editable?) {}
}

private fun LinearLayout.addView(view: View, params: LinearLayout.LayoutParams) {
    this.addView(view, params as android.view.ViewGroup.LayoutParams)
}
