package com.giftexpress.app

import com.giftexpress.app.data.model.DeleteAccountRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DeleteAccountRequestTest {

    private val gson = Gson()

    @Test
    fun `test DeleteAccountRequest serialization matches required payload`() {
        val request = DeleteAccountRequest(password = "Password123")
        val json = gson.toJson(request)

        val jsonObject = gson.fromJson(json, JsonObject::class.java)
        assertNotNull(jsonObject.get("password"))
        assertEquals("Password123", jsonObject.get("password").asString)
    }

    @Test
    fun `test DeleteAccountRequest serialization without password produces empty json object`() {
        val request = DeleteAccountRequest()
        val json = gson.toJson(request)

        assertEquals("{}", json)
    }

    @Test
    fun `test 400 Bad Request message parsing from error response`() {
        val errorJson = """{"message": "The password you entered is incorrect."}"""
        val jsonObject = gson.fromJson(errorJson, JsonObject::class.java)
        val extractedMessage = jsonObject.get("message")?.asString

        assertEquals("The password you entered is incorrect.", extractedMessage)
    }
}
