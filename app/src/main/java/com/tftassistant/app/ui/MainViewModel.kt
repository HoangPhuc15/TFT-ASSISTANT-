package com.tftassistant.app.ui

import androidx.lifecycle.ViewModel
import com.tftassistant.app.domain.TftAssistantController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val controller: TftAssistantController
) : ViewModel() {

    val uiState = controller.uiState

    fun refreshMeta() {
        controller.refreshMeta()
    }
}
