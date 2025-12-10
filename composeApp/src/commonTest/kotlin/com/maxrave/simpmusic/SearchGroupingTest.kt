package com.maxrave.simpmusic

import com.maxrave.domain.data.model.searchResult.albums.AlbumsResult
import com.maxrave.domain.data.model.searchResult.artists.ArtistsResult
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.model.searchResult.songs.SongsResult
import com.maxrave.domain.data.model.searchResult.videos.VideosResult
import com.maxrave.domain.data.type.SearchResultType
import com.maxrave.simpmusic.viewModel.GroupedSearchResult
import com.maxrave.simpmusic.viewModel.SearchCategory
import com.maxrave.simpmusic.viewModel.SearchViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchGroupingTest {

    @Test
    fun `test search results grouping`() = runTest {
        // Create mock search results
        val mockResults = listOf<SearchResultType>(
            SongsResult(
                videoId = "song1",
                title = "Test Song 1",
                artists = emptyList(),
                thumbnails = emptyList(),
                duration = "3:30",
                isExplicit = false,
                album = null
            ),
            SongsResult(
                videoId = "song2",
                title = "Test Song 2",
                artists = emptyList(),
                thumbnails = emptyList(),
                duration = "4:15",
                isExplicit = false,
                album = null
            ),
            AlbumsResult(
                browseId = "album1",
                title = "Test Album",
                artists = emptyList(),
                thumbnails = emptyList(),
                year = "2023",
                isExplicit = false,
                type = "Album"
            ),
            ArtistsResult(
                browseId = "artist1",
                artist = "Test Artist",
                thumbnails = emptyList(),
                shuffleId = "shuffle1",
                radioId = "radio1"
            ),
            VideosResult(
                videoId = "video1",
                title = "Test Video",
                artists = emptyList(),
                thumbnails = emptyList(),
                duration = "5:30",
                isExplicit = false
            ),
            PlaylistsResult(
                browseId = "playlist1",
                title = "Test Playlist",
                author = "Test Author",
                thumbnails = emptyList(),
                videoCount = 10,
                resultType = "Playlist"
            ),
            PlaylistsResult(
                browseId = "podcast1",
                title = "Test Podcast",
                author = "Test Author",
                thumbnails = emptyList(),
                videoCount = 5,
                resultType = "Podcast"
            )
        )

        // Create a mock SearchViewModel
        val mockViewModel = object : SearchViewModel(
            dataStoreManager = MockDataStoreManager(),
            searchRepository = MockSearchRepository()
        ) {
            // Override the state to use our mock results
            override fun getGroupedSearchResults(): List<GroupedSearchResult> {
                return groupSearchResults(mockResults)
            }
        }

        // Test the grouping
        val groupedResults = mockViewModel.getGroupedSearchResults()

        // Verify we have the expected number of categories
        assertEquals(5, groupedResults.size, "Should have 5 categories")

        // Verify each category
        val categories = groupedResults.map { it.category }
        assertTrue(categories.contains(SearchCategory.SONGS), "Should contain SONGS category")
        assertTrue(categories.contains(SearchCategory.ALBUMS), "Should contain ALBUMS category")
        assertTrue(categories.contains(SearchCategory.ARTISTS), "Should contain ARTISTS category")
        assertTrue(categories.contains(SearchCategory.VIDEOS), "Should contain VIDEOS category")
        assertTrue(categories.contains(SearchCategory.PLAYLISTS), "Should contain PLAYLISTS category")

        // Verify song count
        val songsGroup = groupedResults.find { it.category == SearchCategory.SONGS }
        assertEquals(2, songsGroup?.items?.size, "Should have 2 songs")

        // Verify album count
        val albumsGroup = groupedResults.find { it.category == SearchCategory.ALBUMS }
        assertEquals(1, albumsGroup?.items?.size, "Should have 1 album")

        // Verify artist count
        val artistsGroup = groupedResults.find { it.category == SearchCategory.ARTISTS }
        assertEquals(1, artistsGroup?.items?.size, "Should have 1 artist")

        // Verify video count
        val videosGroup = groupedResults.find { it.category == SearchCategory.VIDEOS }
        assertEquals(1, videosGroup?.items?.size, "Should have 1 video")

        // Verify playlist count (should exclude podcasts)
        val playlistsGroup = groupedResults.find { it.category == SearchCategory.PLAYLISTS }
        assertEquals(1, playlistsGroup?.items?.size, "Should have 1 playlist (excluding podcasts)")

        // Verify podcast count
        val podcastsGroup = groupedResults.find { it.category == SearchCategory.PODCASTS }
        assertEquals(1, podcastsGroup?.items?.size, "Should have 1 podcast")
    }

    // Mock implementations for testing
    private class MockDataStoreManager : com.maxrave.domain.manager.DataStoreManager {
        // Implement minimal required methods
    }

    private class MockSearchRepository : com.maxrave.domain.repository.SearchRepository {
        // Implement minimal required methods
    }
}