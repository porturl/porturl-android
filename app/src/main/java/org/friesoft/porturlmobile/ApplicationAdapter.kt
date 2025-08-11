package org.friesoft.porturlmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.friesoft.porturlmobile.model.Application

class ApplicationAdapter(
    val onDelete: (Application) -> Unit,
    val onEdit: (Application) -> Unit
) : RecyclerView.Adapter<ApplicationAdapter.AppViewHolder>() {

    private val items = mutableListOf<Application>()

    fun submitList(list: List<Application>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false)
        return AppViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = items[position]
        holder.name.text = app.name
        holder.url.text = app.url
        holder.deleteBtn.setOnClickListener { onDelete(app) }
        holder.editBtn.setOnClickListener { onEdit(app) }
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.appName)
        val url: TextView = view.findViewById(R.id.appUrl)
        val deleteBtn: Button = view.findViewById(R.id.deleteButton)
        val editBtn: Button = view.findViewById(R.id.editButton)
    }
}