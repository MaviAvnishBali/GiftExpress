package com.giftexpress.app.data.api

import com.giftexpress.app.data.model.CategoryOfferAndBannerResponse
import com.giftexpress.app.data.model.SliderResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class CategoryOfferAndBannerDeserializer : JsonDeserializer<CategoryOfferAndBannerResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CategoryOfferAndBannerResponse {
        if (json.isJsonArray) {
            val jsonArray = json.asJsonArray
            if (jsonArray.size() >= 3) {
                // The actual data is at index 2
                val dataElement = jsonArray.get(2)
                val slidersArray: Array<SliderResponse> = context.deserialize(dataElement, Array<SliderResponse>::class.java)
                return CategoryOfferAndBannerResponse(slidersArray.toList())
            } else {
                throw JsonParseException("Unexpected JSON array size for CategoryOfferAndBannerResponse")
            }
        }
        throw JsonParseException("Expected JSON Array for CategoryOfferAndBannerResponse")
    }
}
