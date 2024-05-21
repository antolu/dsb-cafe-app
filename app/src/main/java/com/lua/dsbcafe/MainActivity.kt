package com.lua.dsbcafe

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity(), NfcActivity.OnNfcTagReadListener {
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techList: Array<Array<String>>? = null

    private lateinit var db: FirebaseFirestore
    var persons = emptyList<Person>()
    private var totalCoffeeCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // get database
        db = FirebaseFirestore.getInstance()

        // Initialize NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        Log.d("NfcActivity", "NFC Adapter detected: $nfcAdapter")

        // Create a PendingIntent that will start this activity
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(
                this, this::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        )

        intentFiltersArray = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
        )

        // add isoDep tech to techList
        techList = arrayOf(arrayOf("android.nfc.tech.IsoDep"))

        enableEdgeToEdge()
        setContent {
            var persons by remember { mutableStateOf(emptyList<Person>()) }
            var totalCoffeeCount by remember { mutableIntStateOf(0) }

            // Fetch data from Firestore
            LaunchedEffect(Unit) { // Fetch once on activity launch
                db.collection("persons")
                    .get()
                    .addOnSuccessListener { result ->
                        persons = result.toObjects(Person::class.java)
                        totalCoffeeCount = persons.sumOf { it.coffeeCount }
                    }
                    .addOnFailureListener { exception ->
                        // log error
                        error("Error getting documents: $exception")
                    }
                db.collection("persons")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && !snapshot.isEmpty) {
                            persons = snapshot.toObjects(Person::class.java)
                            totalCoffeeCount = persons.sumOf { it.coffeeCount }
                        } else {
                            Log.d(TAG, "Current data: null")
                        }
                    }

            }

            AdminMenu()

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(196.dp))

//                Text(
//                    text = totalCoffeeCount.toString(),
//                    fontSize = 64.sp,
//                    modifier = Modifier.padding(bottom = 32.dp)
//                )

//                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Coffee Addiction Breakdown:",
                    fontSize = 28.sp,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // sort by coffee count
                    persons = persons.sortedByDescending { it.coffeeCount }
                    items(
                        count = persons.size,
                        key = { index -> persons[index].badgeId }
                    ) { index ->
                        PersonItem(person = persons[index])
                    }
                }
            }
        }
        updateCoffeeCountDisplay()
    }
    // ... UI elements, NFC Adapter, Data Storage

    override fun onNfcTagRead(badgeId: String) {
        // Show a popup
        Toast.makeText(this, "NFC Tag Read: $badgeId", Toast.LENGTH_SHORT).show()
        Log.d("MainActivity", "Badge ID: $badgeId")

        // Check if badgeId exists in Firestore
        val personsCollection = db.collection("persons")

        // Try to get the document with the badgeId
        personsCollection.document(badgeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // If the document exists, increment the coffee count
                    val person = document.toObject(Person::class.java)
                    person?.let {
                        it.coffeeCount++
                        personsCollection.document(badgeId).set(it)
                        updateCoffeeCountDisplay()
                        showDoubleDialog(badgeId, it)
                    }
                } else {
                    // If the document doesn't exist, prompt for name and add to Firestore
                    promptForName(this) { name ->
                        val newPerson = Person(name, 1, badgeId)
                        personsCollection.document(badgeId).set(newPerson)
                        updateCoffeeCountDisplay()
                        showDoubleDialog(badgeId, newPerson)
                    }
                }
            }
        updateCoffeeCountDisplay()
