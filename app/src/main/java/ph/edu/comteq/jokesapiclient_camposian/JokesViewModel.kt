package ph.edu.comteq.jokesapiclient_camposian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// Sealed interface is often preferred over a sealed class for UI states
sealed interface JokesUiState {
    data object Idle : JokesUiState
    data object Loading : JokesUiState
    data class Success(val jokes: List<Joke>) : JokesUiState
    data class Error(val message: String) : JokesUiState
}

class JokesViewModel : ViewModel() {
    // The error on the next line will now be gone
    private val api = RetrofitInstance.jokeAPI

    // Private mutable state
    private val _uiState = MutableStateFlow<JokesUiState>(JokesUiState.Idle)
    // Public immutable state exposed to the UI
    val uiState: StateFlow<JokesUiState> = _uiState.asStateFlow()

    fun getJokes() {
        // viewModelScope is now available because this class is a ViewModel
        viewModelScope.launch {
            _uiState.value = JokesUiState.Loading
            try {
                val jokes = api.getJokes()
                _uiState.value = JokesUiState.Success(jokes)
            } catch (e: Exception) {
                _uiState.value = JokesUiState.Error(
                    message = e.message ?: "Unknown Error"
                )
            }
        }
    }
}

fun addJoke(setup: String, punchline:String){
    viewModelScope.launch{
        try{
            val newJoke = Joke(id = null, setup = setup, punchLine = punchline)
            api.addJoke(newJoke)
            getJokes()
        }catch(e: Exception){
            _uiState.value = JokesUiState.Error(
                e.message ?: "Unknown Error"
            )
        }
    }
}