package com.example.kltn.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kltn.*
import com.example.kltn.extensions.toLiveData
import com.example.kltn.models.EventModel
import com.google.gson.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.*


class EventService {
    companion object {
        fun get(userId: Int, groupId: Int, month: Int, year: Int): Triple<String, Array<EventInfos>?, Int> {
            if (userId == -1) return Triple("", null, 0)
            var gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(Common.API_HOST + "api/Event/GetByMonth?userId=" + userId + "&groupId=" + groupId + "&month=" + month + "&year=" + year)
                .build()
            try {
                var response = client.newCall(request).execute()
                var statusCode = response.code()
                var responseBody = response.body()?.string()
                if (statusCode == 200) {
                    var events:Array<EventInfos> = gson.fromJson(responseBody, (ArrayList<EventInfos>()).toTypedArray().javaClass)
                    Log.d("events", "events = $events")
                    return Triple("",events , 0)
                }
                else if (statusCode == 400) {
                    if (responseBody == null || responseBody == "") {
                        return Triple("Đã xảy ra lỗi. Vui lòng thử lại sau", null, 0)
                    }
                    return Triple(responseBody, null, 0)
                }
                else {
                    return Triple("Đã xảy ra lỗi. Vui lòng thử lại sau", null, 0)
                }
            } catch (ex: Exception) {
                return Triple("Đã xảy ra lỗi. Vui lòng thử lại sau", null, 0)
            }
        }

        fun update(
            eventId: Int,
            title: String,
            description: String,
            startTime: Date,
            endTime: Date,
            recurrenceType: Int,
            groupId: Int,
            participants: List<Int>,
            creatorId: Int,
        ): String {
            var event = EventInfos()
            event.id = eventId
            event.title = title
            event.description = description
            event.startTime = startTime
            event.endTime = endTime
            event.recurrenceType = recurrenceType
            event.groupId = groupId
            event.creator = UserInfos()
            event.creator.id = creatorId
            for (participant in participants) {
                var u = UserInfos()
                u.id = participant
                event.participants.add(u)
            }
            var Json = MediaType.parse("application/json; charset=utf-8")
            var gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
            var requestBody = RequestBody.create(Json, gson.toJson(event))
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(Common.API_HOST + "api/Event/InsertOrUpdate")
                .post(requestBody)
                .build()
            return try {
                var response = client.newCall(request).execute()
                var statusCode = response.code()
                var responseBody = response.body()?.string()
                if (statusCode == 200) ""
                else if (statusCode == 400 && responseBody != null) responseBody
                else if (statusCode == 401) "Phiên đăng nhập của bạn đã hết hạn.";
                else "Đã xảy ra lỗi. Vui lòng thử lại sau."
            } catch (ex: Exception) {
                "Đã xảy ra lỗi. Vui lòng thử lại sau.";
            }
        }

        fun getById(eventId: Int): Triple<String, EventModel?, Int> {
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(Common.API_HOST + "api/Event/" + eventId)
                .build()
            try {
                var response = client.newCall(request).execute()
                var statusCode = response.code()
                var responseBody = response.body()?.string()
                if (statusCode == 200) {
                    var event: EventModel = gson.fromJson(responseBody, EventModel::class.java)
                    return Triple("", event, 0)
                } else if (statusCode == 400) {
                    if (responseBody == null || responseBody == "") {
                        return Triple("Đã xảy ra lỗi. Vui lòng thử lại sau", null, 0)
                    }
                    return Triple(responseBody, null, 0)
                } else {
                    return Triple("Đã xảy ra lỗi. Vui lòng thử lại sau", null, 0)
                }
            } catch (ex: Exception) {
                return Triple("Đã xảy ra lỗi. Vui lòng thử lại sau", null, 0)
            }
        }
    }
}
