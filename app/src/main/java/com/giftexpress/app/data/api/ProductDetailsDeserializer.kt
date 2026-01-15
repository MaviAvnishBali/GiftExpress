package com.giftexpress.app.data.api

import com.giftexpress.app.data.model.ProductDetailsResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ProductDetailsDeserializer : JsonDeserializer<ProductDetailsResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ProductDetailsResponse {
        if (json.isJsonArray) {
            val jsonArray = json.asJsonArray
            if (jsonArray.size() >= 3) {
                // The actual data is at index 2
                val dataElement = jsonArray.get(2)
                return context.deserialize(dataElement, ProductDetailsResponse::class.java)
            } else {
                throw JsonParseException("Unexpected JSON array size for ProductDetailsResponse")
            }
        } else if (json.isJsonObject) {
            // Fallback if it's already an object (e.g. if API changes)
            // But wait, if I call context.deserialize here on the same type, it might recurse if not careful.
            // However, since we are inside the deserializer registered for ProductDetailsResponse, 
            // calling context.deserialize(json, ProductDetailsResponse::class.java) WILL recurse.
            // We should use a separate Gson instance or manual parsing if we hit this case, 
            // OR we assume the array structure is the only one we handle with this deserializer.
            
            // Actually, the default behavior for `context.deserialize` is to look up the adapter. 
            // If this adapter is registered for ProductDetailsResponse, it will loop.
            // To avoid this, we can parse it manually or use a new Gson instance.
            // But for the array case, we are passing a DIFFERENT JsonElement (the inner object).
            // Does context.deserialize dispatch back to this deserializer?
            // Yes, if the type is ProductDetailsResponse.
            
            // So, I cannot use context.deserialize(dataElement, ProductDetailsResponse::class.java) 
            // because it will come back here.
            
            // Solution: 
            // 1. Define the inner object as a separate class (e.g. ProductDetailsData) and have ProductDetailsResponse wrap it? 
            //    No, ProductDetailsResponse IS the data.
            // 2. Use `Gson()` inside to parse the inner object.
            
            val gson = com.google.gson.Gson()
            return gson.fromJson(json, ProductDetailsResponse::class.java)
        }
        throw JsonParseException("Expected JSON Array for ProductDetailsResponse")
    }
}
