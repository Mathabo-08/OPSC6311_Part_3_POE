package com.example.zenith

import com.google.firebase.firestore.DocumentId

data class BudgetItem(
    @DocumentId // This annotation tells Firestore to map the document's ID to this field
    val id: String = "",
    val category: String = "",
    val allocatedAmount: Double = 0.0,
    val spentAmount: Double = 0.0
)
