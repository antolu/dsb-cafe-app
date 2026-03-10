package com.lua.dsbcafe.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.lua.dsbcafe.data.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PersonRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("persons")

    fun observePersons(): Flow<List<Person>> =
        collection.snapshots().map { it.toObjects(Person::class.java) }

    suspend fun getOrNull(badgeId: String): Person? {
        val doc = collection.document(badgeId).get().await()
        return if (doc.exists()) doc.toObject(Person::class.java) else null
    }

    suspend fun save(person: Person) {
        collection.document(person.badgeId).set(person).await()
    }

    suspend fun resetAllCounts(persons: List<Person>) {
        val batch = db.batch()
        persons.forEach { person ->
            batch.set(collection.document(person.badgeId), person.copy(coffeeCount = 0))
        }
        batch.commit().await()
    }

    suspend fun deletePerson(name: String) {
        val docs = collection.whereEqualTo("name", name).get().await()
        val batch = db.batch()
        docs.forEach { batch.delete(collection.document(it.id)) }
        batch.commit().await()
    }
}
