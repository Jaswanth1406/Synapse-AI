package com.example.synapseai.data.repository

import com.example.synapseai.data.api.RetrofitClient
import com.example.synapseai.data.model.*

class ContactRepository {

    private val espoCrmApi = RetrofitClient.espoCrmApi

    // In-memory contact store for locally added / CSV-imported contacts
    private val localContacts = mutableListOf<Contact>()

    suspend fun fetchLeadsFromCrm(): Result<List<EspoLead>> {
        return try {
            val response = espoCrmApi.getLeads()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.list)
            } else {
                Result.failure(Exception("CRM fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCallLogs(): Result<List<EspoCallLog>> {
        return try {
            val response = espoCrmApi.getCallLogs()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.list)
            } else {
                Result.failure(Exception("Call logs fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addLocalContact(contact: Contact) {
        localContacts.add(contact)
    }

    fun addLocalContacts(contacts: List<Contact>) {
        localContacts.addAll(contacts)
    }

    fun getLocalContacts(): List<Contact> = localContacts.toList()

    fun removeLocalContact(id: String) {
        localContacts.removeAll { it.id == id }
    }

    fun parseCsvContacts(csvContent: String): List<Contact> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size < 2) return emptyList()

        val header = lines[0].split(",").map { it.trim().lowercase() }
        val nameIdx = header.indexOfFirst { it.contains("name") }
        val phoneIdx = header.indexOfFirst { it.contains("phone") || it.contains("number") }
        val companyIdx = header.indexOfFirst { it.contains("company") || it.contains("organization") }

        if (nameIdx == -1 || phoneIdx == -1) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val cols = line.split(",").map { it.trim() }
            if (cols.size > maxOf(nameIdx, phoneIdx)) {
                Contact(
                    id = java.util.UUID.randomUUID().toString(),
                    name = cols.getOrElse(nameIdx) { "" },
                    phoneNumber = cols.getOrElse(phoneIdx) { "" },
                    company = if (companyIdx >= 0) cols.getOrElse(companyIdx) { "" } else ""
                )
            } else null
        }
    }
}
