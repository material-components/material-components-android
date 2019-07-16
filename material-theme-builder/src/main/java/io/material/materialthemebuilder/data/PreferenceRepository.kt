/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.materialthemebuilder.data

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * A simple data repository for in-app settings.
 */
class PreferenceRepository(private val sharedPreferences: SharedPreferences) {

  val nightMode: Int
    get() = sharedPreferences.getInt(PREFERENCE_NIGHT_MODE, PREFERENCE_NIGHT_MODE_DEF_VAL)

  private val _nightModeLive: MutableLiveData<Int> = MutableLiveData()
  val nightModeLive: LiveData<Int>
    get() = _nightModeLive

  var isDarkTheme: Boolean = false
    get() = nightMode == AppCompatDelegate.MODE_NIGHT_YES
    set(value) {
      sharedPreferences.edit().putInt(PREFERENCE_NIGHT_MODE, if (value) {
        AppCompatDelegate.MODE_NIGHT_YES
      } else {
        AppCompatDelegate.MODE_NIGHT_NO
      }).apply()
      field = value
    }

  private val _isDarkThemeLive: MutableLiveData<Boolean> = MutableLiveData()
  val isDarkThemeLive: LiveData<Boolean>
    get() = _isDarkThemeLive

  private val preferenceChangedListener =
    SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
      when (key) {
        PREFERENCE_NIGHT_MODE -> {
          _nightModeLive.value = nightMode
          _isDarkThemeLive.value = isDarkTheme
        }
      }
    }

  init {
    // Init preference LiveData objects.
    _nightModeLive.value = nightMode
    _isDarkThemeLive.value = isDarkTheme

    sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
  }

  companion object {
    private const val PREFERENCE_NIGHT_MODE = "preference_night_mode"
    private const val PREFERENCE_NIGHT_MODE_DEF_VAL = AppCompatDelegate.MODE_NIGHT_NO
  }
}
