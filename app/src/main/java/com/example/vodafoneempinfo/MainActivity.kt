package com.example.vodafoneempinfo


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the activity-scoped AuthRepository in the AuthManager
        authManager.setAuthRepository(authRepository)

        enableEdgeToEdge()
        setContent {
            VodafoneEmpInfoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MyApp()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the AuthRepository reference to prevent memory leaks
        authManager.clearAuthRepository()
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("import") {
            ImportScreen(navController)
        }
        composable("export") {
            ExportScreen(navController)
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavHostController
) {
    val viewModel: MainViewModel = hiltViewModel()

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val appState by viewModel.appState.collectAsStateWithLifecycle()

    val currentUser by viewModel.authManagerInstance.currentUser.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Header Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Icon/Logo placeholder
                    Card(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(40.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF667eea)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "V",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Vodafone Employee",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Data Management App",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A5568),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Authentication Status
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (authState.isAuthenticated)
                                Color(0xFF10B981).copy(alpha = 0.1f)
                            else
                                Color(0xFFEF4444).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (authState.isAuthenticated) Color(0xFF10B981) else Color(0xFFEF4444),
                                            CircleShape
                                        )
                                )

                                Text(
                                    text = if (authState.isAuthenticated) "Connected" else "Not Connected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (authState.isAuthenticated) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }

                            // Show current user name when authenticated
                            if (authState.isAuthenticated && currentUser != null) {
                                Text(
                                    text = "Signed in as: $currentUser",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Authentication Button
                    Button(
                        onClick = {
                            if (authState.isAuthenticated) viewModel.signOut() else viewModel.signIn()
                        },
                        enabled = !authState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (authState.isAuthenticated)
                                Color(0xFFEF4444)
                            else
                                Color(0xFF667eea)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (authState.isAuthenticated) "Sign Out" else "Sign In with Microsoft",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Navigation Section
            if (authState.isAuthenticated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Choose Action",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Button(
                            onClick = { navController.navigate("import") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    text = "Import Data",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { navController.navigate("export") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    text = "Export Data",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Instructions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Getting Started",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val instructions = listOf(
                            "Sign in with your Microsoft account",
                            "Import data to update your records",
                            "Export data to view your information"
                        )

                        instructions.forEachIndexed { index, instruction ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFF667eea), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Text(
                                    text = instruction,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4A5568)
                                )
                            }

                            if (index < instructions.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            // Error Display
            if (authState.error != null || appState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )

                            TextButton(
                                onClick = { viewModel.clearError() }
                            ) {
                                Text(
                                    "Dismiss",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = authState.error ?: appState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VodafoneEmpInfoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = dynamicColorScheme(),
        content = content
    )
}

@Composable
private fun dynamicColorScheme(): ColorScheme {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isSystemInDarkTheme()) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        if (isSystemInDarkTheme()) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
    }
}
/*
fun parseStringToMapSafe(input: String): Map<String, Int> {
    val result = mutableMapOf<String, Int>()

    input.split("\n").forEach { line ->
        if (line.isNotBlank()) {
            val parts = line.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val valueStr = parts[1].trim()

                // Try to convert to Int, skip if invalid
                valueStr.toIntOrNull()?.let { value ->
                    result[key] = value
                }
            }
        }
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScree(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "My Information App",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E3A59),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Description
        Text(
            text = "View detailed information about various topics",
            fontSize = 16.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Navigate to Details Button
        Button(
            onClick = {
                navController.navigate("details")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "View Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScree(navController: NavHostController) {
    // Sample data - in real app this would be passed as parameter
    val sampleData = mapOf(
        "Name" to "John Doe",
        "Age" to "28",
        "Email" to "john.doe@example.com",
        "Phone" to "+1 (555) 123-4567",
        "Address" to "123 Main St, City, State",
        "Occupation" to "Software Developer",
        "Company" to "Tech Solutions Inc.",
        "Experience" to "5 years",
        "Skills" to "Kotlin, Android, Compose",
        "Status" to "Active"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ){
            // Top Bar with Back Button
            CenterAlignedTopAppBar(
                title = {
                        Text(
                            text = "Monthly Goals",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4F46E5)
                )
            )
        }

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleData.toList()) { (title, value) ->
                InformationRow(title = title, value = value)
            }
        }
    }
}

@Composable
fun InformationRow(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: DataEntryViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()

//    var showEmployeeDropdown by remember { mutableStateOf(false) }
    var showDateDropdown by remember { mutableStateOf(false) }

    val availableDates = remember { viewModel.getCurrentMonthDays() }

    val viewModelMain: MainViewModel = hiltViewModel()
    val currentUser by viewModelMain.authManagerInstance.currentUser.collectAsStateWithLifecycle()
    val isAdmin = currentUser?.lowercase()?.contains("savvas kotzamanidis") == true

    // Show success/error messages
    LaunchedEffect(uiState.isSubmissionSuccessful) {
        if (uiState.isSubmissionSuccessful) {
            Toast.makeText(context, "Input Successful", Toast.LENGTH_LONG).show()
            viewModel.clearSuccessFlag()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Import Data",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigate("login") }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF667eea)
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Employee Name Dropdown (now restricted to current user)
            if (isAdmin) {
                var showEmployeeDropdown by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = showEmployeeDropdown,
                    onExpandedChange = { showEmployeeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.dataEntry.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Select Employee (Admin)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEmployeeDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = PrimaryNotEditable,
                                enabled = true
                            )
                    )

                    ExposedDropdownMenu(
                        expanded = showEmployeeDropdown,
                        onDismissRequest = { showEmployeeDropdown = false }
                    ) {
                        employees.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text(employee.displayName) },
                                onClick = {
                                    viewModel.updateSelectedEmployee(employee)
                                    showEmployeeDropdown = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Static field for regular users
                OutlinedTextField(
                    value = uiState.dataEntry.name,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Your Name (Auto-selected)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
            /*
                        OutlinedTextField(
                            value = uiState.dataEntry.name,
                            onValueChange = { }, // No-op since it's read-only
                            readOnly = true,
                            enabled = false, // This makes it grayed out
                            label = { Text("Your Name (Auto-selected)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                       ExposedDropdownMenuBox(
                            expanded = showEmployeeDropdown,
                            onExpandedChange = { showEmployeeDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.dataEntry.name,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Name") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEmployeeDropdown)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        type = PrimaryNotEditable,
                                        enabled = employees.size > 1 // Only enable if more than one option
                                    )
                            )

                            if (employees.size > 1) {
                                ExposedDropdownMenu(
                                    expanded = showEmployeeDropdown,
                                    onDismissRequest = { showEmployeeDropdown = false }
                                ) {
                                    employees.forEach { employee ->
                                        DropdownMenuItem(
                                            text = { Text(employee.displayName) },
                                            onClick = {
                                                viewModel.updateSelectedEmployee(employee)
                                                showEmployeeDropdown = false
                                            }
                                        )
                                    }
                                }
                            } else if (employees.size == 1) {
                                // Auto-select the single employee
                                LaunchedEffect(employees) {
                                    if (uiState.dataEntry.name.isEmpty()) {
                                        viewModel.updateSelectedEmployee(employees.first())
                                    }
                                }
                            }
                        }
            */
            // Rest of the form remains the same...
            // Date Dropdown
            ExposedDropdownMenuBox(
                expanded = showDateDropdown,
                onExpandedChange = { showDateDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.dataEntry.date,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDateDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = PrimaryNotEditable,
                            enabled = true
                        )
                )

                ExposedDropdownMenu(
                    expanded = showDateDropdown,
                    onDismissRequest = { showDateDropdown = false }
                ) {
                    availableDates.forEach { date ->
                        DropdownMenuItem(
                            text = { Text(date) },
                            onClick = {
                                viewModel.updateDate(date)
                                showDateDropdown = false
                            }
                        )
                    }
                }
            }

            // All the numeric input fields remain the same...
            DataEntryTextField(value = uiState.dataEntry.portin, onValueChange = viewModel::updatePortin, label = "Portin")
            DataEntryTextField(value = uiState.dataEntry.p2p, onValueChange = viewModel::updateP2P, label = "P2P")
            DataEntryTextField(value = uiState.dataEntry.newFixedAdsl, onValueChange = viewModel::updateNewFixedAdsl, label = "NewFixed ADSL")
            DataEntryTextField(value = uiState.dataEntry.newFixedVdsl, onValueChange = viewModel::updateNewFixedVdsl, label = "NewFixed VDSL")
            DataEntryTextField(value = uiState.dataEntry.newFixedFtth, onValueChange = viewModel::updateNewFixedFtth, label = "NewFixed FTTH")
            DataEntryTextField(value = uiState.dataEntry.fwa, onValueChange = viewModel::updateFwa, label = "FWA")
            DataEntryTextField(value = uiState.dataEntry.wirelessHome, onValueChange = viewModel::updateWirelessHome, label = "Wireless Home")
            DataEntryTextField(value = uiState.dataEntry.onenet, onValueChange = viewModel::updateOnenet, label = "ONENET")
            DataEntryTextField(value = uiState.dataEntry.fixedMigrationFtth, onValueChange = viewModel::updateFixedMigrationFtth, label = "FIXED MIGRATION FTTH")
            DataEntryTextField(value = uiState.dataEntry.ec2post, onValueChange = viewModel::updateEc2post, label = "EC2POST")
            DataEntryTextField(value = uiState.dataEntry.post2post, onValueChange = viewModel::updatePost2post, label = "POST2POST")
            DataEntryTextField(value = uiState.dataEntry.tvNew, onValueChange = viewModel::updateTvNew, label = "TV NEW")
            DataEntryTextField(value = uiState.dataEntry.tvMigration, onValueChange = viewModel::updateTvMigration, label = "TV MIGRATION")
            DataEntryTextField(value = uiState.dataEntry.vdslMigration, onValueChange = viewModel::updateVdslMigration, label = "VDSL MIGRATION")
            DataEntryTextField(value = uiState.dataEntry.phoneRenewal, onValueChange = viewModel::updatePhoneRenewal, label = "PHONE RENEWAL")
            DataEntryTextField(value = uiState.dataEntry.fixedRenewal, onValueChange = viewModel::updateFixedRenewal, label = "FIXED RENEWAL")
            DataEntryTextField(value = uiState.dataEntry.totalEtopup, onValueChange = viewModel::updateTotalEtopup, label = "TOTAL ETOPUP")
            DataEntryTextField(value = uiState.dataEntry.totalPayments, onValueChange = viewModel::updateTotalPayments, label = "TOTAL PAYMENTS")
            DataEntryTextField(value = uiState.dataEntry.mobileDeals, onValueChange = viewModel::updateMobileDeals, label = "MOBILE DEALS")
            DataEntryTextField(value = uiState.dataEntry.fixedDeals, onValueChange = viewModel::updateFixedDeals, label = "FIXED DEALS")

            // Error Message
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Button(
                onClick = { viewModel.submitData() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Data")
                }
            }

            OutlinedButton(
                onClick = { viewModel.clearForm() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Form")
            }
        }
    }
}

