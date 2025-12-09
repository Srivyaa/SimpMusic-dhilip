package com.maxrave.media3.service.callback

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.maxrave.domain.data.model.searchResult.albums.AlbumsResult
import com.maxrave.domain.data.model.searchResult.artists.ArtistsResult
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.model.searchResult.songs.SongsResult
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.repository.AlbumRepository
import com.maxrave.domain.repository.HomeRepository
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.PodcastRepository
import com.maxrave.domain.repository.PlaylistRepository
import com.maxrave.domain.repository.SearchRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.repository.StreamRepository
import com.maxrave.domain.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class SimpleMediaSessionCallbackTest {

    private lateinit var callback: SimpleMediaSessionCallback
    private val mockContext = mockk<android.content.Context>(relaxed = true)
    private val mockScope = CoroutineScope(Dispatchers.IO)
    private val mockMediaPlayerHandler = mockk<MediaPlayerHandler>(relaxed = true)
    private val mockSearchRepository = mockk<SearchRepository>(relaxed = true)
    private val mockSongRepository = mockk<SongRepository>(relaxed = true)
    private val mockLocalPlaylistRepository = mockk<LocalPlaylistRepository>(relaxed = true)
    private val mockPlaylistRepository = mockk<PlaylistRepository>(relaxed = true)
    private val mockHomeRepository = mockk<HomeRepository>(relaxed = true)
    private val mockStreamRepository = mockk<StreamRepository>(relaxed = true)
    private val mockPodcastRepository = mockk<PodcastRepository>(relaxed = true)
    private val mockAlbumRepository = mockk<AlbumRepository>(relaxed = true)

    @Before
    fun setup() {
        callback = SimpleMediaSessionCallback(
            mockContext,
            mockScope,
            mockMediaPlayerHandler,
            mockSearchRepository,
            mockSongRepository,
            mockLocalPlaylistRepository,
            mockPlaylistRepository,
            mockHomeRepository,
            mockStreamRepository,
            mockPodcastRepository,
            mockAlbumRepository
        )
    }

    @Test
    fun `test search results are retained when same query is searched`() = runTest {
        // Setup mock data
        val testQuery = "test query"
        val mockSongs = listOf(
            SongsResult(
                videoId = "1",
                title = "Test Song 1",
                artists = emptyList(),
                thumbnails = emptyList(),
                isExplicit = false,
                duration = "3:00",
                durationSeconds = 180,
                videoType = "MUSIC_VIDEO_TYPE_ATV"
            )
        )

        val mockPlaylists = listOf(
            PlaylistsResult(
                browseId = "PL1",
                title = "Test Playlist 1",
                author = "Test Author",
                thumbnails = emptyList(),
                videoCount = "10",
                videoCountText = "10 videos"
            )
        )

        // Mock repository responses
        coEvery { mockSearchRepository.getSearchDataSong(testQuery) } returns flowOf(
            Resource.Success(mockSongs)
        )
        coEvery { mockSearchRepository.getSearchDataPlaylist(testQuery) } returns flowOf(
            Resource.Success(mockPlaylists)
        )
        coEvery { mockSearchRepository.getSearchDataFeaturedPlaylist(testQuery) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataAlbum(testQuery) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataArtist(testQuery) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataPodcast(testQuery) } returns flowOf(
            Resource.Success(emptyList())
        )

        // First search
        val firstSearchResult = callback.onSearch(
            mockk(relaxed = true),
            mockk(relaxed = true),
            testQuery,
            null
        ).get()

        assertNotNull(firstSearchResult)
        assertEquals(LibraryResult.RESULT_SUCCESS, firstSearchResult.resultCode)

        // Verify search results are populated
        val field = callback.javaClass.getDeclaredField("searchTempList")
        field.isAccessible = true
        val searchTempList = field.get(callback) as List<*>
        assertEquals(1, searchTempList.size)

        val playlistField = callback.javaClass.getDeclaredField("searchPlaylistResults")
        playlistField.isAccessible = true
        val searchPlaylistResults = playlistField.get(callback) as List<*>
        assertEquals(1, searchPlaylistResults.size)

        // Second search with same query - should retain results
        val secondSearchResult = callback.onSearch(
            mockk(relaxed = true),
            mockk(relaxed = true),
            testQuery,
            null
        ).get()

        assertNotNull(secondSearchResult)
        assertEquals(LibraryResult.RESULT_SUCCESS, secondSearchResult.resultCode)

        // Verify search results are still populated (not cleared)
        val searchTempListAfter = field.get(callback) as List<*>
        assertEquals(1, searchTempListAfter.size)

        val searchPlaylistResultsAfter = playlistField.get(callback) as List<*>
        assertEquals(1, searchPlaylistResultsAfter.size)
    }

    @Test
    fun `test search results are cleared when different query is searched`() = runTest {
        // Setup mock data for first query
        val firstQuery = "first query"
        val mockSongs = listOf(
            SongsResult(
                videoId = "1",
                title = "Test Song 1",
                artists = emptyList(),
                thumbnails = emptyList(),
                isExplicit = false,
                duration = "3:00",
                durationSeconds = 180,
                videoType = "MUSIC_VIDEO_TYPE_ATV"
            )
        )

        // Setup mock data for second query
        val secondQuery = "second query"
        val mockSongs2 = listOf(
            SongsResult(
                videoId = "2",
                title = "Test Song 2",
                artists = emptyList(),
                thumbnails = emptyList(),
                isExplicit = false,
                duration = "4:00",
                durationSeconds = 240,
                videoType = "MUSIC_VIDEO_TYPE_ATV"
            )
        )

        // Mock repository responses
        coEvery { mockSearchRepository.getSearchDataSong(firstQuery) } returns flowOf(
            Resource.Success(mockSongs)
        )
        coEvery { mockSearchRepository.getSearchDataSong(secondQuery) } returns flowOf(
            Resource.Success(mockSongs2)
        )
        coEvery { mockSearchRepository.getSearchDataPlaylist(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataFeaturedPlaylist(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataAlbum(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataArtist(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataPodcast(any()) } returns flowOf(
            Resource.Success(emptyList())
        )

        // First search
        callback.onSearch(
            mockk(relaxed = true),
            mockk(relaxed = true),
            firstQuery,
            null
        ).get()

        // Verify search results are populated
        val field = callback.javaClass.getDeclaredField("searchTempList")
        field.isAccessible = true
        var searchTempList = field.get(callback) as List<*>
        assertEquals(1, searchTempList.size)
        assertEquals("1", (searchTempList[0] as com.maxrave.domain.data.model.browse.album.Track).videoId)

        // Second search with different query - should clear and repopulate results
        callback.onSearch(
            mockk(relaxed = true),
            mockk(relaxed = true),
            secondQuery,
            null
        ).get()

        // Verify search results are updated with new data
        searchTempList = field.get(callback) as List<*>
        assertEquals(1, searchTempList.size)
        assertEquals("2", (searchTempList[0] as com.maxrave.domain.data.model.browse.album.Track).videoId)
    }

    @Test
    fun `test clear search action clears all search results`() = runTest {
        // Setup mock data
        val testQuery = "test query"
        val mockSongs = listOf(
            SongsResult(
                videoId = "1",
                title = "Test Song 1",
                artists = emptyList(),
                thumbnails = emptyList(),
                isExplicit = false,
                duration = "3:00",
                durationSeconds = 180,
                videoType = "MUSIC_VIDEO_TYPE_ATV"
            )
        )

        // Mock repository responses
        coEvery { mockSearchRepository.getSearchDataSong(testQuery) } returns flowOf(
            Resource.Success(mockSongs)
        )
        coEvery { mockSearchRepository.getSearchDataPlaylist(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataFeaturedPlaylist(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataAlbum(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataArtist(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        coEvery { mockSearchRepository.getSearchDataPodcast(any()) } returns flowOf(
            Resource.Success(emptyList())
        )

        // Perform search
        callback.onSearch(
            mockk(relaxed = true),
            mockk(relaxed = true),
            testQuery,
            null
        ).get()

        // Verify search results are populated
        val field = callback.javaClass.getDeclaredField("searchTempList")
        field.isAccessible = true
        var searchTempList = field.get(callback) as List<*>
        assertEquals(1, searchTempList.size)

        // Clear search
        val clearResult = callback.onCustomCommand(
            mockk(relaxed = true),
            mockk(relaxed = true),
            SessionCommand("clear_search", Bundle()),
            Bundle()
        ).get()

        assertEquals(SessionResult.RESULT_SUCCESS, clearResult.resultCode)

        // Verify search results are cleared
        searchTempList = field.get(callback) as List<*>
        assertEquals(0, searchTempList.size)

        val playlistField = callback.javaClass.getDeclaredField("searchPlaylistResults")
        playlistField.isAccessible = true
        val searchPlaylistResults = playlistField.get(callback) as List<*>
        assertEquals(0, searchPlaylistResults.size)
    }
}