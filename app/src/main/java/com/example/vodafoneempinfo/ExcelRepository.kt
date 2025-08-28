package com.example.vodafoneempinfo

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelRepository @Inject constructor(
    private val client: OkHttpClient,
    private val getAccessToken: suspend () -> String?
) {
    private val fileId = "01HUQKBFZFOS2XCFQW3RD2M67K72AM3WIL"
    private val siteId =
        "cbsmcom.sharepoint.com,5a9063f8-42da-4ccc-ab8d-e3a137426a18,ac4fadb3-a47f-42c4-9c47-e8418eb134f9"
    private val driveId = "b!-GOQWtpCzEyrjeOhN0JqGLOtT6x_pMRCnEfoQY6xNPkaj56tt0ekTJW3UffQKc2t"

    // Predefined employee list with their table names
    private val employees = listOf(
        Employee("Κατερίνα Γ", "katerina"),
        Employee("Ειρήνη Μ", "eirinim"),
        Employee("Ειρήνη Σ", "eirinis"),
        Employee("Αναστασία Π", "anastasia"),
        Employee("Ευγενία Π", "eugenia"),
        Employee("Άντα Κ", "anta"),
    )

    suspend fun getEmployees(): List<Employee> = employees

    suspend fun updateEmployeeData(
        employeeName: String,
        dataEntry: EmployeeDataEntry
    ): Result<ExcelUpdateResult> {
        return withContext(Dispatchers.IO) {
            try {
                val employee = employees.find { it.displayName == employeeName }
                    ?: return@withContext Result.failure(IllegalArgumentException("Employee not found"))

                val tableName = employee.tableName

                // Find the row index by date
                val rowIndex = findRowByDate(dataEntry.date, tableName)
                    ?: return@withContext Result.failure(IllegalArgumentException("Date not found in table"))

                // Update the row
                updateTableRow(tableName, rowIndex, dataEntry)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun findRowByDate(date: String, tableName: String): Int? {
        return try {
            val accessToken = getAccessToken()
                ?: return null

            val url =
                "https://graph.microsoft.com/v1.0/sites/$siteId/drives/$driveId/items/$fileId/workbook/tables('$tableName')/rows"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val rows = jsonResponse.getJSONArray("value")

                // Convert app format (4/July/2025) to Excel format (4/7/2025)
                val targetDate = convertDateFormat(date)
                // Convert 4/7/2025 to serial number 1900
                val date1900 = dateToExcel1900Serial(targetDate)

                // Search through table rows to find matching date
                for (i in 0 until rows.length()) {
                    val row = rows.getJSONObject(i)
                    val values = row.getJSONArray("values")

                    if (values.length() > 0) {
                        val rowValues = values.getJSONArray(0)
                        if (rowValues.length() > 0) {
                            val cellValue = rowValues.opt(0)
                            val dateValue: Long? = when (cellValue) {
                                is Number -> cellValue.toLong()
                                is String -> {
                                    val trimmed = cellValue.trim()
                                    if (trimmed.isNotEmpty()) {
                                        try {
                                            trimmed.toLong()
                                        } catch (e: NumberFormatException) {
                                            null
                                        }
                                    } else null
                                }

                                else -> null
                            }

                            if (dateValue == date1900) {
                                return row.getInt("index")
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun updateTableRow(
        tableName: String,
        rowIndex: Int,
        dataEntry: EmployeeDataEntry
    ): Result<ExcelUpdateResult> {
        return try {
            val accessToken = getAccessToken()
                ?: return Result.failure(IllegalArgumentException("Access token not available"))

            // Prepare data array - convert date to Excel format for storage
            val rowData = JSONArray().apply {
                put(convertDateFormat(dataEntry.date)) // Convert to Excel format (4/7/2025)
                put(dataEntry.portin)
                put(dataEntry.p2p)
                put(dataEntry.newFixedAdsl)
                put(dataEntry.newFixedVdsl)
                put(dataEntry.newFixedFtth)
                put(dataEntry.fwa)
                put(dataEntry.wirelessHome)
                put(dataEntry.onenet)
                put(dataEntry.fixedMigrationFtth)
                put(dataEntry.ec2post)
                put(dataEntry.post2post)
                put(dataEntry.tvNew)
                put(dataEntry.tvMigration)
                put(dataEntry.vdslMigration)
                put(dataEntry.phoneRenewal)
                put(dataEntry.fixedRenewal)
                put(dataEntry.totalEtopup)
                put(dataEntry.totalPayments)
                put(dataEntry.mobileDeals)
                put(dataEntry.fixedDeals)
            }

            val requestBody = JSONObject().apply {
                put("values", JSONArray().apply { put(rowData) })
            }

            val url =
                "https://graph.microsoft.com/v1.0/sites/$siteId/drives/$driveId/items/$fileId/workbook/tables('$tableName')/rows/itemAt(index=$rowIndex)"

            val request = Request.Builder()
                .url(url)
                .patch(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(ExcelUpdateResult(true, "Data updated successfully"))
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Result.success(
                    ExcelUpdateResult(
                        false,
                        "Failed to update data: ${response.code} - $errorBody"
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun convertDateFormat(date: String): String {
        return try {
            val parts = date.split("/")
            if (parts.size == 3) {
                val day = parts[0].toInt().toString() // Remove leading zeros
                val month = getMonthNumber(parts[1])
                val year = parts[2]
                "$day/$month/$year"
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    private fun getMonthNumber(monthName: String): String {
        return when (monthName.lowercase()) {
            "january", "jan" -> "1"
            "february", "feb" -> "2"
            "march", "mar" -> "3"
            "april", "apr" -> "4"
            "may" -> "5"
            "june", "jun" -> "6"
            "july", "jul" -> "7"
            "august", "aug" -> "8"
            "september", "sep" -> "9"
            "october", "oct" -> "10"
            "november", "nov" -> "11"
            "december", "dec" -> "12"
            else -> {
                // If it's already a number, convert to int and back to remove leading zeros
                try {
                    monthName.toInt().toString()
                } catch (e: NumberFormatException) {
                    monthName
                }
            }
        }
    }

    fun dateToExcel1900Serial(dateString: String): Long {
        // Parse the date string - handles both single and double digit days/months
        val formatter = DateTimeFormatter.ofPattern("[d/M/yyyy][dd/MM/yyyy]")
        val inputDate = LocalDate.parse(dateString, formatter)

        // Excel 1900 epoch (January 1, 1900)
        val excel1900Epoch = LocalDate.of(1900, 1, 1)

        // Calculate days between dates
        val daysDifference = ChronoUnit.DAYS.between(excel1900Epoch, inputDate)

        // Excel 1900 system quirks:
        // 1. January 1, 1900 is day 1 (not 0)
        // 2. Excel incorrectly treats 1900 as a leap year, so we add 1 for dates after Feb 28, 1900
        val excelSerial = daysDifference + 1

        // Add 1 more day if the date is after February 28, 1900 due to Excel's leap year bug
        return if (inputDate.isAfter(LocalDate.of(1900, 2, 28))) {
            excelSerial + 1
        } else {
            excelSerial
        }
    }
}