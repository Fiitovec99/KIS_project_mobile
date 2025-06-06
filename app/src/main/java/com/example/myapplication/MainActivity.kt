package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.collections.chunked

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SchoolApp()
            }
        }
    }
}

class SchoolViewModel : ViewModel() {
    private val _schedule = MutableLiveData<List<DaySchedule>>()
    val schedule: LiveData<List<DaySchedule>> get() = _schedule

    private val _subjectsItems = MutableLiveData<Map<String, List<String>>>()
    val subjectsItems: LiveData<Map<String, List<String>>> get() = _subjectsItems

    init {
        _schedule.value = listOf(
            DaySchedule("Понедельник", listOf("Математика", "Русский язык", "География", "Физ. культура")),
            DaySchedule("Вторник", listOf("Математика", "Русский язык", "География", "Физ. культура")),
            DaySchedule("Среда", listOf("Математика", "Русский язык", "География", "Физ. культура")),
            DaySchedule("Четверг", listOf("Математика", "Русский язык", "География", "Физ. культура")),
            DaySchedule("Пятница", listOf("Математика", "Русский язык", "География", "Физ. культура")),
            DaySchedule("Суббота", listOf("Математика", "Русский язык", "География", "Физ. культура", "Литература"))
        )

        _subjectsItems.value = mapOf(
            "Математика" to listOf("Тетрадь", "Учебник", "Линейка"),
            "Русский язык" to listOf("Тетрадь", "Учебник"),
            "География" to listOf("Тетрадь", "Атлас", "Контурные карты"),
            "Физ. культура" to listOf("Спортивная форма"),
            "Литература" to listOf("Тетрадь", "Книга")
        )
    }

    fun updateDaySchedule(dayName: String, newSubjects: List<String>) {
        val currentSchedule = _schedule.value?.toMutableList() ?: return
        val dayIndex = currentSchedule.indexOfFirst { it.name == dayName }
        if (dayIndex != -1) {
            currentSchedule[dayIndex] = DaySchedule(dayName, newSubjects)
            _schedule.value = currentSchedule
        }
    }

    fun updateSubjectItems(subjectName: String, items: List<String>) {
        val currentItems = _subjectsItems.value?.toMutableMap() ?: return
        currentItems[subjectName] = items
        _subjectsItems.value = currentItems
    }
}

@Composable
fun SchoolApp(viewModel: SchoolViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.Schedule.route
    ) {
        composable(Routes.Schedule.route) {
            ScheduleScreen(navController, viewModel)
        }

        composable(
            route = "${Routes.LessonsEditor.route}/{day}",
            arguments = listOf(navArgument("day") { type = NavType.StringType })
        ) { backStackEntry ->
            val day = backStackEntry.arguments?.getString("day") ?: "Понедельник"
            LessonsEditorScreen(navController, day, viewModel)
        }

        composable(
            route = "${Routes.SubjectEditor.route}/{subject}",
            arguments = listOf(navArgument("subject") { type = NavType.StringType })
        ) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: ""
            SubjectEditorScreen(navController, subject, viewModel)
        }

        composable(Routes.SchoolChecklist.route) {
            SchoolChecklistScreen(navController, viewModel)
        }
    }
}


sealed class Routes(val route: String) {
    object Schedule : Routes("schedule")
    object LessonsEditor : Routes("lessons_editor") {
        fun createRoute(day: String) = "$route/$day"
    }
    object SubjectEditor : Routes("subject_editor") {
        fun createRoute(subject: String) = "$route/$subject"
    }
    object SchoolChecklist : Routes("school_checklist")
}


data class DaySchedule(val name: String, val subjects: List<String>)


@Composable
fun ScheduleScreen(navController: NavController, viewModel: SchoolViewModel) {
    val schedule by viewModel.schedule.observeAsState(emptyList())

    val rows = schedule.chunked(2)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Расписание",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(rows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { day ->
                            DayCard(day = day, navController = navController)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item{
                    Button(
                        onClick = { navController.navigate(Routes.SchoolChecklist.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Взять с собой")
                    }
                }
            }
        }
    }
}