//        updatePersonCoffeeCount()
    }

    // UI updates
    private fun updateCoffeeCountDisplay() {
        // Update the displayed coffee count
        db.collection("persons")
            .get()
            .addOnSuccessListener { result ->
                val persons = result.toObjects(Person::class.java)
                totalCoffeeCount = persons.sumOf { it.coffeeCount }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    override fun onResume() {
        super.onResume()
        Log.d("NfcActivity", "onResume")

        // Enable foreground dispatch to receive NFC intents while your activity is in the foreground
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList)
    }

    override fun onPause() {
        super.onPause()
        Log.d("NfcActivity", "onPause")

        // Disable foreground dispatch when your activity is not in the foreground
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("NfcActivity", "New intent received: $intent")

        // log the action
        Log.d("NfcActivity", "Action: ${intent?.action}")

        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
            val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            val badgeId = tagId?.joinToString("") { "%02x".format(it) }

            onNfcTagRead(badgeId ?: "")
        }
    }

    private fun showDoubleDialog(badgeId: String, person: Person) {
        val dialog = AlertDialog.Builder(this)
            .setMessage("Make it a double?")
            .setPositiveButton("Make it a double!") { dialog, _ ->
                person.coffeeCount++
                db.collection("persons").document(badgeId).set(person)
                updateCoffeeCountDisplay()
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Dismiss the dialog after 10 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 10000)
    }

    // Admin menu handling
    @Composable
    private fun AdminMenu() {
        var isExpanded by remember { mutableStateOf(false) }
        var tapCount by remember { mutableStateOf(0) }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
//            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = totalCoffeeCount.toString(),
                fontSize = 64.sp,
                modifier = Modifier
                    .padding(bottom = 32.dp, top=64.dp)
                    .align(Alignment.TopCenter)
                    .clickable(indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        })
                    {
                        tapCount++
                        if (tapCount >= 5) {
                            isExpanded = !isExpanded
                            tapCount = 0 // reset the counter
                        }
                    }

            )
            if (isExpanded) {
                FloatingActionButton(
                    content = {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null
                        )
                    }, // Add this line
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .height(56.dp)
                        .width(56.dp)
                )
                SpeedDial(
                    fabState = isExpanded,
                    labels = listOf("Reset Counts", "Send Statistics Email", "Delete User"),
                    icons = listOf(
                        Icons.Default.Refresh,
                        Icons.Default.Email,
                        Icons.Default.Delete
                    ),
                    actions = listOf(
                        { resetCounts() },
                        { sendStatisticsEmail() },
                        { deleteUser() }),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp)
                )
            }

            // Reset tapCount after 10 seconds
            LaunchedEffect(tapCount) {
                if (tapCount > 0) {
                    delay(5000) // delay for 10 seconds
                    tapCount = 0
                }
            }
        }

    }

    @Composable
    fun SpeedDial(
        fabState: Boolean,
        labels: List<String>,
        icons: List<ImageVector>,
        actions: List<() -> Unit>,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (fabState) {
                labels.zip(icons).zip(actions).forEach { (labelIcon, action) ->
                    val (label, icon) = labelIcon
                    FloatingActionButton(
                        onClick = {
                            Log.d(TAG, "Clicked on $label")
                            action()
                        },
                        modifier = Modifier.padding(bottom = 8.dp).zIndex(1f)
                    ) {
                        Icon(icon, contentDescription = label)
                    }
                }
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }

    private fun resetCounts() {
        val personsCollection = db.collection("persons")

        Log.d(TAG, "Resetting coffee counts")

        persons.forEach { person ->
            person.coffeeCount = 0
            personsCollection.document(person.badgeId).set(person)
        }

        updateCoffeeCountDisplay()
    }

    private fun sendStatisticsEmail() {
        val emailBody = persons.joinToString("\n") { "${it.name}: ${it.coffeeCount}" }

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            val currentMonth = java.time.LocalDate.now().month
            putExtra(Intent.EXTRA_SUBJECT, "Coffee Count for $currentMonth")
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        startActivity(Intent.createChooser(emailIntent, "Send Email"))

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }

    private fun deleteUser() {
        // Prompt for the name of the user to delete
        promptForExistingName(this) { name ->
            val personsCollection = db.collection("persons")
            personsCollection.whereEqualTo("name", name).get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        personsCollection.document(document.id).delete()
                    }
                    Log.d(TAG, "Deleted document with name $name")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting document", e)
                }
        }
    }

    private fun promptForExistingName(context: Context, onNameReceived: (String) -> Unit) {
        val personsCollection = db.collection("persons")
        personsCollection.get().addOnSuccessListener { result ->
            val names = result.documents.map { document ->
                val person = document.toObject(Person::class.java)
                person?.name
            }.filterNotNull().toTypedArray()
            var selectedName: String? = null

            AlertDialog.Builder(context)
                .setTitle("Select a name")
                .setSingleChoiceItems(names, -1) { _, which ->
                    selectedName = names[which]
                }
                .setPositiveButton("OK") { dialog, _ ->
                    if (selectedName != null) {
                        onNameReceived(selectedName!!)
                    } else {
                        Toast.makeText(context, "Please select a name", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

}

fun promptForName(context: Context, onNameReceived: (String) -> Unit) {
    val editText = EditText(context).apply {
        hint = "Enter your name"
    }

    AlertDialog.Builder(context)
        .setTitle("Name")
        .setMessage("Please enter your name")
        .setView(editText)
        .setPositiveButton("OK") { dialog, _ ->
            val name = editText.text.toString()
            if (name.isNotBlank()) {
                onNameReceived(name)
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        .show()
}

@Composable
fun PersonItem(person: Person) {
    Text(
        text = "${person.name}: ${person.coffeeCount}",
        modifier = Modifier.padding(start = 16.dp),
        fontSize = 24.sp
    )
}