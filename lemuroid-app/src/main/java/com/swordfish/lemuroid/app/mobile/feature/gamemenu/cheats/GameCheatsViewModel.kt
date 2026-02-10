package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class GameCheatsViewModel(
    application: Application,
    private val gameMenuRequest: GameMenuActivity.GameMenuRequest
) : AndroidViewModel(application) {

    val cheats = mutableStateListOf<Cheat>()

    private val cheatsDir = File(application.filesDir, "cheats")
    private val cheatsFile: File by lazy {
        File(cheatsDir, "${gameMenuRequest.game.id}.json")
    }

    init {
        loadCheats()
    }

    private fun loadCheats() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (cheatsFile.exists()) {
                    try {
                        val json = cheatsFile.readText()
                        val loadedCheats = Json.decodeFromString(ListSerializer(Cheat.serializer()), json)
                        withContext(Dispatchers.Main) {
                            cheats.clear()
                            cheats.addAll(loadedCheats)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun addCheat(name: String, code: String) {
        cheats.add(Cheat(name, code, true))
        saveCheats()
    }

    fun toggleCheat(cheat: Cheat) {
        val index = cheats.indexOf(cheat)
        if (index != -1) {
            // Re-create the object to trigger recomposition if needed, but since it's a data class in a mutable list...
            // Actually, mutableStateListOf triggers on add/remove/set.
            // If I modify a property of an item, it might not trigger.
            // So I should replace the item.
            val newCheat = cheat.copy(enabled = !cheat.enabled)
            cheats[index] = newCheat
            saveCheats()
        }
    }

    fun removeCheat(cheat: Cheat) {
        cheats.remove(cheat)
        saveCheats()
    }

    private fun saveCheats() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!cheatsDir.exists()) {
                        cheatsDir.mkdirs()
                    }
                    val json = Json.encodeToString(ListSerializer(Cheat.serializer()), cheats.toList())
                    cheatsFile.writeText(json)
                    saveCheatsAsCht()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveCheatsAsCht() {
        val gameFileName = gameMenuRequest.game.fileName.substringBeforeLast(".")
        val chtFile = File(cheatsDir, "$gameFileName.cht")
        val content = StringBuilder()
        content.append("cheats = ${cheats.size}\n\n")
        cheats.forEachIndexed { index, cheat ->
            content.append("cheat${index}_desc = \"${cheat.name}\"\n")
            content.append("cheat${index}_code = \"${cheat.code}\"\n")
            content.append("cheat${index}_enable = ${cheat.enabled}\n\n")
        }
        chtFile.writeText(content.toString())
    }

    class Factory(
        private val application: Application,
        private val gameMenuRequest: GameMenuActivity.GameMenuRequest
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameCheatsViewModel(application, gameMenuRequest) as T
        }
    }
}