@Composable
fun DayCard(day: DaySchedule, navController: NavController) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable {
                navController.navigate(Routes.LessonsEditor.createRoute(day.name))
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                day.subjects.forEachIndexed { index, subject ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${index + 1}. $subject",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonsEditorScreen(
    navController: NavController,
    initialDay: String,
    viewModel: SchoolViewModel
) {
    val schedule by viewModel.schedule.observeAsState(emptyList())
    val daySchedule = schedule.find { it.name == initialDay } ?: DaySchedule(initialDay, emptyList())

    var lessonCount by remember { mutableStateOf(daySchedule.subjects.size) }
    var lessons by remember {
        mutableStateOf(
            daySchedule.subjects.map { TextFieldValue(it) }
        )
    }

    val focusManager = LocalFocusManager.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = initialDay, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = lessonCount.toString(),
                onValueChange = {
                    val count = it.toIntOrNull() ?: 0
                    lessonCount = count
                    lessons = List(count) { i ->
                        lessons.getOrNull(i) ?: TextFieldValue("")
                    }
                },
                label = { Text("Количество уроков") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))
            lessons.forEachIndexed { index, textFieldValue ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = {
                            val newList = lessons.toMutableList()
                            newList[index] = it
                            lessons = newList
                        },
                        label = { Text("Урок ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (index < lessons.size - 1) {
                                    focusManager.moveFocus(FocusDirection.Down)
                                } else {
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    )
                    Text(
                        text = "✏️",
                        modifier = Modifier
                            .clickable {
                                navController.navigate(Routes.SubjectEditor.createRoute(textFieldValue.text))
                            }
                            .padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    val newSubjects = lessons.map { it.text }
                    viewModel.updateDaySchedule(initialDay, newSubjects)
                    focusManager.clearFocus()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
fun SubjectEditorScreen(
    navController: NavController,
    initialSubject: String,
    viewModel: SchoolViewModel
) {
    val subjectItems by viewModel.subjectsItems.observeAsState(emptyMap())
    val initialItems = subjectItems[initialSubject] ?: emptyList()

    var subjectName by remember { mutableStateOf(TextFieldValue(initialSubject)) }
    var item1 by remember { mutableStateOf(TextFieldValue(initialItems.getOrElse(0) { "" })) }
    var item2 by remember { mutableStateOf(TextFieldValue(initialItems.getOrElse(1) { "" })) }
    var item3 by remember { mutableStateOf(TextFieldValue(initialItems.getOrElse(2) { "" })) }
    var item4 by remember { mutableStateOf(TextFieldValue(initialItems.getOrElse(3) { "" })) }

    val focusManager = LocalFocusManager.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subjectName.text,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Взять с собой", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = item1,
                onValueChange = { item1 = it },
                label = { Text("Предмет 1") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = item2,
                onValueChange = { item2 = it },
                label = { Text("Предмет 2") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = item3,
                onValueChange = { item3 = it },
                label = { Text("Предмет 3") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = item4,
                onValueChange = { item4 = it },
                label = { Text("Предмет 4") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val items = listOf(item1.text, item2.text, item3.text, item4.text)
                        .filter { it.isNotBlank() }
                    viewModel.updateSubjectItems(subjectName.text, items)
                    focusManager.clearFocus()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
fun SchoolChecklistScreen(navController: NavController, viewModel: SchoolViewModel) {
    val daysOfWeek = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
    var selectedDay by remember { mutableStateOf(daysOfWeek[0]) }
    var expanded by remember { mutableStateOf(false) }

    val schedule by viewModel.schedule.observeAsState(emptyList())
    val subjectItems by viewModel.subjectsItems.observeAsState(emptyMap())

    val daySchedule = schedule.find { it.name == selectedDay }
    val checklistItems = daySchedule?.subjects?.flatMap { subject ->
        subjectItems[subject]?.map { item -> item to subject } ?: emptyList()
    } ?: emptyList()

    Column(Modifier.padding(16.dp)) {
        Text("Взять с собой", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RectangleShape
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDay)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day) },
                        onClick = {
                            selectedDay = day
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        checklistItems.forEach { (item, subject) ->
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("• $item", fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Text(subject, fontSize = 16.sp, color = Color.Gray)
                }
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Назад")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SchoolAppPreview() {
    MyApplicationTheme {
        SchoolApp()
    }
}