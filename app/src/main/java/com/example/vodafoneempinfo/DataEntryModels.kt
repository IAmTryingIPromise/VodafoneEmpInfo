package com.example.vodafoneempinfo

// Data models for the Excel data entry feature

data class EmployeeDataEntry(
    val name: String = "",
    val date: String = "",
    val portin: String = "",
    val p2p: String = "",
    val newFixedAdsl: String = "",
    val newFixedVdsl: String = "",
    val newFixedFtth: String = "",
    val fwa: String = "",
    val wirelessHome: String = "",
    val onenet: String = "",
    val fixedMigrationFtth: String = "",
    val ec2post: String = "",
    val post2post: String = "",
    val tvNew: String = "",
    val tvMigration: String = "",
    val vdslMigration: String = "",
    val phoneRenewal: String = "",
    val fixedRenewal: String = "",
    val totalEtopup: String = "",
    val totalPayments: String = "",
    val mobileDeals: String = "",
    val fixedDeals: String = ""
)

data class Employee(
    val displayName: String,
    val tableName: String,
    val userName: String
)

data class ExcelWorksheet(
    val name: String,
    val id: String
)

data class ExcelUpdateResult(
    val success: Boolean,
    val message: String
)

data class UserInfo(
    val displayName: String,
    val userPrincipalName: String
)