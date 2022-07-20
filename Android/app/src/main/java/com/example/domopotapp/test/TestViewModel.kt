package com.example.domopotapp.test

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TestViewModel: ViewModel() {
    val text: MutableLiveData<String> by lazy {
        MutableLiveData<String>("Test")
    }
    val testData: MutableLiveData<MutableList<TestData>> by lazy {
        MutableLiveData<MutableList<TestData>>(mutableListOf())
    }
}