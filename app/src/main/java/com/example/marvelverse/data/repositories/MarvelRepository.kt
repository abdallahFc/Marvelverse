package com.example.marvelverse.data.repositories

import com.example.marvelverse.data.dataSources.local.FakeLocalData
import com.example.marvelverse.data.dataSources.local.MarvelDatabase
import com.example.marvelverse.data.dataSources.remote.RetrofitClient
import com.example.marvelverse.domain.entities.Character
import com.example.marvelverse.domain.entities.Comic
import com.example.marvelverse.domain.entities.Event
import com.example.marvelverse.domain.entities.Series
import com.example.marvelverse.domain.mapper.CharacterMapper
import com.example.marvelverse.domain.mapper.CharacterSearchEntityToCharacterMapper
import com.example.marvelverse.domain.mapper.CharacterToCharacterSearchEntityMapper
import com.example.marvelverse.domain.mapper.ComicMapper
import com.example.marvelverse.domain.mapper.ComicSearchEntityToComicMapper
import com.example.marvelverse.domain.mapper.ComicToComicSearchEntityMapper
import com.example.marvelverse.domain.mapper.EventMapper
import com.example.marvelverse.domain.mapper.EventSearchEntityToEventMapper
import com.example.marvelverse.domain.mapper.EventToEventSearchEntityMapper
import com.example.marvelverse.domain.mapper.SeriesMapper
import io.reactivex.rxjava3.core.Single


object MarvelRepository {
    private val marvelApiServices by lazy {
        RetrofitClient.marvelApiServices
    }
    private val characterMapper = CharacterMapper()
    private val charToCharSearchEntityMapper = CharacterToCharacterSearchEntityMapper()
    private val charSearchEntityToCharMapper = CharacterSearchEntityToCharacterMapper()
    private val comicToComicSearchEntityMapper = ComicToComicSearchEntityMapper()
    private val comicSearchEntityToComicMapper = ComicSearchEntityToComicMapper()
    private val eventToEventSearchEntity = EventToEventSearchEntityMapper()
    private val eventSearchEntityToEventMapper = EventSearchEntityToEventMapper()
    private val comicMapper = ComicMapper()
    private val eventMapper = EventMapper()
    private val seriesMapper = SeriesMapper()
    private val fakeLocalData = FakeLocalData()

    // TODO: Remove
    lateinit var db: MarvelDatabase

    fun searchComics(limit: Int? = null, title: String? = null): Single<List<Comic>> {
        return marvelApiServices.fetchComics(limit, title).map { baseResponse ->
            baseResponse.data?.results?.map { comicDto ->
                comicMapper.map(comicDto)
            } ?: emptyList()
        }
    }

    fun searchSeries(limit: Int? = null, title: String? = null): Single<List<Series>> {
        return marvelApiServices.fetchSeries(limit, title).map { baseResponse ->
            baseResponse.data?.results?.map { seriesDto ->
                seriesMapper.map(seriesDto)
            } ?: emptyList()
        }
    }

    fun searchCharacters(limit: Int? = null, title: String? = null): Single<List<Character>> {
        return marvelApiServices.fetchCharacters(limit, title).map { baseResponse ->
            baseResponse.data?.results?.map { characterDto ->
                characterMapper.map(characterDto)
            } ?: emptyList()
        }
    }

    fun searchCacheCharacters(limit: Int? = null, name: String): Single<List<Character>> {
        val savedChars = db.searchCharacterDao.getAllCharacters(name).blockingGet()
        return if (savedChars.isNotEmpty()) {
            Single.just(savedChars.map {
                charSearchEntityToCharMapper.map(it)
            })
        } else {
            val response = searchCharacters(limit, name)
            val cachedResponse = response.blockingGet().map {
                charToCharSearchEntityMapper.map(it)
            }
            db.searchCharacterDao.insertCharacters(cachedResponse).subscribe()
            return response
        }
    }

