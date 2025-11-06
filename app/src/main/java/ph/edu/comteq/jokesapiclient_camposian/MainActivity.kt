package ph.edu.comteq.jokesapiclient_camposian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // 👈 FIX: Import for LazyColumn items
import androidx.compose.material.icons.Icons // 👈 FIX: Import for Material Icons
import androidx.compose.material.icons.filled.Add // 👈 FIX: Import for Add Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState // 👈 FIX: Import for collectAsState
import androidx.compose.runtime.getValue // 👈 FIX: Imports for state delegation
import androidx.compose.runtime.mutableStateOf // 👈 FIX: Correct mutableStateOf import
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                // 👇 FIX: Use mutableStateOf for a single boolean value
                var showDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showDialog = true } // 👈 FIX: Set showDialog to true
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add, // 👈 FIX: Use imageVector
                                contentDescription = "Add New Jokes"
                            )
                        }
                    }
                ) { innerPadding ->
                    JokesScreen(
                        viewModel = jokesViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )

                    // This composable will be displayed only when showDialog is true
                    if (showDialog) {
                        AddJokeDialog(
                            onDismiss = { showDialog = false },
                            onConfirm = { setup, punchline ->
                                // jokesViewModel.addJoke(setup, punchline) // This function doesn't exist yet
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JokesScreen(viewModel: JokesViewModel, modifier: Modifier = Modifier) {
    // 👇 FIX: The 'by' keyword delegates state access, import was missing
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getJokes()
    }

    Column(modifier.fillMaxSize()) {
        // 👇 FIX: Changed JokesUiState.idle to JokesUiState.Idle to match definition
        when (val state = uiState) {
            is JokesUiState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(), // 👈 FIX: Use fillMaxSize to center properly
                    contentAlignment = Alignment.Center
                ) {
                    Text("Click the '+' button to fetch jokes!")
                }
            }

            is JokesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is JokesUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) { // 👈 FIX: Apply modifier
                    // 👇 FIX: Define the 'joke' item to be used inside the block
                    items(state.jokes) { joke ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp) // Added vertical padding
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(joke.setup, style = MaterialTheme.typography.bodyLarge)
                                Text(joke.punchline, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            is JokesUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddJokeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    // 👇 FIX: Use mutableStateOf for simple String values
    var setup by remember { mutableStateOf("") }
    var punchline by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss, // 👈 FIX: Correct parameter name
        title = { Text(text = "Add New Joke") },
        text = {
            Column {
                OutlinedTextField(
                    value = setup,
                    onValueChange = { setup = it },
                    label = { Text("Setup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    // 👇 FIX: This field was incorrectly using the 'setup' state variable
                    value = punchline,
                    onValueChange = { punchline = it },
                    label = { Text("Punchline") }, // Corrected spelling
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 👇 FIX: isNotBlank() is a function call
                    if (setup.isNotBlank() && punchline.isNotBlank()) {
                        onConfirm(setup, punchline)
                    }
                },
                // 👇 FIX: isNotBlank() is a function call
                enabled = setup.isNotBlank() && punchline.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            // 👇 FIX: Corrected onClick lambda and parameter name
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


