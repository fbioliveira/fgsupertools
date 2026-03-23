package com.fglabs.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fglabs.app.audio.AudioPlayer
import com.fglabs.app.audio.AudioRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val recorder = AudioRecorder(application)
    private val player = AudioPlayer(application)
    private var currentFile: File? = null
    private var amplitudeJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _recentRecordings = MutableStateFlow<List<File>>(emptyList())
    val recentRecordings: StateFlow<List<File>> = _recentRecordings

    private val _recordingCount = MutableStateFlow(0)
    val recordingCount: StateFlow<Int> = _recordingCount

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude

    init {
        loadRecordings()
    }

    private fun loadRecordings() {
        val filesDir = getApplication<Application>().filesDir
        val allFiles = filesDir.listFiles { file -> file.extension == "m4a" } ?: emptyArray()
        
        _recordingCount.value = allFiles.size
        
        _recentRecordings.value = allFiles
            .sortedByDescending { it.lastModified() }
            .take(3)
    }

    fun startRecording() {
        val file = File(getApplication<Application>().filesDir, "recording_${System.currentTimeMillis()}.m4a")
        currentFile = file
        recorder.start(file)
        _isRecording.value = true
        startAmplitudeTracking()
    }

    fun stopRecording() {
        recorder.stop()
        _isRecording.value = false
        _amplitude.value = 0f
        amplitudeJob?.cancel()
        loadRecordings()
    }

    private fun startAmplitudeTracking() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (isActive) {
                val maxAmp = recorder.getMaxAmplitude().toFloat()
                // Normalize amplitude between 0 and 1 (Max is usually 32767)
                _amplitude.value = (maxAmp / 32767f).coerceIn(0f, 1f)
                delay(100)
            }
        }
    }

    fun playRecording(file: File) {
        player.playFile(file)
    }

    fun deleteRecording(file: File) {
        if (file.exists()) {
            file.delete()
            loadRecordings()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.stop()
        amplitudeJob?.cancel()
    }
}
