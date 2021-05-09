package com.example.google.assistant

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.google.data.AssiatntDao
import java.lang.IllegalArgumentException

class AssiatntViewModelFactory (
    private val dataSource : AssiatntDao ,
            private val application : Application ) :ViewModelProvider.Factory

{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)){
            return AssistantViewModel(dataSource,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
