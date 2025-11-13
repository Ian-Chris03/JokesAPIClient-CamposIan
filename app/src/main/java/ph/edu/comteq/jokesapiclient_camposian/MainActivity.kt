package ph.edu.comteq.jokesapiclient_camposian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.edu.comteq.jokesapiclient_camposian.ui.theme.JokesAPIClientCamposIanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JokesAPIClientCamposIanTheme {
                val jokesViewModel: JokesViewModel = viewModel()
                JokesApp(viewModel = jokesViewModel)
            }
        }
    }
}

@Composable
fun JokesApp(viewModel: JokesViewModel) {
    val screenState by viewModel.screenState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onAddJokeClicked() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Joke")
            }
        }
    ) { innerPadding ->
        JokesScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )

        if (screenState.showAddDialog) {
            AddJokeDialog(
                onDismiss = { viewModel.onDialogDismiss() },
                onConfirm = { viewModel.addJoke() },
                dialogState = screenState.dialogState,
                onSetupChanged = { viewModel.onDialogSetupChanged(it) },
                onPunchlineChanged = { viewModel.onDialogPunchlineChanged(it) }
            )
        }

        screenState.jokeToEdit?.let { joke ->
            EditJokeDialog(
                joke = joke,
                onDismiss = { viewModel.onDialogDismiss() },
                onConfirm = { viewModel.updateJoke() },
                dialogState = screenState.dialogState,
                onSetupChanged = { viewModel.onDialogSetupChanged(it) },
                onPunchlineChanged = { viewModel.onDialogPunchlineChanged(it) }
            )
        }

        screenState.jokeToDelete?.let { joke ->
            AlertDialog(
                onDismissRequest = { viewModel.onDialogDismiss() },
                title = { Text("Delete Joke") },
                text = { Text("Are you sure you want to delete this joke?") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteJoke() }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDialogDismiss() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun JokesScreen(viewModel: JokesViewModel, modifier: Modifier = Modifier) {
    val screenState by viewModel.screenState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.get_Jokes()
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (val state = screenState.jokesUiState) {
            is JokesUIState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No Jokes To Show")
                }
            }
            is JokesUIState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is JokesUIState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for the FAB
                ) {
                    items(state.jokes, key = { it.id ?: it.hashCode() }) { joke ->
                        JokeItem(
                            joke = joke,
                            viewModel = viewModel
                        )
                    }
                }
            }
            is JokesUIState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(modifier = Modifier.padding(16.dp), text = state.message)
                }
            }
        }
    }
}

@Composable
fun JokeItem(joke: joke, viewModel: JokesViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = joke.setup)
                Text(text = joke.punchline)
            }
            IconButton(onClick = { viewModel.onDeleteJokeClicked(joke) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Joke",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = { viewModel.onEditJokeClicked(joke) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Joke"
                )
            }
        }
    }
}

@Composable
fun AddJokeDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dialogState: DialogState,
    onSetupChanged: (String) -> Unit,
    onPunchlineChanged: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Joke") },
        text = {
            Column {
                OutlinedTextField(
                    value = dialogState.setup,
                    onValueChange = onSetupChanged,
                    label = { Text("Setup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = dialogState.punchline,
                    onValueChange = onPunchlineChanged,
                    label = { Text("Punchline") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dialogState.setup.isNotBlank() && dialogState.punchline.isNotBlank()) {
                        onConfirm()
                    }
                },
                enabled = dialogState.setup.isNotBlank() && dialogState.punchline.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditJokeDialog(
    joke: joke,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dialogState: DialogState,
    onSetupChanged: (String) -> Unit,
    onPunchlineChanged: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Joke") },
        text = {
            Column {
                OutlinedTextField(
                    value = dialogState.setup,
                    onValueChange = onSetupChanged,
                    label = { Text("Setup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = dialogState.punchline,
                    onValueChange = onPunchlineChanged,
                    label = { Text("Punchline") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dialogState.setup.isNotBlank() && dialogState.punchline.isNotBlank()) {
                        onConfirm()
                    }
                },
                enabled = dialogState.setup.isNotBlank() && dialogState.punchline.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