    fun searchCachedComics(limit: Int? = null, title: String): Single<List<Comic>> {
        val savedComics = db.searchComicDao.getAllComics(title).blockingGet()
        return if (savedComics.isNotEmpty()) {
            Single.just(savedComics.map {
                comicSearchEntityToComicMapper.map(it)
            })
        } else {
            val response = searchComics(limit, title)
            val cachedResponse = response.blockingGet().map {
                comicToComicSearchEntityMapper.map(it)
            }
            db.searchComicDao.insertComics(cachedResponse).subscribe()
            return response
        }
    }


    fun searchCachedEvents(limit: Int? = null, title: String): Single<List<Event>> {
        val savedEvents = db.searchEventDao.getAllEvents(title).blockingGet()
        return if (savedEvents.isNotEmpty()) {
            Single.just(savedEvents.map {
                eventSearchEntityToEventMapper.map(it)
            })
        } else {
            val response = searchEvents(limit, title)
            val cachedResponse = response.blockingGet().map {
                eventToEventSearchEntity.map(it)
            }
            db.searchEventDao.insertEvents(cachedResponse).subscribe()
            return response
        }
    }


    fun searchEvents(limit: Int? = null, title: String? = null): Single<List<Event>> {
        return marvelApiServices.fetchEvents(limit, title).map { baseResponse ->
            baseResponse.data?.results?.map { eventDto ->
                eventMapper.map(eventDto)
            } ?: emptyList()
        }
    }

    fun getComicsByUrl(url: String): Single<List<Comic>> {
        return marvelApiServices.fetchComicsByUrl(url).map { baseResponse ->
            baseResponse.data?.results?.map { comicDto ->
                comicMapper.map(comicDto)
            } ?: emptyList()
        }
    }

    fun getSeriesByUrl(url: String): Single<List<Series>> {
        return marvelApiServices.fetchSeriesByUrl(url).map { baseResponse ->
            baseResponse.data?.results?.map { seriesDto ->
                seriesMapper.map(seriesDto)
            } ?: emptyList()
        }
    }

    fun getCharactersByUrl(url: String): Single<List<Character>> {
        return marvelApiServices.fetchCharactersByUrl(url).map { baseResponse ->
            baseResponse.data?.results?.map { characterDto ->
                characterMapper.map(characterDto)
            } ?: emptyList()
        }
    }

    fun getEventsByUrl(url: String): Single<List<Event>> {
        return marvelApiServices.fetchEventsByUrl(url).map { baseResponse ->
            baseResponse.data?.results?.map { eventDto ->
                eventMapper.map(eventDto)
            } ?: emptyList()
        }
    }

    fun getRandomCharacters(): Single<List<Character>> {
        return marvelApiServices.fetchCharacters(80, null).map { baseResponse ->
            baseResponse.data?.results?.shuffled()?.take(20)?.map { characterDto ->
                characterMapper.map(characterDto)
            } ?: emptyList()
        }
    }

    fun getRandomComics(): Single<List<Comic>> {
        return marvelApiServices.fetchComics(50, null).map { baseResponse ->
            baseResponse.data?.results?.shuffled()?.take(10)?.map { comicDto ->
                comicMapper.map(comicDto)
            } ?: emptyList()
        }
    }

    fun getRandomSeries(): Single<List<Series>> {
        return marvelApiServices.fetchSeries(50, null).map { baseResponse ->
            baseResponse.data?.results?.shuffled()?.take(10)?.map { seriesDto ->
                seriesMapper.map(seriesDto)
            } ?: emptyList()
        }
    }

    fun getRandomEvents(): Single<List<Event>> {
        return marvelApiServices.fetchEvents(50, null).map { baseResponse ->
            baseResponse.data?.results?.shuffled()?.take(10)?.map { eventDto ->
                eventMapper.map(eventDto)
            } ?: emptyList()
        }
    }

    fun getItems() = fakeLocalData.getAboutItems()
}