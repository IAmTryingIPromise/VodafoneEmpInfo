package com.example.vodafoneempinfo


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
            // ExportScreen(navController)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vodafone Employee App",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Authentication Status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = if (authState.isAuthenticated) "Authenticated" else "Not Authenticated",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (authState.isAuthenticated)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Authentication Button
                if (authState.isAuthenticated) {
                    Button(
                        onClick = { viewModel.signOut() },
                        enabled = !authState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign Out")
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.signIn()
                            //navController.navigate("information")
                             },
                        enabled = !authState.isLoading
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign In")
                        }
                    }
                }
            }
        }

        // Navigation buttons
        if (authState.isAuthenticated) {
            TextButton(
                onClick = {
                    navController.navigate("import")
                },
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Green,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Import Data"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    navController.navigate("export")
                },
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Export Data"
                )
            }
        }
/*
        // File Input Section
        if (authState.isAuthenticated) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter File Name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = appState.fileName,
                        onValueChange = { viewModel.updateFileName(it) },
                        label = { Text("File name (without .txt)") },
                        placeholder = { Text("example-file") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !appState.isLoading
                    )

                    Button(
                        onClick = { viewModel.getFileContent() },
                        enabled = !appState.isLoading && appState.fileName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (appState.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text("Loading...")
                            }
                        } else {
                            Text("Get File Content")
                        }
                    }
                }
            }
        }
*/
        // Error Display
        if (authState.error != null || appState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text("Dismiss")
                        }
                    }

                    Text(
                        text = authState.error ?: appState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
/*
        // File Content Display
        if (appState.fileContent.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "File Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = appState.fileContent,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
*/
        // Instructions Card
        if (!authState.isAuthenticated) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text =  "1. Sign in with your Microsoft account\n" +
                                "2. Click Import button to import information\n" +
                                "3. Click Export button to display information",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
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

    var showEmployeeDropdown by remember { mutableStateOf(false) }
    var showDateDropdown by remember { mutableStateOf(false) }

    val availableDates = remember { viewModel.getCurrentMonthDays() }

    // Show success/error messages
    LaunchedEffect(uiState.isSubmissionSuccessful) {
        if (uiState.isSubmissionSuccessful) {
            Toast.makeText(
                context,
                "Input Successful",
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearSuccessFlag()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            Toast.makeText(
                context,
                uiState.errorMessage,
                Toast.LENGTH_LONG
            ).show()
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        IconButton(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Employee Data Entry",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Employee Name Dropdown
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

            // Numeric Input Fields
            DataEntryTextField(
                value = uiState.dataEntry.portin,
                onValueChange = viewModel::updatePortin,
                label = "Portin"
            )

            DataEntryTextField(
                value = uiState.dataEntry.p2p,
                onValueChange = viewModel::updateP2P,
                label = "P2P"
            )

            DataEntryTextField(
                value = uiState.dataEntry.newFixedAdsl,
                onValueChange = viewModel::updateNewFixedAdsl,
                label = "NewFixed ADSL"
            )

            DataEntryTextField(
                value = uiState.dataEntry.newFixedVdsl,
                onValueChange = viewModel::updateNewFixedVdsl,
                label = "NewFixed VDSL"
            )

            DataEntryTextField(
                value = uiState.dataEntry.newFixedFtth,
                onValueChange = viewModel::updateNewFixedFtth,
                label = "NewFixed FTTH"
            )

            DataEntryTextField(
                value = uiState.dataEntry.fwa,
                onValueChange = viewModel::updateFwa,
                label = "FWA"
            )

            DataEntryTextField(
                value = uiState.dataEntry.wirelessHome,
                onValueChange = viewModel::updateWirelessHome,
                label = "Wireless Home"
            )

            DataEntryTextField(
                value = uiState.dataEntry.onenet,
                onValueChange = viewModel::updateOnenet,
                label = "ONENET"
            )

            DataEntryTextField(
                value = uiState.dataEntry.fixedMigrationFtth,
                onValueChange = viewModel::updateFixedMigrationFtth,
                label = "FIXED MIGRATION FTTH"
            )

            DataEntryTextField(
                value = uiState.dataEntry.ec2post,
                onValueChange = viewModel::updateEc2post,
                label = "EC2POST"
            )

            DataEntryTextField(
                value = uiState.dataEntry.post2post,
                onValueChange = viewModel::updatePost2post,
                label = "POST2POST"
            )

            DataEntryTextField(
                value = uiState.dataEntry.tvNew,
                onValueChange = viewModel::updateTvNew,
                label = "TV NEW"
            )

            DataEntryTextField(
                value = uiState.dataEntry.tvMigration,
                onValueChange = viewModel::updateTvMigration,
                label = "TV MIGRATION"
            )

            DataEntryTextField(
                value = uiState.dataEntry.vdslMigration,
                onValueChange = viewModel::updateVdslMigration,
                label = "VDSL MIGRATION"
            )

            DataEntryTextField(
                value = uiState.dataEntry.phoneRenewal,
                onValueChange = viewModel::updatePhoneRenewal,
                label = "PHONE RENEWAL"
            )

            DataEntryTextField(
                value = uiState.dataEntry.fixedRenewal,
                onValueChange = viewModel::updateFixedRenewal,
                label = "FIXED RENEWAL"
            )

            DataEntryTextField(
                value = uiState.dataEntry.totalEtopup,
                onValueChange = viewModel::updateTotalEtopup,
                label = "TOTAL ETOPUP"
            )

            DataEntryTextField(
                value = uiState.dataEntry.totalPayments,
                onValueChange = viewModel::updateTotalPayments,
                label = "TOTAL PAYMENTS"
            )

            DataEntryTextField(
                value = uiState.dataEntry.mobileDeals,
                onValueChange = viewModel::updateMobileDeals,
                label = "MOBILE DEALS"
            )

            DataEntryTextField(
                value = uiState.dataEntry.fixedDeals,
                onValueChange = viewModel::updateFixedDeals,
                label = "FIXED DEALS"
            )

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

            // Clear Form Button
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}