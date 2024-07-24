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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;

/**
 * Represents a Material color scheme, a mapping of color roles to colors.
 *
 * @deprecated Please use com.google.ux.material.libmonet.dynamiccolor.DynamicScheme instead. Refer
 *     to https://github.com/material-foundation/material-color-utilities/blob/main/make_schemes.md
 *     for migration guidance.
 * @hide
 */
@Deprecated
@RestrictTo(LIBRARY_GROUP)
@CheckReturnValue
public class Scheme {
  private int primary;
  private int onPrimary;
  private int primaryContainer;
  private int onPrimaryContainer;
  private int secondary;
  private int onSecondary;
  private int secondaryContainer;
  private int onSecondaryContainer;
  private int tertiary;
  private int onTertiary;
  private int tertiaryContainer;
  private int onTertiaryContainer;
  private int error;
  private int onError;
  private int errorContainer;
  private int onErrorContainer;
  private int background;
  private int onBackground;
  private int surface;
  private int onSurface;
  private int surfaceVariant;
  private int onSurfaceVariant;
  private int outline;
  private int outlineVariant;
  private int shadow;
  private int scrim;
  private int inverseSurface;
  private int inverseOnSurface;
  private int inversePrimary;

  public Scheme() {}

  public Scheme(
      int primary,
      int onPrimary,
      int primaryContainer,
      int onPrimaryContainer,
      int secondary,
      int onSecondary,
      int secondaryContainer,
      int onSecondaryContainer,
      int tertiary,
      int onTertiary,
      int tertiaryContainer,
      int onTertiaryContainer,
      int error,
      int onError,
      int errorContainer,
      int onErrorContainer,
      int background,
      int onBackground,
      int surface,
      int onSurface,
      int surfaceVariant,
      int onSurfaceVariant,
      int outline,
      int outlineVariant,
      int shadow,
      int scrim,
      int inverseSurface,
      int inverseOnSurface,
      int inversePrimary) {
    super();
    this.primary = primary;
    this.onPrimary = onPrimary;
    this.primaryContainer = primaryContainer;
    this.onPrimaryContainer = onPrimaryContainer;
    this.secondary = secondary;
    this.onSecondary = onSecondary;
    this.secondaryContainer = secondaryContainer;
    this.onSecondaryContainer = onSecondaryContainer;
    this.tertiary = tertiary;
    this.onTertiary = onTertiary;
    this.tertiaryContainer = tertiaryContainer;
    this.onTertiaryContainer = onTertiaryContainer;
    this.error = error;
    this.onError = onError;
    this.errorContainer = errorContainer;
    this.onErrorContainer = onErrorContainer;
    this.background = background;
    this.onBackground = onBackground;
    this.surface = surface;
    this.onSurface = onSurface;
    this.surfaceVariant = surfaceVariant;
    this.onSurfaceVariant = onSurfaceVariant;
    this.outline = outline;
    this.outlineVariant = outlineVariant;
    this.shadow = shadow;
    this.scrim = scrim;
    this.inverseSurface = inverseSurface;
    this.inverseOnSurface = inverseOnSurface;
    this.inversePrimary = inversePrimary;
  }

  /** Creates a light theme Scheme from a source color in ARGB, i.e. a hex code. */
  public static Scheme light(int argb) {
    return lightFromCorePalette(CorePalette.of(argb));
  }

  /** Creates a dark theme Scheme from a source color in ARGB, i.e. a hex code. */
  public static Scheme dark(int argb) {
    return darkFromCorePalette(CorePalette.of(argb));
  }

  /** Creates a light theme content-based Scheme from a source color in ARGB, i.e. a hex code. */
  public static Scheme lightContent(int argb) {
    return lightFromCorePalette(CorePalette.contentOf(argb));
  }

  /** Creates a dark theme content-based Scheme from a source color in ARGB, i.e. a hex code. */
  public static Scheme darkContent(int argb) {
    return darkFromCorePalette(CorePalette.contentOf(argb));
  }