@Composable
private fun DataEntryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (label == "FIXED DEALS") ImeAction.Done else ImeAction.Next),
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: DataEntryViewModel = hiltViewModel()

    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()

    var showEmployeeDropdown by remember { mutableStateOf(false) }
    var showDateDropdown by remember { mutableStateOf(false) }

    val availableDates = remember { viewModel.getCurrentMonthDays() }

    // Load all employees for export (different from import)
    LaunchedEffect(Unit) {
        viewModel.loadAllEmployeesForExport()
    }

    // Show error messages
    LaunchedEffect(exportState.errorMessage) {
        if (exportState.errorMessage != null) {
            Toast.makeText(context, exportState.errorMessage, Toast.LENGTH_LONG).show()
            kotlinx.coroutines.delay(5000)
            viewModel.clearExportError()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Export Data",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigate("login") }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF3B82F6)
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Employee Name Dropdown (shows all employees for export)
            ExposedDropdownMenuBox(
                expanded = showEmployeeDropdown,
                onExpandedChange = { showEmployeeDropdown = it }
            ) {
                OutlinedTextField(
                    value = exportState.selectedEmployeeName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select Employee") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEmployeeDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = PrimaryNotEditable,
                            enabled = true
                        )
                )

                ExposedDropdownMenu(
                    expanded = showEmployeeDropdown,
                    onDismissRequest = { showEmployeeDropdown = false }
                ) {
                    employees.forEach { employee ->
                        DropdownMenuItem(
                            text = { Text(employee.displayName) },
                            onClick = {
                                viewModel.updateExportSelectedEmployee(employee)
                                showEmployeeDropdown = false
                            }
                        )
                    }
                }
            }

            // Rest of the export screen remains the same...
            // Date Dropdown, Export Button, Display Data, etc.

            // Date Dropdown
            ExposedDropdownMenuBox(
                expanded = showDateDropdown,
                onExpandedChange = { showDateDropdown = it }
            ) {
                OutlinedTextField(
                    value = exportState.selectedDate,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select Date") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDateDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = PrimaryNotEditable,
                            enabled = true
                        )
                )

                ExposedDropdownMenu(
                    expanded = showDateDropdown,
                    onDismissRequest = { showDateDropdown = false }
                ) {
                    availableDates.forEach { date ->
                        DropdownMenuItem(
                            text = { Text(date) },
                            onClick = {
                                viewModel.updateExportDate(date)
                                showDateDropdown = false
                            }
                        )
                    }
                }
            }

            // Export Button
            Button(
                onClick = { viewModel.exportData() },
                enabled = !exportState.isLoading &&
                        exportState.selectedEmployeeName.isNotEmpty() &&
                        exportState.selectedDate.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (exportState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Export Data")
                }
            }

            // Clear Button
            OutlinedButton(
                onClick = { viewModel.clearExportData() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Selection")
            }

            // Display exported data
            if (exportState.hasData && exportState.exportedData != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Employee Data",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Employee Info
                        DataDisplayRow("Employee", exportState.exportedData!!.name)
                        DataDisplayRow("Date", exportState.exportedData!!.date)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Data fields
                        DataDisplayRow("Portin", exportState.exportedData!!.portin)
                        DataDisplayRow("P2P", exportState.exportedData!!.p2p)
                        DataDisplayRow("NewFixed ADSL", exportState.exportedData!!.newFixedAdsl)
                        DataDisplayRow("NewFixed VDSL", exportState.exportedData!!.newFixedVdsl)
                        DataDisplayRow("NewFixed FTTH", exportState.exportedData!!.newFixedFtth)
                        DataDisplayRow("FWA", exportState.exportedData!!.fwa)
                        DataDisplayRow("Wireless Home", exportState.exportedData!!.wirelessHome)
                        DataDisplayRow("ONENET", exportState.exportedData!!.onenet)
                        DataDisplayRow("Fixed Migration FTTH", exportState.exportedData!!.fixedMigrationFtth)
                        DataDisplayRow("EC2POST", exportState.exportedData!!.ec2post)
                        DataDisplayRow("POST2POST", exportState.exportedData!!.post2post)
                        DataDisplayRow("TV New", exportState.exportedData!!.tvNew)
                        DataDisplayRow("TV Migration", exportState.exportedData!!.tvMigration)
                        DataDisplayRow("VDSL Migration", exportState.exportedData!!.vdslMigration)
                        DataDisplayRow("Phone Renewal", exportState.exportedData!!.phoneRenewal)
                        DataDisplayRow("Fixed Renewal", exportState.exportedData!!.fixedRenewal)
                        DataDisplayRow("Total E-topup", exportState.exportedData!!.totalEtopup)
                        DataDisplayRow("Total Payments", exportState.exportedData!!.totalPayments)
                        DataDisplayRow("Mobile Deals", exportState.exportedData!!.mobileDeals)
                        DataDisplayRow("Fixed Deals", exportState.exportedData!!.fixedDeals)
                    }
                }
            }

            // Error Message
            exportState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DataDisplayRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.4f)
        )

        Text(
            text = value.ifEmpty { "0" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End,
            color = if (value.isEmpty() || value == "0") {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}