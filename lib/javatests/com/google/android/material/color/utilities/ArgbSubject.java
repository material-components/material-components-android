/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.color.utilities;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

/** Enables assertions involving colors as integers to log as hex codes instead of raw integers. */
public final class ArgbSubject extends Subject {

  public static ArgbSubject assertThat(int argb) {
    return assertAbout(ArgbSubject::new).that(argb);
  }

  private final int actual;

  private ArgbSubject(FailureMetadata failureMetadata, int subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  @Override
  protected String actualCustomStringRepresentation() {
    return userFriendlyHexCode(actual);
  }

  public void isSameColorAs(int otherArgb) {
    if (otherArgb != actual) {
      failWithActual("color", userFriendlyHexCode(otherArgb));
    }
  }

  private static String userFriendlyHexCode(int integer) {
    String hexString = Integer.toHexString(integer);
    if (hexString.length() > 6) {
      return String.format("#%s", hexString.substring(hexString.length() - 6));
    } else if (hexString.length() == 6) {
      return String.format("#%s", hexString);
    } else {
      return String.format("??? #%s", hexString);
    }
  }
}
