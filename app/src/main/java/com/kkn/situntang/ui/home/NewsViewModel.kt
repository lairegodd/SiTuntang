package com.kkn.situntang.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.situntang.model.News
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL

class NewsViewModel : ViewModel() {
    private val _newsList = MutableStateFlow<List<News>>(emptyList())
    val newsList = _newsList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchRssFeed()
    }

    fun fetchRssFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val news = withContext(Dispatchers.IO) {
                    parseRss(URL("https://tuntang-desa.web.id/feed"))
                }
                _newsList.value = news
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to manual latest news if feed fails
                _newsList.value = listOf(
                    News(
                        title = "MUSYAWARAH DESA (MUSDES) LAPORAN PERTANGGUNGJAWABAN REALISASI APB DESA TAHUN ANGGARAN 2024",
                        date = "31 Jan 2025",
                        description = "Pemerintah Desa Tuntang menyelenggarakan Musyawarah Desa (Musdes) dalam rangka Laporan Pertanggungjawaban Realisasi APB Desa Tahun Anggaran 2024 di Aula Balai Desa.",
                        imageUrl = "https://tuntang-desa.web.id/assets/images/berita/logo_desa.png",
                        link = "https://tuntang-desa.web.id/artikel/2025/1/31/musyawarah-desa-musdes-laporan-pertanggungjawaban-realisasi-apb-desa-tahun-anggaran-2024"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseRss(url: URL): List<News> {
        val newsItems = mutableListOf<News>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(url.openStream(), "UTF-8")

        var eventType = parser.eventType
        var currentTitle = ""
        var currentLink = ""
        var currentPubDate = ""
        var currentDescription = ""
        var insideItem = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName.equals("item", ignoreCase = true)) {
                        insideItem = true
                    } else if (insideItem) {
                        when (tagName.lowercase()) {
                            "title" -> currentTitle = parser.nextText()
                            "link" -> currentLink = parser.nextText()
                            "pubdate" -> currentPubDate = parser.nextText()
                            "description" -> {
                                val rawDesc = parser.nextText()
                                // Simple HTML tag removal for description
                                currentDescription = rawDesc.replace(Regex("<[^>]*>"), "").take(150) + "..."
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName.equals("item", ignoreCase = true)) {
                        newsItems.add(
                            News(
                                title = currentTitle,
                                date = formatRssDate(currentPubDate),
                                description = currentDescription,
                                imageUrl = "https://tuntang-desa.web.id/assets/images/berita/logo_desa.png",
                                link = currentLink
                            )
                        )
                        insideItem = false
                    }
                }
            }
            eventType = parser.next()
        }
        return newsItems
    }

    private fun formatRssDate(rawDate: String): String {
        return try {
            // Typical RSS format: Tue, 31 Jan 2025 09:00:00 +0000
            // Return just the date part for simplicity
            rawDate.split(" ").filterIndexed { index, _ -> index in 1..3 }.joinToString(" ")
        } catch (e: Exception) {
            rawDate
        }
    }
}
