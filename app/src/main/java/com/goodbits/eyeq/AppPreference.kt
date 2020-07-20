package com.goodbits.eyeq

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.preference.PreferenceManager

/**
 * Created by vimal on 8/11/17.
 */
class AppPreference(context: Context) {
    companion object {

        private var preferences: SharedPreferences? = null
        private val IS_USER_LOGIN = "IsUserLoggedIn"
        private val IS_USER_SIGNEDUP = "IsUserSignedUp"
        private val IS_FORM_SHOWN = "IsFormShown"
        internal lateinit var editor: SharedPreferences.Editor
        internal lateinit var context: Context
    }


    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        editor = preferences?.edit()!!
    }

//    fun savePreference(key: String, value: Boolean?) {
//        preferences?.edit()?.putBoolean(key, value!!)?.apply()
//    }
//
//    fun savePreference(key: String, value: String) {
//        preferences?.edit()?.putString(key, value)?.commit()
//    }

    fun isFormShown(formShown: Boolean) {

        editor.putBoolean(IS_USER_LOGIN, true)
        editor.putBoolean(IS_FORM_SHOWN, true)
        editor.commit()

    }

//    fun userSignup(token: String) {
//        editor.putBoolean(IS_USER_SIGNEDUP, true)
//        editor.commit()
//    }

//    fun logoutUser() {
//        // Clearing all user data from Shared Preferences
//        editor.clear()
//        editor.commit()
//
//    }


    fun isUserLoggedIn(): Boolean {
        return preferences!!.getBoolean(IS_USER_LOGIN, false)
    }

    fun isUserSignedUp(): Boolean {
        return preferences!!.getBoolean(IS_USER_SIGNEDUP, false)
    }

}