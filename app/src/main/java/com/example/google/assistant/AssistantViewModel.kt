package com.example.google.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.google.data.AssiatntDao
import com.example.google.data.Assistant
import kotlinx.coroutines.*

class AssistantViewModel (
        private val database : AssiatntDao, application: Application
):AndroidViewModel(application)
{
    private var viewModeJob= Job()

    override fun onCleared() {
        super.onCleared()
        viewModeJob.cancel()
    }
    private val  uiScope = CoroutineScope(Dispatchers.Main + viewModeJob)
     private var currentMessage = MutableLiveData<Assistant?>()
    val messages = database.getAllMessages()
    init {
        initializeCurrentMessage()
    }
  //each message and command for each msg answers of the question  
    private fun initializeCurrentMessage() {
        uiScope.launch {
            currentMessage.value = getCurrentMessageFromDtaabase()
        }
    }

    private suspend fun getCurrentMessageFromDtaabase(): Assistant? {
             return  withContext(Dispatchers.IO) {
                 var message = database.getCurrentMessage()
                 //Intializing default message
                 if (message?.assistant_message == "DEFAULT_MESSAGE" || message?.human_message == "DEFAULT_MESSAGE") {
                     message= null
                 }
                 message
             }
    }
  //couroutunes are weel suited familiar task events iterators means for multitasking , rx java , we can do asynchrionous task
    fun sendMessageToDatabase(assistantMessage : String, humanMessage: String){
        uiScope.launch {
            val newAssistant = Assistant()
            newAssistant.assistant_message= assistantMessage
            newAssistant.human_message=humanMessage
            insert(newAssistant)
            currentMessage.value=getCurrentMessageFromDtaabase()
        }
    }
    //CRUDE OPERATION READ , DELETE , UPDATE , INSERT
    //save msgs to database and handle multitasking couroutines here
    private suspend fun insert(message : Assistant)
    {
     withContext(Dispatchers.IO){

         database.insert(message)
     }
    }
    private suspend fun update(message : Assistant)
    {
        withContext(Dispatchers.IO){

            database.update(message)
        }
    }
   fun onClear(){
       uiScope.launch {
       clear()
           currentMessage.value=null
       }
   }
    private suspend fun clear()
    {
        withContext(Dispatchers.IO){

            database.clear()
        }
    }
}
