package com.example.domopotapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.MainActivity
import com.example.domopotapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Login : Fragment(R.layout.login_fragment) {

    private val TAG = MainActivity::class.java.name
    private lateinit var login: Button
    private val viewModel by activityViewModels<MainViewModel>()


    private lateinit var mEventListener: ValueEventListener


    companion object{
        private const val RC_SIGN_IN = 120
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.w("Current user: ", viewModel.mAuth.currentUser.toString())


        login = view.findViewById<Button>(R.id.loginBtn)

        //disabilita il tasto "back"
        if(viewModel.mAuth.currentUser != null){
            findNavController().navigate(R.id.login_to_home)
        }

        viewModel.googleSignInClient = activity?.let { GoogleSignIn.getClient(it, viewModel.gso) }!!
        login.setOnClickListener {
            signIn()
            //findNavController().navigate(R.id.login_to_home)


        }
    }

    private fun signIn(){
        val signInIntent = viewModel.googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if(task.isSuccessful){
                try{
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("SignInActivity", "FirebaseAuthWithGoogle: "+ account.id)
                    firebaseAuthWithGoogle(account.idToken)
                }catch (e: ApiException){
                    Log.w("SignInActivity", "Google sign in failed", e)
                }
            }else{
                Log.w("SignInActivity", exception.toString())
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModel.mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Log.d("SignInActivity", "signInWithCredential:success")
                    val user = viewModel.mAuth.currentUser
                    if (user != null) {
                        writeUserInBD(user)
                    } else {
                        Log.w("SignInActivity", "signInWithCredential:failure")
                    }
                }
            }
    }

    fun writeUserInBD(user: FirebaseUser)  {
        mEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = snapshot.child(user.uid).value
                Log.w("DB REsponse: ", "VALUE: "+ value.toString())
                if (value == null){
                    Log.w("DB REsponse: ", "Utente non presente nel DB")
                    val values: MutableMap<String, Any> = HashMap()
                    values["eMail"] = user.email.toString()
                    values["name"] = user.displayName.toString()
                    values["pots"] = 0
                    viewModel.db.child("Users")
                        .child(user.uid)
                        .setValue(values)
                    viewModel.db.removeEventListener(mEventListener)

                    findNavController().navigate(R.id.login_to_home)

                }else{
                    Log.w("DB REsponse: ", "Utente già presente nel DB")
                    viewModel.db.removeEventListener(mEventListener)

                    findNavController().navigate(R.id.login_to_home)
                }

            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("DB REsponse: ", "Failed to read value.", error.toException())
            }
        }
        viewModel.db.child("Users").addListenerForSingleValueEvent(mEventListener)


    }

}

