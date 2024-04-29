package com.example.securepass

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Keep track of the private key for each user
    private val userPrivateKeys: MutableMap<String, BigInteger> = HashMap()
    private var y: BigInteger? = null

    private val ONESIGNAL_APP_ID = "1906e057-2ac0-426a-a415-b7849565fdd8"


    //set layout to activity_main, set the button colors, and displays pop-up box for forgot password
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            //println(token)
            //Log.d("hi", "myID: " + token)
            //Toast.makeText(baseContext, "token is: " + token, Toast.LENGTH_SHORT).show()
        })





        //set text and button color
        val usernameInputLayout = findViewById<TextInputLayout>(R.id.usernameInputLayout)
        val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)

        usernameEditText.setHintTextColor(resources.getColor(R.color.ivory_white))
        usernameInputLayout.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.ivory_white))

        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)

        passwordEditText.setHintTextColor(resources.getColor(R.color.ivory_white))
        passwordInputLayout.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.ivory_white))

        val forgotPassText: TextView = findViewById(R.id.forgotPassText)

        //function for "forgot password"
        forgotPassText.setOnClickListener{
            val emailEditText = EditText(this)
            emailEditText.hint = "Enter your email"

            AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage("Enter your email to reset the password")
                .setView(emailEditText)
                .setPositiveButton("Reset") { _, _ ->
                    val email = emailEditText.text.toString()

                    if (email.isNotEmpty()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Password reset email sent to $email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to send password reset email. Please check your email and try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Enter your email address", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }



    }


    fun generateOrRetrievePrivateKey(userUid: String): BigInteger {
        return userPrivateKeys.computeIfAbsent(userUid) {
            // Generate a new private key if not available
            BigInteger(256, SecureRandom())
        }
    }

    fun generateSchnorrProof(password: String, privateKey: BigInteger): Pair<BigInteger?, BigInteger> {
        val random = SecureRandom()
        val g = BigInteger("2") // Generator
        val h = hash(password.toByteArray()) // Hash of the password

        y = g.modPow(privateKey, h) // Set the value of y

        val r = BigInteger(256, random) // Random value
        val c = y?.toByteArray()
            ?.let { hash(g.toByteArray(), h.toByteArray(), it, g.modPow(r, h).toByteArray()) }
        val s = r.add(privateKey.multiply(c))

        return Pair(c, s)
    }



    /*fun verifySchnorrProof(password: String, publicKey: BigInteger, proof: Pair<BigInteger, BigInteger>): Boolean {
        val g = BigInteger("2")
        val h = hash(password.toByteArray())

        val c = proof.first
        val s = proof.second

        val v1 = g.modPow(s, h)
        val v2 = publicKey.modPow(c, h)
        val v3 = (g.modPow(c, h) * v2) % h

        val calculatedC = hash(g.toByteArray(), h.toByteArray(), publicKey.toByteArray(), v1.toByteArray())

        Log.d("ZKP", "v1: $v1")
        Log.d("ZKP", "v2: $v2")
        Log.d("ZKP", "v3: $v3")
        Log.d("ZKP", "calculatedC: $calculatedC")

        Log.d("ZKP", "g^s mod h: ${g.modPow(s, h)}")

        return calculatedC == c && v3 == g.modPow(s, h)
    }*/

    fun verifySchnorrProof(password: String, publicKey: BigInteger, proof: Pair<BigInteger?, BigInteger>): Boolean {
        val g = BigInteger("2")
        val h = hash(password.toByteArray())

        val c = proof.first
        val s = proof.second

        val v1 = g.modPow(s, h)
        val v2 = publicKey.modPow(c, h)
       // val v3 = (g.modPow(c, h) * y!!.modPow(c, h)).mod(h) // Include y (public key) in the calculation

        val v3 = g.modPow(c, h).multiply(y!!.modPow(s, h)).mod(h)

        val calculatedC = hash(g.toByteArray(), h.toByteArray(), publicKey.toByteArray(), v1.toByteArray())

        // Logging for debugging
        println("c: $c")
        println("calculatedC: $calculatedC")
        println("v3: $v3")
        println("v2: $v2")

        return calculatedC == c && v3 == v2
    }


    fun hash(vararg values: ByteArray): BigInteger {
        val digest = MessageDigest.getInstance("SHA-256")
        for (value in values) {
            digest.update(value)
        }
        val hashedBytes = digest.digest()
        return BigInteger(1, hashedBytes)
    }



    //responsible for login by authenticating credentials and move to home screen on valid authentication
    fun onLogin(v: View) {
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // User successfully logged in
                        val user = auth.currentUser
                        val userId = user?.uid
                        val userEmail = user?.email
                        Log.e("Firestore", "user uid: $userId")
                        Log.e("Firestore", "user email: $userEmail")

                        // Save login time to Firestore
                        handleSuccessfulLogin(user)

                        val profileIntent = Intent(this, HomeActivity::class.java)
                        startActivity(profileIntent)
                    } else {
                        // Handle login failure
                        Log.e("Firestore", "Login failed: ${task.exception}")
                        //Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            //error message if the username or password is empty
            Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    /*fun onLogin(v: View) {
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            val user = auth.currentUser
            val userUid = user?.uid

            if (userUid != null) {
                val publicKey = hash(userUid.toByteArray()) // Use UID as a simplified public key
                val privateKey = generateOrRetrievePrivateKey(userUid)

                val passwordProof = generateSchnorrProof(password, privateKey)
                val isVerified = verifySchnorrProof(password, publicKey, passwordProof)

                Log.d("ZKP", "Public Key: $publicKey")
                Log.d("ZKP", "Private Key: $privateKey")
                Log.d("ZKP", "Generated Proof: $passwordProof")
                Log.d("ZKP", "Is Verified: $isVerified")

                if (isVerified) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // User successfully logged in
                                val loggedInUser = auth.currentUser
                                val userId = loggedInUser?.uid
                                val userEmail = loggedInUser?.email
                                Log.e("Firestore", "user uid: $userId")
                                Log.e("Firestore", "user email: $userEmail")

                                // Save login time to Firestore
                                handleSuccessfulLogin(loggedInUser)

                                val profileIntent = Intent(this, HomeActivity::class.java)
                                startActivity(profileIntent)
                            } else {
                                // Handle login failure
                                Log.e("Firestore", "Login failed: ${task.exception}")
                                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Invalid username or password
                    Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle the case where the user does not have a UID
                Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Show an error message if the username or password is empty
            Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }*/

    //navigate to sign-up page
    fun onSignUp(v : View){
        val profileIntent = Intent(this, SignUp::class.java)
        startActivity(profileIntent)
    }


    // Function to save login time to Firestore
    private fun handleSuccessfulLogin(user: FirebaseUser?) {
        if (user != null) {
            saveLoginTimeToFirestore(user.uid)
        }
    }

    //record user's login time
    private fun saveLoginTimeToFirestore(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Create a new document with the login time
        val loginTimeData = hashMapOf(
            "loginTime" to FieldValue.serverTimestamp() // Use server timestamp to ensure accuracy
        )

        // Add the document to the "loginTimes" collection for the user
        db.collection("users").document(userId)
            .collection("loginTimes").add(loginTimeData)
            .addOnSuccessListener { documentReference ->
                // Log the login time successfully
                Log.e("Firestore", "Login time successfully recorded in Firestore")
            }
            .addOnFailureListener { e ->
                // Handle the failure
                Log.e("Firestore", "Failed to record Login time in Firestore")
            }
    }

    /*fun handlePasswordRequestWithApproval(onApproval: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        // Display authentication/approval dialog
        displayApprovalDialog(
            onApproval = {
                // If user approves, proceed with fetching and decrypting passwords
                val user = auth.currentUser
                if (user != null) {
                    val userId = user.uid
                    fetchAndDecryptPasswords(userId, onApproval, onFailure)
                } else {
                    onFailure("User not authenticated")
                }
            },
            onCancel = {
                // Handle case where user cancels approval
                onFailure("User canceled approval")
            }
        )
    }

    // Display authentication/approval dialog
    private fun displayApprovalDialog(onApproval: () -> Unit, onCancel: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Approval Required")
            .setMessage("Do you approve the password retrieval?")
            .setPositiveButton("Approve") { dialog, which ->
                // User clicked Approve
                onApproval()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // User clicked Cancel
                onCancel()
                dialog.dismiss()
            }
            .setCancelable(false) // Prevent dismissing the dialog with outside touch or back button

        val dialog = builder.create()
        dialog.show()
    }

    // Fetch and decrypt passwords from Firestore
    private fun fetchAndDecryptPasswords(userId: String, onApproval: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        // Create a Firestore reference to the "user_passwords" sub-collection
        val userPasswordsCollection = FirebaseFirestore.getInstance()
            .collection("passwords")
            .document(userId)
            .collection("user_passwords")

        // Use your app context and alias to create an Encrypter instance
        val appContext = applicationContext
        val encrypter = Encrypter.Builder(appContext, "password_alias")
            .build()

        // Fetch passwords from Firestore and decrypt
        userPasswordsCollection.get().addOnSuccessListener { documents ->
            val decryptedPasswords = mutableListOf<String>()
            for (document in documents) {
                val password = document.getString("password")?.let { encryptedData ->
                    try {
                        val decryptedData = encrypter.decryptOrNull(encryptedData)
                        decryptedData?.toString() ?: ""
                    } catch (e: Exception) {
                        onFailure("Decryption error for password: ${e.message}")
                    }
                } ?: ""

                decryptedPasswords.add(password.toString())
            }

            // Call onApproval with the decrypted passwords
            onApproval(decryptedPasswords)
        }.addOnFailureListener { exception ->
            // Handle Firestore query failure
            onFailure("Error querying Firestore: $exception")
        }
    }*/

}