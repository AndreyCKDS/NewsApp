package com.almazov.restapi.screens.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.almazov.restapi.R
import com.almazov.restapi.model.Article
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_article.view.*
import kotlinx.coroutines.*

class NewsAdapter(private val viewModel: NewsAdapterViewModel): RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(view: View): RecyclerView.ViewHolder(view)

    private val callback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_article,parent,false)
        )
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article =  differ.currentList[position]

        holder.itemView.apply {
            Glide.with(this).load(article.urlToImage).error(R.drawable.no_image_sample).into(article_image)
            article_image.clipToOutline = true
            article_title.text = article.title
            article_date.text = article.publishedAt

            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }

            MainScope().launch {
                val favourite = MainScope().async {viewModel.checkFavouriteUrl(article.url)}
                icon_favourite.isChecked = favourite.await()
            }
            icon_favourite.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.saveFavouriteArticle(article)
                } else {
                    viewModel.deleteFromFavouriteArticle(article)
                }
            }

            icon_share.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.apply {
                    type = "text/plain"
                    putExtra("Share this", article.url)
                }
                val chooser = Intent.createChooser(intent,"Share using...")
                context.startActivity(chooser)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((Article) -> Unit)? = null

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }
}