package com.example.aula

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class Clue(
    val title: String,
    val question: String,
    val answer: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TreasureHuntApp()
            }
        }
    }
}

@Composable
fun TreasureHuntApp() {
    val navController = rememberNavController()

    val clues = listOf(
        Clue(
            title = "Pista 1",
            question = "Tenho teclas, mas não abro portas. O que sou eu?",
            answer = "teclado"
        ),
        Clue(
            title = "Pista 2",
            question = "Sou cheio de páginas, mas não sou um caderno. O que sou eu?",
            answer = "livro"
        ),
        Clue(
            title = "Pista 3",
            question = "Quanto é 5 + 3?",
            answer = "8"
        )
    )

    var startTime by rememberSaveable { mutableLongStateOf(0L) }
    var elapsedSeconds by rememberSaveable { mutableLongStateOf(0L) }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onStart = {
                    startTime = System.currentTimeMillis()
                    elapsedSeconds = 0L
                    navController.navigate("clue/0")
                }
            )
        }

        composable(
            route = "clue/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val currentClue = clues[index]

            ClueScreen(
                clue = currentClue,
                clueNumber = index + 1,
                totalClues = clues.size,
                onBack = {
                    navController.popBackStack()
                },
                onNext = {
                    if (index < clues.lastIndex) {
                        navController.navigate("clue/${index + 1}")
                    } else {
                        elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                        navController.navigate("treasure")
                    }
                }
            )
        }

        composable("treasure") {
            TreasureScreen(
                elapsedSeconds = elapsedSeconds,
                onRestart = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onStart: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caça ao Tesouro") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bem-vindo à caça ao tesouro!",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Resolva as pistas para encontrar o tesouro escondido.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onStart) {
                Text("Iniciar caça ao tesouro")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClueScreen(
    clue: Clue,
    clueNumber: Int,
    totalClues: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var answer by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf("") }
    var canAdvance by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${clue.title} ($clueNumber/$totalClues)") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = clue.question,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = {
                    answer = it
                    feedback = ""
                    canAdvance = false
                },
                label = { Text("Digite sua resposta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val userAnswer = normalizeText(answer)
                    val correctAnswer = normalizeText(clue.answer)

                    if (userAnswer == correctAnswer) {
                        feedback = "Resposta correta!"
                        canAdvance = true
                    } else {
                        feedback = "Resposta incorreta. Tente novamente."
                        canAdvance = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Validar resposta")
            }

            if (feedback.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBack) {
                    Text("Voltar")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onNext,
                    enabled = canAdvance
                ) {
                    Text(
                        if (clueNumber == totalClues) "Encontrar Tesouro"
                        else "Próxima Pista"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasureScreen(
    elapsedSeconds: Long,
    onRestart: () -> Unit
) {
    // Tratamento do botão físico de voltar:
    // em vez de sair da tela final, volta para a Home.
    BackHandler {
        onRestart()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tesouro Encontrado") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Parabéns! Você encontrou o tesouro!",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tempo total: ${formatTime(elapsedSeconds)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRestart) {
                Text("Voltar à tela inicial")
            }
        }
    }
}

fun normalizeText(text: String): String {
    return text
        .trim()
        .lowercase(Locale.getDefault())
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}