package com.example.vodafoneempinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DataEntryViewModel @Inject constructor(
    private val excelRepository: ExcelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataEntryUiState())
    val uiState: StateFlow<DataEntryUiState> = _uiState.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    init {
        loadEmployees()
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            _employees.value = excelRepository.getEmployees()
        }
    }

    fun updateSelectedEmployee(employee: Employee) {
        _uiState.value = _uiState.value.copy(
            dataEntry = _uiState.value.dataEntry.copy(name = employee.displayName)
        )
    }

    fun updateDate(date: String) {
        _uiState.value = _uiState.value.copy(
            dataEntry = _uiState.value.dataEntry.copy(date = date)
        )
    }

    fun updatePortin(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(portin = value)
            )
        }
    }

    fun updateP2P(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(p2p = value)
            )
        }
    }

    fun updateNewFixedAdsl(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(newFixedAdsl = value)
            )
        }
    }

    fun updateNewFixedVdsl(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(newFixedVdsl = value)
            )
        }
    }

    fun updateNewFixedFtth(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(newFixedFtth = value)
            )
        }
    }

    fun updateFwa(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(fwa = value)
            )
        }
    }

    fun updateWirelessHome(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(wirelessHome = value)
            )
        }
    }

    fun updateOnenet(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(onenet = value)
            )
        }
    }

    fun updateFixedMigrationFtth(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(fixedMigrationFtth = value)
            )
        }
    }

    fun updateEc2post(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(ec2post = value)
            )
        }
    }

    fun updatePost2post(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(post2post = value)
            )
        }
    }

    fun updateTvNew(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(tvNew = value)
            )
        }
    }

    fun updateTvMigration(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(tvMigration = value)
            )
        }
    }

    fun updateVdslMigration(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(vdslMigration = value)
            )
        }
    }

    fun updatePhoneRenewal(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(phoneRenewal = value)
            )
        }
    }

    fun updateFixedRenewal(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(fixedRenewal = value)
            )
        }
    }

    fun updateTotalEtopup(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(totalEtopup = value)
            )
        }
    }

    fun updateTotalPayments(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(totalPayments = value)
            )
        }
    }

    fun updateMobileDeals(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(mobileDeals = value)
            )
        }
    }

    fun updateFixedDeals(value: String) {
        if (isValidNumber(value)) {
            _uiState.value = _uiState.value.copy(
                dataEntry = _uiState.value.dataEntry.copy(fixedDeals = value)
            )
        }
    }

    private fun isValidNumber(value: String): Boolean {
        return value.isEmpty() || value.all { it.isDigit() || it == '.' }
    }

    fun submitData() {
        if (!isFormValid()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all required fields"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val result = excelRepository.updateEmployeeData(
                    employeeName = _uiState.value.dataEntry.name,
                    dataEntry = _uiState.value.dataEntry
                )

                result.fold(
                    onSuccess = { updateResult ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSubmissionSuccessful = updateResult.success,
                            errorMessage = if (updateResult.success) null else updateResult.message
                        )
                        if (updateResult.success) {
                            clearForm()
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "An error occurred"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    private fun isFormValid(): Boolean {
        val data = _uiState.value.dataEntry
        return data.name.isNotEmpty() && data.date.isNotEmpty()
    }

    fun clearForm() {
        _uiState.value = _uiState.value.copy(
            dataEntry = EmployeeDataEntry()
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessFlag() {
        _uiState.value = _uiState.value.copy(isSubmissionSuccessful = false)
    }

    fun getCurrentMonthDays(): List<String> {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d/MMMM/yyyy")

        val currentMonthDays = (1..currentDate.lengthOfMonth()).map { day ->
            currentDate.withDayOfMonth(day).format(formatter)
        }

        val previousMonth = currentDate.minusMonths(1)
        val previousMonthDays = (1..previousMonth.lengthOfMonth()).map { day ->
            previousMonth.withDayOfMonth(day).format(formatter)
        }

        return previousMonthDays + currentMonthDays
    }
}

data class DataEntryUiState(
    val dataEntry: EmployeeDataEntry = EmployeeDataEntry(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSubmissionSuccessful: Boolean = false
)