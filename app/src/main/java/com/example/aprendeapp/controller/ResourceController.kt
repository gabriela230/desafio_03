package com.example.aprendeapp.controller

import com.example.aprendeapp.model.Resource
import com.example.aprendeapp.network.ApiClient

class ResourceController {
    suspend fun list() = ApiClient.service.getResources()
    suspend fun create(item: Resource) = ApiClient.service.createResource(item)
    suspend fun update(item: Resource) = ApiClient.service.updateResource(item.id, item)
    suspend fun delete(id: Int) = ApiClient.service.deleteResource(id)
    suspend fun rate(item: Resource, stars: Int): Resource {
        val updated = item.copy(ratingTotal = item.ratingTotal + stars, ratingCount = item.ratingCount + 1)
        return ApiClient.service.updateRating(item.id, mapOf("ratingTotal" to updated.ratingTotal, "ratingCount" to updated.ratingCount))
    }
}
