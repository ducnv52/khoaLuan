package com.example.kltn

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.kltn.models.UserModel
import com.example.kltn.services.UserService
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.hours

class ActivityEditEvent : AppCompatActivity() {
    private lateinit var btnEditEventBack: TextView
    private lateinit var txtEditEventStartTime: TextView
    private lateinit var txtEditEventStartDate: TextView
    private lateinit var txtEditEventEndTime: TextView
    private lateinit var txtEditEventEndDate: TextView
    private lateinit var txtEditEventParticipant: AutoCompleteTextView
    private lateinit var recyclerViewParticipant: RecyclerView
    private lateinit var selectedParticipantAdapter: AdapterParticipant
    private var listParticipant = ArrayList<UserModel>()
    private val _participants: ArrayList<UserModel> = ArrayList<UserModel>()
    private val _participants2: ArrayList<UserModel> = ArrayList<UserModel>()
    private var participants: ArrayList<String> = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editevent)
        btnEditEventBack = findViewById(R.id.btnEditEventBack)
        txtEditEventStartTime = findViewById(R.id.txtEditEventStartTime)
        txtEditEventStartDate = findViewById(R.id.txtEditEventStartDate)
        txtEditEventEndTime = findViewById(R.id.txtEditEventEndTime)
        txtEditEventEndDate = findViewById(R.id.txtEditEventEndDate)
        txtEditEventParticipant = findViewById(R.id.txtEditEventParticipant)
        recyclerViewParticipant = findViewById(R.id.recyclerViewParticipant)
        btnEditEventBack.setOnClickListener {
            finish()
        }
        val calendar = Calendar.getInstance()

        val startDatePicker = object: DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                txtEditEventStartDate.text = dateToString(dayOfMonth, month, year)
            }
        }
        val endDatePicker = object: DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                txtEditEventEndDate.text = dateToString(dayOfMonth, month, year)
            }
        }
        txtEditEventStartDate.setOnClickListener {
            val startDatePickerDialog = DatePickerDialog(this, startDatePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            startDatePickerDialog.show()
        }
        txtEditEventEndDate.setOnClickListener {
            val endDatePickerDialog = DatePickerDialog(this, endDatePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            endDatePickerDialog.show()
        }

        val startTimePicker = object: TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, month: Int, dayOfMonth: Int) {
                txtEditEventStartTime.text = timeToString(dayOfMonth, month)
            }
        }
        val endTimePicker = object: TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, month: Int, dayOfMonth: Int) {
                txtEditEventEndTime.text = timeToString(dayOfMonth, month)
            }
        }
        txtEditEventStartTime.setOnClickListener {
            val startTimePickerDialog = TimePickerDialog(this, startTimePicker, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            startTimePickerDialog.show()
        }
        txtEditEventEndTime.setOnClickListener {
            val endTimePickerDialog = TimePickerDialog(this, endTimePicker, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            endTimePickerDialog.show()
        }

        txtEditEventStartTime.text = timeToString(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        txtEditEventStartDate.text = dateToString(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

        var calendarNext = Calendar.getInstance()
        calendarNext.add(Calendar.HOUR, 1)
        txtEditEventEndTime.text = timeToString(calendarNext.get(Calendar.HOUR_OF_DAY), calendarNext.get(Calendar.MINUTE))
        txtEditEventEndDate.text = dateToString(calendarNext.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

        selectedParticipantAdapter = AdapterParticipant { view, participant -> deleteParticipant(view, participant) }
        recyclerViewParticipant.adapter = selectedParticipantAdapter
        val participantAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, participants)
        txtEditEventParticipant.threshold = 2
        txtEditEventParticipant.setAdapter(participantAdapter)
        txtEditEventParticipant.setOnItemClickListener {parent, view, position, id ->
            Log.d("Debug:", "select item")
            Log.d("Debug:", _participants2.count().toString())
            listParticipant.add(_participants2[position])
            selectedParticipantAdapter.submitList(listParticipant.toMutableList())
            txtEditEventParticipant.setText("")
            txtEditEventParticipant.clearFocus()
        }
        txtEditEventParticipant.doBeforeTextChanged { text, start, count, after ->
            _participants2.clear()
            for (item in _participants) _participants2.add(item)
        }
        txtEditEventParticipant.doAfterTextChanged {
            Log.d("Debug:", "doAfterTextChanged")
            if (it.toString().length >= txtEditEventParticipant.threshold) {
                var selectedParticipant = ArrayList<Int>()
                for (item in listParticipant) {
                    if (selectedParticipant.contains(item.id)) continue
                    selectedParticipant.add(item.id)
                }
                var searchParticipants = UserService.SearchWithout(
                    it.toString(),
                    selectedParticipant
                ) as ArrayList<UserModel>
                _participants.clear()
                for (item in searchParticipants) _participants.add(item)
                participantAdapter.clear()
                for (user in _participants) {
                    participantAdapter.add(user.username + " - " + user.lastname + " " + user.firstname)
                }

                participantAdapter.notifyDataSetChanged()
            }
            Log.d("Debug:", _participants.count().toString())
        }
    }
    fun deleteParticipant(view: View?, participant: UserModel) {
        var newList = listParticipant.filter { !(it.id == participant.id) }
        listParticipant.clear()
        for (item in newList) listParticipant.add(item)
        selectedParticipantAdapter.submitList(listParticipant.toMutableList())
    }
    fun timeToString(hourOfDay: Int, minute: Int): String {
        var time = "";
        if (hourOfDay < 10) time += "0";
        time += hourOfDay.toString()
        time += "h"
        if (minute < 10) time += "0"
        time += minute.toString()
        return time
    }
    fun dateToString(dayOfMonth: Int, month: Int, year: Int): String {
        var dateText = "";
        val date = Date(year, month, dayOfMonth)
        val cal = Calendar.getInstance()
        cal.setTime(date)
        var dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == 1) dateText = "Chủ nhật, "
        else dateText = "Thứ " + dayOfWeek.toString() + ", "
        if (dayOfMonth < 10) dateText += "0";
        dateText += dayOfMonth.toString()
        dateText += "/"
        if (month < 9) dateText += "0"
        dateText += (month + 1).toString()
        dateText += "/"
        dateText += year.toString()
        return dateText
    }
}