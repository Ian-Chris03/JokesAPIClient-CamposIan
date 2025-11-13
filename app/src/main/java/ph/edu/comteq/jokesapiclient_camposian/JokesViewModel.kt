package ph.edu.comteq.jokesapiclient_camposian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception

sealed class JokesUIState {
    object Idle : JokesUIState()
    object Loading : JokesUIState()
    data class Success(val jokes: List<joke>) : JokesUIState()
    data class Error(val message: String) : JokesUIState()
}

data class DialogState(
    val setup: String = "",
    val punchline: String = "",
)

data class JokesScreenState(
    val jokesUiState: JokesUIState = JokesUIState.Idle,
    val showAddDialog: Boolean = false,
    val jokeToEdit: joke? = null,
    val jokeToDelete: joke? = null,
    val dialogState: DialogState = DialogState(),
)

class JokesViewModel : ViewModel() {
    private val api = retroFitInstance.jokeAPI

    private val _screenState = MutableStateFlow(JokesScreenState())
    val screenState: StateFlow<JokesScreenState> = _screenState.asStateFlow()

    fun onAddJokeClicked() {
        _screenState.update { it.copy(showAddDialog = true) }
    }

    fun onEditJokeClicked(joke: joke) {
        _screenState.update { it.copy(jokeToEdit = joke, dialogState = DialogState(setup = joke.setup, punchline = joke.punchline)) }
    }

    fun onDeleteJokeClicked(joke: joke) {
        _screenState.update { it.copy(jokeToDelete = joke) }
    }

    fun onDialogDismiss() {
        _screenState.update { it.copy(showAddDialog = false, jokeToEdit = null, jokeToDelete = null, dialogState = DialogState()) }
    }

    fun onDialogSetupChanged(setup: String) {
        _screenState.update { it.copy(dialogState = it.dialogState.copy(setup = setup)) }
    }

    fun onDialogPunchlineChanged(punchline: String) {
        _screenState.update { it.copy(dialogState = it.dialogState.copy(punchline = punchline)) }
    }

    fun get_Jokes() {
        viewModelScope.launch {
            _screenState.update { it.copy(jokesUiState = JokesUIState.Loading) }
            try {
                val jokes = api.getJokes()
                _screenState.update { it.copy(jokesUiState = JokesUIState.Success(jokes)) }
            } catch (e: Exception) {
                _screenState.update { it.copy(jokesUiState = JokesUIState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun addJoke() {
        viewModelScope.launch {
            try {
                val dialogState = _screenState.value.dialogState
                onDialogDismiss()
                val newJoke = joke(setup = dialogState.setup, punchline = dialogState.punchline)
                api.addJokes(newJoke)
                get_Jokes()
            } catch (e: Exception) {
                _screenState.update { it.copy(jokesUiState = JokesUIState.Error(e.message ?: "Unknown error while adding joke")) }
            }
        }
    }

    fun deleteJoke() {
        viewModelScope.launch {
            val jokeId = _screenState.value.jokeToDelete?.id
            if (jokeId == null) {
                onDialogDismiss()
                return@launch
            }
            try {
                onDialogDismiss()
                api.deleteJokes(jokeId)
                get_Jokes()
            } catch (e: Exception) {
                _screenState.update { it.copy(jokesUiState = JokesUIState.Error(e.message ?: "Unknown error while deleting joke")) }
            }
        }
    }

    fun updateJoke() {
        viewModelScope.launch {
            try {
                val jokeToEdit = _screenState.value.jokeToEdit
                val dialogState = _screenState.value.dialogState
                if (jokeToEdit == null) {
                    onDialogDismiss()
                    return@launch
                }
                onDialogDismiss()
                val updatedJoke = joke(id = jokeToEdit.id, setup = dialogState.setup, punchline = dialogState.punchline)
                api.updateJoke(jokeToEdit.id!!, updatedJoke)
                get_Jokes()
            } catch (e: Exception) {
                _screenState.update { it.copy(jokesUiState = JokesUIState.Error(e.message ?: "Unknown error while updating joke")) }
            }
        }
    }
}