  private static Scheme lightFromCorePalette(CorePalette core) {
    return new Scheme()
        .withPrimary(core.a1.tone(40))
        .withOnPrimary(core.a1.tone(100))
        .withPrimaryContainer(core.a1.tone(90))
        .withOnPrimaryContainer(core.a1.tone(10))
        .withSecondary(core.a2.tone(40))
        .withOnSecondary(core.a2.tone(100))
        .withSecondaryContainer(core.a2.tone(90))
        .withOnSecondaryContainer(core.a2.tone(10))
        .withTertiary(core.a3.tone(40))
        .withOnTertiary(core.a3.tone(100))
        .withTertiaryContainer(core.a3.tone(90))
        .withOnTertiaryContainer(core.a3.tone(10))
        .withError(core.error.tone(40))
        .withOnError(core.error.tone(100))
        .withErrorContainer(core.error.tone(90))
        .withOnErrorContainer(core.error.tone(10))
        .withBackground(core.n1.tone(99))
        .withOnBackground(core.n1.tone(10))
        .withSurface(core.n1.tone(99))
        .withOnSurface(core.n1.tone(10))
        .withSurfaceVariant(core.n2.tone(90))
        .withOnSurfaceVariant(core.n2.tone(30))
        .withOutline(core.n2.tone(50))
        .withOutlineVariant(core.n2.tone(80))
        .withShadow(core.n1.tone(0))
        .withScrim(core.n1.tone(0))
        .withInverseSurface(core.n1.tone(20))
        .withInverseOnSurface(core.n1.tone(95))
        .withInversePrimary(core.a1.tone(80));
  }

  private static Scheme darkFromCorePalette(CorePalette core) {
    return new Scheme()
        .withPrimary(core.a1.tone(80))
        .withOnPrimary(core.a1.tone(20))
        .withPrimaryContainer(core.a1.tone(30))
        .withOnPrimaryContainer(core.a1.tone(90))
        .withSecondary(core.a2.tone(80))
        .withOnSecondary(core.a2.tone(20))
        .withSecondaryContainer(core.a2.tone(30))
        .withOnSecondaryContainer(core.a2.tone(90))
        .withTertiary(core.a3.tone(80))
        .withOnTertiary(core.a3.tone(20))
        .withTertiaryContainer(core.a3.tone(30))
        .withOnTertiaryContainer(core.a3.tone(90))
        .withError(core.error.tone(80))
        .withOnError(core.error.tone(20))
        .withErrorContainer(core.error.tone(30))
        .withOnErrorContainer(core.error.tone(80))
        .withBackground(core.n1.tone(10))
        .withOnBackground(core.n1.tone(90))
        .withSurface(core.n1.tone(10))
        .withOnSurface(core.n1.tone(90))
        .withSurfaceVariant(core.n2.tone(30))
        .withOnSurfaceVariant(core.n2.tone(80))
        .withOutline(core.n2.tone(60))
        .withOutlineVariant(core.n2.tone(30))
        .withShadow(core.n1.tone(0))
        .withScrim(core.n1.tone(0))
        .withInverseSurface(core.n1.tone(90))
        .withInverseOnSurface(core.n1.tone(20))
        .withInversePrimary(core.a1.tone(40));
  }

  public int getPrimary() {
    return primary;
  }

  public void setPrimary(int primary) {
    this.primary = primary;
  }

  @CanIgnoreReturnValue
  public Scheme withPrimary(int primary) {
    this.primary = primary;
    return this;
  }

  public int getOnPrimary() {
    return onPrimary;
  }

  public void setOnPrimary(int onPrimary) {
    this.onPrimary = onPrimary;
  }

  @CanIgnoreReturnValue
  public Scheme withOnPrimary(int onPrimary) {
    this.onPrimary = onPrimary;
    return this;
  }

  public int getPrimaryContainer() {
    return primaryContainer;
  }

