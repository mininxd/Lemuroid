package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCheatsScreen(
    viewModel: GameCheatsViewModel,
    onCheatsUpdated: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.game_menu_cheats_add))
            }
        }
    ) { paddingValues ->
        if (viewModel.cheats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.empty_view_default))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(viewModel.cheats) { cheat ->
                    CheatItem(
                        cheat = cheat,
                        onToggle = {
                            viewModel.toggleCheat(cheat)
                            onCheatsUpdated()
                        },
                        onDelete = {
                            viewModel.removeCheat(cheat)
                            onCheatsUpdated()
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCheatDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, code ->
                viewModel.addCheat(name, code)
                onCheatsUpdated()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CheatItem(cheat: Cheat, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = cheat.enabled, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(text = cheat.name, style = MaterialTheme.typography.titleMedium)
            Text(text = cheat.code, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCheatDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.game_menu_cheats_add)) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.game_menu_cheats_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(stringResource(R.string.game_menu_cheats_code)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        onAdd(name, code)
                    }
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
