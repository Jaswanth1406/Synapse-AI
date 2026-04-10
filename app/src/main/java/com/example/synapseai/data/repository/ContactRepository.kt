package com.example.synapseai.data.repository

import com.example.synapseai.data.api.RetrofitClient
import com.example.synapseai.data.model.*

class ContactRepository {

    private val api = RetrofitClient.api

    // In-memory contact store for locally added / CSV-imported contacts
    private val localContacts = mutableListOf<Contact>()

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

    /**
     * Push local contacts to the backend CRM as leads.
     */
    suspend fun pushLeadsBatch(contacts: List<Contact>): Result<BatchLeadResponse> {
        return try {
            val leads = contacts.map { contact ->
                val parts = contact.name.split(" ", limit = 2)
                LeadEntry(
                    firstName = parts.getOrElse(0) { contact.name },
                    lastName = parts.getOrElse(1) { "" },
                    phoneNumber = contact.phoneNumber
                )
            }
            val request = BatchLeadRequest(
                leads = leads,
                userId = UserIdConstants.MASTER_USER_ID
            )
            val response = api.createLeads(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Lead push failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch leads from EspoCRM via GET /api/leads and convert to local Contact model.
     */
    suspend fun fetchCrmLeads(): Result<List<Contact>> {
        return try {
            val response = api.getCrmLeads()
            if (response.isSuccessful && response.body() != null) {
                val contacts = response.body()!!.map { crm ->
                    val status = when (crm.status?.lowercase()) {
                        "interested", "converted" -> LeadStatus.INTERESTED
                        "not interested", "dead", "lost" -> LeadStatus.NOT_INTERESTED
                        "callback", "follow-up" -> LeadStatus.CALLBACK
                        "no answer" -> LeadStatus.NO_ANSWER
                        else -> LeadStatus.PENDING
                    }
                    Contact(
                        id = crm.id,
                        name = crm.name,
                        phoneNumber = crm.phoneNumber ?: "",
                        company = "",
                        leadStatus = status,
                        lastCallDate = crm.updatedAt
                    )
                }
                Result.success(contacts)
            } else {
                Result.failure(Exception("CRM leads fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