  public void setPrimaryContainer(int primaryContainer) {
    this.primaryContainer = primaryContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withPrimaryContainer(int primaryContainer) {
    this.primaryContainer = primaryContainer;
    return this;
  }

  public int getOnPrimaryContainer() {
    return onPrimaryContainer;
  }

  public void setOnPrimaryContainer(int onPrimaryContainer) {
    this.onPrimaryContainer = onPrimaryContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withOnPrimaryContainer(int onPrimaryContainer) {
    this.onPrimaryContainer = onPrimaryContainer;
    return this;
  }

  public int getSecondary() {
    return secondary;
  }

  public void setSecondary(int secondary) {
    this.secondary = secondary;
  }

  @CanIgnoreReturnValue
  public Scheme withSecondary(int secondary) {
    this.secondary = secondary;
    return this;
  }

  public int getOnSecondary() {
    return onSecondary;
  }

  public void setOnSecondary(int onSecondary) {
    this.onSecondary = onSecondary;
  }

  @CanIgnoreReturnValue
  public Scheme withOnSecondary(int onSecondary) {
    this.onSecondary = onSecondary;
    return this;
  }

  public int getSecondaryContainer() {
    return secondaryContainer;
  }

  public void setSecondaryContainer(int secondaryContainer) {
    this.secondaryContainer = secondaryContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withSecondaryContainer(int secondaryContainer) {
    this.secondaryContainer = secondaryContainer;
    return this;
  }

  public int getOnSecondaryContainer() {
    return onSecondaryContainer;
  }

  public void setOnSecondaryContainer(int onSecondaryContainer) {
    this.onSecondaryContainer = onSecondaryContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withOnSecondaryContainer(int onSecondaryContainer) {
    this.onSecondaryContainer = onSecondaryContainer;
    return this;
  }

  public int getTertiary() {
    return tertiary;
  }

  public void setTertiary(int tertiary) {
    this.tertiary = tertiary;
  }

  @CanIgnoreReturnValue
  public Scheme withTertiary(int tertiary) {
    this.tertiary = tertiary;
    return this;
  }

  public int getOnTertiary() {
    return onTertiary;
  }

  public void setOnTertiary(int onTertiary) {
    this.onTertiary = onTertiary;
  }

  @CanIgnoreReturnValue
  public Scheme withOnTertiary(int onTertiary) {
    this.onTertiary = onTertiary;
    return this;
  }

  public int getTertiaryContainer() {
    return tertiaryContainer;
  }

  public void setTertiaryContainer(int tertiaryContainer) {
    this.tertiaryContainer = tertiaryContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withTertiaryContainer(int tertiaryContainer) {
    this.tertiaryContainer = tertiaryContainer;
    return this;
  }

  public int getOnTertiaryContainer() {
    return onTertiaryContainer;
  }

  public void setOnTertiaryContainer(int onTertiaryContainer) {
    this.onTertiaryContainer = onTertiaryContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withOnTertiaryContainer(int onTertiaryContainer) {
    this.onTertiaryContainer = onTertiaryContainer;
    return this;
  }

  public int getError() {
    return error;
  }

  public void setError(int error) {
    this.error = error;
  }

  @CanIgnoreReturnValue
  public Scheme withError(int error) {
    this.error = error;
    return this;
  }

  public int getOnError() {
    return onError;
  }

  public void setOnError(int onError) {
    this.onError = onError;
  }

  @CanIgnoreReturnValue
  public Scheme withOnError(int onError) {
    this.onError = onError;
    return this;
  }

  public int getErrorContainer() {
    return errorContainer;
  }

  public void setErrorContainer(int errorContainer) {
    this.errorContainer = errorContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withErrorContainer(int errorContainer) {
    this.errorContainer = errorContainer;
    return this;
  }

  public int getOnErrorContainer() {
    return onErrorContainer;
  }

  public void setOnErrorContainer(int onErrorContainer) {
    this.onErrorContainer = onErrorContainer;
  }

  @CanIgnoreReturnValue
  public Scheme withOnErrorContainer(int onErrorContainer) {
    this.onErrorContainer = onErrorContainer;
    return this;
  }

  public int getBackground() {
    return background;
  }

  public void setBackground(int background) {
    this.background = background;
  }

  @CanIgnoreReturnValue
  public Scheme withBackground(int background) {
    this.background = background;
    return this;
  }

  public int getOnBackground() {
    return onBackground;
  }

  public void setOnBackground(int onBackground) {
    this.onBackground = onBackground;
  }

  @CanIgnoreReturnValue
  public Scheme withOnBackground(int onBackground) {
    this.onBackground = onBackground;
    return this;
  }

  public int getSurface() {
    return surface;
  }

  public void setSurface(int surface) {
    this.surface = surface;
  }

  @CanIgnoreReturnValue
  public Scheme withSurface(int surface) {
    this.surface = surface;
    return this;
  }

  public int getOnSurface() {
    return onSurface;
  }

  public void setOnSurface(int onSurface) {
    this.onSurface = onSurface;
  }

  @CanIgnoreReturnValue
  public Scheme withOnSurface(int onSurface) {
    this.onSurface = onSurface;
    return this;
  }

  public int getSurfaceVariant() {
    return surfaceVariant;
  }

  public void setSurfaceVariant(int surfaceVariant) {
    this.surfaceVariant = surfaceVariant;
  }

  @CanIgnoreReturnValue
  public Scheme withSurfaceVariant(int surfaceVariant) {
    this.surfaceVariant = surfaceVariant;
    return this;
  }

  public int getOnSurfaceVariant() {
    return onSurfaceVariant;
  }

  public void setOnSurfaceVariant(int onSurfaceVariant) {
    this.onSurfaceVariant = onSurfaceVariant;
  }

  @CanIgnoreReturnValue
  public Scheme withOnSurfaceVariant(int onSurfaceVariant) {
    this.onSurfaceVariant = onSurfaceVariant;
    return this;
  }

  public int getOutline() {
    return outline;
  }

  public void setOutline(int outline) {
    this.outline = outline;
  }

  @CanIgnoreReturnValue
  public Scheme withOutline(int outline) {
    this.outline = outline;
    return this;
  }

  public int getOutlineVariant() {
    return outlineVariant;
  }

  public void setOutlineVariant(int outlineVariant) {
    this.outlineVariant = outlineVariant;
  }

  @CanIgnoreReturnValue
  public Scheme withOutlineVariant(int outlineVariant) {
    this.outlineVariant = outlineVariant;
    return this;
  }

  public int getShadow() {
    return shadow;
  }

  public void setShadow(int shadow) {
    this.shadow = shadow;
  }

  @CanIgnoreReturnValue
  public Scheme withShadow(int shadow) {
    this.shadow = shadow;
    return this;
  }

  public int getScrim() {
    return scrim;
  }

  public void setScrim(int scrim) {
    this.scrim = scrim;
  }

  @CanIgnoreReturnValue
  public Scheme withScrim(int scrim) {
    this.scrim = scrim;
    return this;
  }

  public int getInverseSurface() {
    return inverseSurface;
  }

  public void setInverseSurface(int inverseSurface) {
    this.inverseSurface = inverseSurface;
  }

  @CanIgnoreReturnValue
  public Scheme withInverseSurface(int inverseSurface) {
    this.inverseSurface = inverseSurface;
    return this;
  }

  public int getInverseOnSurface() {
    return inverseOnSurface;
  }

  public void setInverseOnSurface(int inverseOnSurface) {
    this.inverseOnSurface = inverseOnSurface;
  }

  @CanIgnoreReturnValue
  public Scheme withInverseOnSurface(int inverseOnSurface) {
    this.inverseOnSurface = inverseOnSurface;
    return this;
  }

  public int getInversePrimary() {
    return inversePrimary;
  }

  public void setInversePrimary(int inversePrimary) {
    this.inversePrimary = inversePrimary;
  }

  @CanIgnoreReturnValue
  public Scheme withInversePrimary(int inversePrimary) {
    this.inversePrimary = inversePrimary;
    return this;
  }

  @Override
  public String toString() {
    return "Scheme{"
        + "primary="
        + primary
        + ", onPrimary="
        + onPrimary
        + ", primaryContainer="
        + primaryContainer
        + ", onPrimaryContainer="
        + onPrimaryContainer
        + ", secondary="
        + secondary
        + ", onSecondary="
        + onSecondary
        + ", secondaryContainer="
        + secondaryContainer
        + ", onSecondaryContainer="
        + onSecondaryContainer
        + ", tertiary="
        + tertiary
        + ", onTertiary="
        + onTertiary
        + ", tertiaryContainer="
        + tertiaryContainer
        + ", onTertiaryContainer="
        + onTertiaryContainer
        + ", error="
        + error
        + ", onError="
        + onError
        + ", errorContainer="
        + errorContainer
        + ", onErrorContainer="
        + onErrorContainer
        + ", background="
        + background
        + ", onBackground="
        + onBackground
        + ", surface="
        + surface
        + ", onSurface="
        + onSurface
        + ", surfaceVariant="
        + surfaceVariant
        + ", onSurfaceVariant="
        + onSurfaceVariant
        + ", outline="
        + outline
        + ", outlineVariant="
        + outlineVariant
        + ", shadow="
        + shadow
        + ", scrim="
        + scrim
        + ", inverseSurface="
        + inverseSurface
        + ", inverseOnSurface="
        + inverseOnSurface
        + ", inversePrimary="
        + inversePrimary
        + '}';
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof Scheme)) {
      return false;
    }

    Scheme scheme = (Scheme) object;

    if (primary != scheme.primary) {
      return false;
    }
    if (onPrimary != scheme.onPrimary) {
      return false;
    }
    if (primaryContainer != scheme.primaryContainer) {
      return false;
    }
    if (onPrimaryContainer != scheme.onPrimaryContainer) {
      return false;
    }
    if (secondary != scheme.secondary) {
      return false;
    }
    if (onSecondary != scheme.onSecondary) {
      return false;
    }
    if (secondaryContainer != scheme.secondaryContainer) {
      return false;
    }
    if (onSecondaryContainer != scheme.onSecondaryContainer) {
      return false;
    }
    if (tertiary != scheme.tertiary) {
      return false;
    }
    if (onTertiary != scheme.onTertiary) {
      return false;
    }
    if (tertiaryContainer != scheme.tertiaryContainer) {
      return false;
    }
    if (onTertiaryContainer != scheme.onTertiaryContainer) {
      return false;
    }
    if (error != scheme.error) {
      return false;
    }
    if (onError != scheme.onError) {
      return false;
    }
    if (errorContainer != scheme.errorContainer) {
      return false;
    }
    if (onErrorContainer != scheme.onErrorContainer) {
      return false;
    }
    if (background != scheme.background) {
      return false;
    }
    if (onBackground != scheme.onBackground) {
      return false;
    }
    if (surface != scheme.surface) {
      return false;
    }
    if (onSurface != scheme.onSurface) {
      return false;
    }
    if (surfaceVariant != scheme.surfaceVariant) {
      return false;
    }
    if (onSurfaceVariant != scheme.onSurfaceVariant) {
      return false;
    }
    if (outline != scheme.outline) {
      return false;
    }
    if (outlineVariant != scheme.outlineVariant) {
      return false;
    }
    if (shadow != scheme.shadow) {
      return false;
    }
    if (scrim != scheme.scrim) {
      return false;
    }
    if (inverseSurface != scheme.inverseSurface) {
      return false;
    }
    if (inverseOnSurface != scheme.inverseOnSurface) {
      return false;
    }
    if (inversePrimary != scheme.inversePrimary) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = System.identityHashCode(this);
    result = 31 * result + primary;
    result = 31 * result + onPrimary;
    result = 31 * result + primaryContainer;
    result = 31 * result + onPrimaryContainer;
    result = 31 * result + secondary;
    result = 31 * result + onSecondary;
    result = 31 * result + secondaryContainer;
    result = 31 * result + onSecondaryContainer;
    result = 31 * result + tertiary;
    result = 31 * result + onTertiary;
    result = 31 * result + tertiaryContainer;
    result = 31 * result + onTertiaryContainer;
    result = 31 * result + error;
    result = 31 * result + onError;
    result = 31 * result + errorContainer;
    result = 31 * result + onErrorContainer;
    result = 31 * result + background;
    result = 31 * result + onBackground;
    result = 31 * result + surface;
    result = 31 * result + onSurface;
    result = 31 * result + surfaceVariant;
    result = 31 * result + onSurfaceVariant;
    result = 31 * result + outline;
    result = 31 * result + outlineVariant;
    result = 31 * result + shadow;
    result = 31 * result + scrim;
    result = 31 * result + inverseSurface;
    result = 31 * result + inverseOnSurface;
    result = 31 * result + inversePrimary;
    return result;
  }
}
