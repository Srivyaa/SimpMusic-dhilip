package com.maxrave.simpmusic.extension

import com.maxrave.domain.data.model.browse.album.Track

/**
 * Extension function to check if a track matches a search query.
 * This function performs a comprehensive search across multiple track properties.
 * 
 * @param query The search query to match against (should be lowercase and trimmed)
 * @return true if the track matches the query, false otherwise
 */
fun Track.matchesSearchQuery(query: String): Boolean {
    if (query.isBlank()) return false
    
    // Search in track title
    val titleMatch = title.lowercase().contains(query)
    
    // Search in artist names
    val artistMatch = artists?.any { artist ->
        artist.name.lowercase().contains(query)
    } ?: false
    
    // Search in album name
    val albumMatch = album?.name?.lowercase()?.contains(query) ?: false
    
    // Search in year
    val yearMatch = year?.lowercase()?.contains(query) ?: false
    
    // Return true if any of the properties match
    return titleMatch || artistMatch || albumMatch || yearMatch
}