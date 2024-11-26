/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.google.android.material.shape;

import static java.lang.Math.PI;
import static java.lang.Math.min;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.graphics.shapes.CornerRounding;
import androidx.graphics.shapes.RoundedPolygon;
import androidx.graphics.shapes.RoundedPolygonKt;
import androidx.graphics.shapes.ShapesKt;
import androidx.graphics.shapes.Shapes_androidKt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class providing static methods for creating Material endorsed shapes using the {@code
 * androidx.graphics.shapes} library.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialShapes {
  // Cache various roundings for use below
  private static final CornerRounding CORNER_ROUND_15 =
      new CornerRounding(/* radius= */ .15f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_20 =
      new CornerRounding(/* radius= */ .2f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_30 =
      new CornerRounding(/* radius= */ .3f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_50 =
      new CornerRounding(/* radius= */ .5f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_100 =
      new CornerRounding(/* radius= */ 1f, /* smoothing= */ 0f);

  /** A circle shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon CIRCLE = normalize(getCircle(), /* radial= */ true);

  /** A rounded square shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon SQUARE = normalize(getSquare(), /* radial= */ true);

  /** A slanted rounded square shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon SLANTED_SQUARE =
      normalize(getSlantedSquare(), /* radial= */ true);

  /** An arch shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon ARCH = normalize(getArch(), /* radial= */ true);

  /** A fan shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon FAN = normalize(getFan(), /* radial= */ true);

  /** An arrow shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon ARROW = normalize(getArrow(), /* radial= */ true);

  /** A semi-circle shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon SEMI_CIRCLE = normalize(getSemiCircle(), /* radial= */ true);

  /** An oval shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon OVAL =
      normalize(getOval(/* rotateDegrees= */ -45f), /* radial= */ true);

  /** A pill shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon PILL = normalize(getPill(), /* radial= */ true);

  /** A triangle shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon TRIANGLE =
      normalize(getTriangle(/* rotateDegrees= */ -90f), /* radial= */ true);

  /** A diamond shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon DIAMOND = normalize(getDiamond(), /* radial= */ true);

  /** A clam-shell shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon CLAM_SHELL = normalize(getClamShell(), /* radial= */ true);

  /** A pentagon shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon PENTAGON = normalize(getPentagon(), /* radial= */ true);

  /** A gem shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon GEM =
      normalize(getGem(/* rotateDegrees= */ -90f), /* radial= */ true);

  public static final RoundedPolygon SUNNY = normalize(getSunny(), /* radial= */ true);
  public static final RoundedPolygon VERY_SUNNY = normalize(getVerySunny(), /* radial= */ true);
  public static final RoundedPolygon COOKIE_4 = normalize(getCookie4(), /* radial= */ true);
  public static final RoundedPolygon COOKIE_6 = normalize(getCookie6(), /* radial= */ true);
  public static final RoundedPolygon COOKIE_7 = normalize(getCookie7(), /* radial= */ true);
  public static final RoundedPolygon COOKIE_9 = normalize(getCookie9(), /* radial= */ true);
  public static final RoundedPolygon COOKIE_12 = normalize(getCookie12(), /* radial= */ true);
  public static final RoundedPolygon GHOSTISH = normalize(getGhostish(), /* radial= */ true);
  public static final RoundedPolygon CLOVER_4 = normalize(getClover4(), /* radial= */ true);
  public static final RoundedPolygon CLOVER_8 = normalize(getClover8(), /* radial= */ true);
  public static final RoundedPolygon BURST = normalize(getBurst(), /* radial= */ true);
  public static final RoundedPolygon SOFT_BURST = normalize(getSoftBurst(), /* radial= */ true);
  public static final RoundedPolygon BOOM = normalize(getBoom(), /* radial= */ true);
  public static final RoundedPolygon SOFT_BOOM = normalize(getSoftBoom(), /* radial= */ true);
  public static final RoundedPolygon FLOWER = normalize(getFlower(), /* radial= */ true);
  public static final RoundedPolygon PUFFY = normalize(getPuffy(), /* radial= */ true);
  public static final RoundedPolygon PUFFY_DIAMOND =
      normalize(getPuffyDiamond(), /* radial= */ true);
  public static final RoundedPolygon PIXEL_CIRCLE = normalize(getPixelCircle(), /* radial= */ true);
  public static final RoundedPolygon PIXEL_TRIANGLE =
      normalize(getPixelTriangle(), /* radial= */ true);
  public static final RoundedPolygon BUN = normalize(getBun(), /* radial= */ true);
  public static final RoundedPolygon HEART = normalize(getHeart(), /* radial= */ true);

  @NonNull
  private static RoundedPolygon getCircle() {
    return ShapesKt.circle(RoundedPolygon.Companion, /* numVertices= */ 10);
  }

  @NonNull
  private static RoundedPolygon getSquare() {
    return ShapesKt.rectangle(
        RoundedPolygon.Companion,
        /* width= */ 1f,
        /* height= */ 1f,
        /* rounding= */ CORNER_ROUND_30,
        /* perVertexRounding= */ null,
        /* centerX= */ 0f,
        /* centerY= */ 0f);
  }

  @NonNull
  private static RoundedPolygon getSlantedSquare() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(
        new VertexAndRounding(new PointF(0.926f, 0.970f), new CornerRounding(0.189f, 0.811f)));
    points.add(
        new VertexAndRounding(new PointF(-0.021f, 0.967f), new CornerRounding(0.187f, 0.057f)));
    return customPolygon(points, 2, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getArch() {
    return Shapes_androidKt.transformed(
        RoundedPolygonKt.RoundedPolygon(
            /* numVertices= */ 4,
            /* radius= */ 1f,
            /* centerX= */ 0f,
            /* centerY= */ 0f,
            /* rounding= */ CornerRounding.Unrounded,
            /* perVertexRounding= */ Arrays.asList(
                CORNER_ROUND_100, CORNER_ROUND_100, CORNER_ROUND_20, CORNER_ROUND_20)),
        createRotationMatrix(-135));
  }

  @NonNull
  private static RoundedPolygon getFan() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(1f, 1f), new CornerRounding(0.148f, 0.417f)));
    points.add(new VertexAndRounding(new PointF(0f, 1f), new CornerRounding(0.151f, 0f)));
    points.add(new VertexAndRounding(new PointF(0f, 0f), new CornerRounding(0.148f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.978f, 0.020f), new CornerRounding(0.803f, 0f)));
    return customPolygon(points, 1, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getArrow() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 0.892f), new CornerRounding(0.313f, 0f)));
    points.add(new VertexAndRounding(new PointF(-0.216f, 1.050f), new CornerRounding(0.207f, 0f)));
    points.add(
        new VertexAndRounding(new PointF(0.499f, -0.160f), new CornerRounding(0.215f, 1.000f)));
    points.add(new VertexAndRounding(new PointF(1.225f, 1.060f), new CornerRounding(0.211f, 0f)));
    return customPolygon(points, 1, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getSemiCircle() {
    return ShapesKt.rectangle(
        RoundedPolygon.Companion,
        /* width= */ 1.6f,
        /* height= */ 1f,
        /* rounding= */ CornerRounding.Unrounded,
        /* perVertexRounding= */ Arrays.asList(
            CORNER_ROUND_20, CORNER_ROUND_20, CORNER_ROUND_100, CORNER_ROUND_100),
        /* centerX= */ 0f,
        /* centerY= */ 0f);
  }

  @NonNull
  private static RoundedPolygon getOval() {
    return Shapes_androidKt.transformed(
        ShapesKt.circle(RoundedPolygon.Companion),
        createScaleMatrix(/* scaleX= */ 1f, /* scaleY= */ .64f));
  }

  @NonNull
  private static RoundedPolygon getOval(float rotateDegrees) {
    return Shapes_androidKt.transformed(getOval(), createRotationMatrix(rotateDegrees));
  }

  @NonNull
  private static RoundedPolygon getPill() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.961f, 0.039f), new CornerRounding(0.426f, 0f)));
    points.add(new VertexAndRounding(new PointF(1.001f, 0.428f)));
    points.add(new VertexAndRounding(new PointF(1.000f, 0.609f), CORNER_ROUND_100));
    return customPolygon(points, 2, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getTriangle() {
    return RoundedPolygonKt.RoundedPolygon(
        /* numVertices= */ 3,
        /* radius= */ 1f,
        /* centerX= */ 0f,
        /* centerY= */ 0f,
        /* rounding= */ CORNER_ROUND_20);
  }

  @NonNull
  private static RoundedPolygon getTriangle(float rotateDegrees) {
    return Shapes_androidKt.transformed(getTriangle(), createRotationMatrix(rotateDegrees));
  }

  @NonNull
  private static RoundedPolygon getDiamond() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(
        new VertexAndRounding(new PointF(0.500f, 1.096f), new CornerRounding(0.151f, 0.524f)));
    points.add(new VertexAndRounding(new PointF(0.040f, 0.500f), new CornerRounding(0.159f, 0f)));
    return customPolygon(points, 2, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getClamShell() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.171f, 0.841f), new CornerRounding(0.159f, 0f)));
    points.add(new VertexAndRounding(new PointF(-0.020f, 0.500f), new CornerRounding(0.140f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.170f, 0.159f), new CornerRounding(0.159f, 0f)));
    return customPolygon(points, 2, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getPentagon() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, -0.009f), new CornerRounding(0.172f, 0f)));
    return customPolygon(points, 5, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getGem() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(
        new VertexAndRounding(new PointF(0.499f, 1.023f), new CornerRounding(0.241f, 0.778f)));
    points.add(new VertexAndRounding(new PointF(-0.005f, 0.792f), new CornerRounding(0.208f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.073f, 0.258f), new CornerRounding(0.228f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.433f, -0.000f), new CornerRounding(0.491f, 0f)));
    return customPolygon(points, 1, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getGem(float rotateDegrees) {
    return Shapes_androidKt.transformed(getGem(), createRotationMatrix(rotateDegrees));
  }

  @NonNull
  private static RoundedPolygon getSunny() {
    return ShapesKt.star(
        RoundedPolygon.Companion,
        /* numVerticesPerRadius= */ 8,
        /* radius= */ 1f,
        /* innerRadius= */ 0.8f,
        /* rounding= */ CORNER_ROUND_15);
  }

  @NonNull
  private static RoundedPolygon getVerySunny() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 1.080f), new CornerRounding(0.085f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.358f, 0.843f), new CornerRounding(0.085f, 0f)));
    return customPolygon(points, 8, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getCookie4() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(1.237f, 1.236f), new CornerRounding(0.258f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.500f, 0.918f), new CornerRounding(0.233f, 0f)));
    return customPolygon(points, 4, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getCookie6() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.723f, 0.884f), new CornerRounding(0.394f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.500f, 1.099f), new CornerRounding(0.398f, 0f)));
    return customPolygon(points, 6, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getCookie7() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 7,
            /* radius= */ 1f,
            /* innerRadius= */ 0.75f,
            /* rounding= */ CORNER_ROUND_50),
        createRotationMatrix(-90f));
  }

  @NonNull
  private static RoundedPolygon getCookie9() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 9,
            /* radius= */ 1f,
            /* innerRadius= */ 0.8f,
            /* rounding= */ CORNER_ROUND_50),
        createRotationMatrix(-90f));
  }

  @NonNull
  private static RoundedPolygon getCookie12() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 12,
            /* radius= */ 1f,
            /* innerRadius= */ 0.8f,
            /* rounding= */ CORNER_ROUND_50),
        createRotationMatrix(-90f));
  }

  @NonNull
  private static RoundedPolygon getGhostish() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 0f), CORNER_ROUND_100));
    points.add(new VertexAndRounding(new PointF(1f, 0f), CORNER_ROUND_100));
    points.add(new VertexAndRounding(new PointF(1f, 1.140f), new CornerRounding(0.254f, 0.106f)));
    points.add(new VertexAndRounding(new PointF(0.575f, 0.906f), new CornerRounding(0.253f, 0f)));
    return customPolygon(points, 1, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getClover4() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 0.074f)));
    points.add(new VertexAndRounding(new PointF(0.725f, -0.099f), new CornerRounding(0.476f, 0f)));
    return customPolygon(points, 4, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getClover8() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 0.036f)));
    points.add(new VertexAndRounding(new PointF(0.758f, -0.101f), new CornerRounding(0.209f, 0f)));
    return customPolygon(points, 8, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getBurst() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, -0.006f), new CornerRounding(0.006f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.592f, 0.158f), new CornerRounding(0.006f, 0f)));
    return customPolygon(points, 12, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getSoftBurst() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.193f, 0.277f), new CornerRounding(0.053f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.176f, 0.055f), new CornerRounding(0.053f, 0f)));
    return customPolygon(points, 10, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getBoom() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.457f, 0.296f), new CornerRounding(0.007f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.500f, -0.051f), new CornerRounding(0.007f, 0f)));
    return customPolygon(points, 15, 0.5f, 0.5f, false);
  }

  @NonNull
  private static RoundedPolygon getSoftBoom() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.733f, 0.454f)));
    points.add(new VertexAndRounding(new PointF(0.839f, 0.437f), new CornerRounding(0.532f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.949f, 0.449f), new CornerRounding(0.439f, 1f)));
    points.add(new VertexAndRounding(new PointF(0.998f, 0.478f), new CornerRounding(0.174f, 0f)));
    return customPolygon(points, 16, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getFlower() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.370f, 0.187f)));
    points.add(new VertexAndRounding(new PointF(0.416f, 0.049f), new CornerRounding(0.381f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.479f, 0f), new CornerRounding(0.095f, 0f)));
    return customPolygon(points, 8, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getPuffy() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 0.053f)));
    points.add(new VertexAndRounding(new PointF(0.545f, -0.040f), new CornerRounding(0.405f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.670f, -0.035f), new CornerRounding(0.426f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.717f, 0.066f), new CornerRounding(0.574f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.722f, 0.128f)));
    points.add(new VertexAndRounding(new PointF(0.777f, 0.002f), new CornerRounding(0.360f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.914f, 0.149f), new CornerRounding(0.660f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.926f, 0.289f), new CornerRounding(0.660f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.881f, 0.346f)));
    points.add(new VertexAndRounding(new PointF(0.940f, 0.344f), new CornerRounding(0.126f, 0f)));
    points.add(new VertexAndRounding(new PointF(1.003f, 0.437f), new CornerRounding(0.255f, 0f)));
    return Shapes_androidKt.transformed(
        customPolygon(points, 2, 0.5f, 0.5f, true), createScaleMatrix(1f, 0.742f));
  }

  @NonNull
  private static RoundedPolygon getPuffyDiamond() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.870f, 0.130f), new CornerRounding(0.146f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.818f, 0.357f)));
    points.add(new VertexAndRounding(new PointF(1.000f, 0.332f), new CornerRounding(0.853f, 0f)));
    return customPolygon(points, 4, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getPixelCircle() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 0.000f)));
    points.add(new VertexAndRounding(new PointF(0.704f, 0.000f)));
    points.add(new VertexAndRounding(new PointF(0.704f, 0.065f)));
    points.add(new VertexAndRounding(new PointF(0.843f, 0.065f)));
    points.add(new VertexAndRounding(new PointF(0.843f, 0.148f)));
    points.add(new VertexAndRounding(new PointF(0.926f, 0.148f)));
    points.add(new VertexAndRounding(new PointF(0.926f, 0.296f)));
    points.add(new VertexAndRounding(new PointF(1.000f, 0.296f)));
    return customPolygon(points, 2, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getPixelTriangle() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.110f, 0.500f)));
    points.add(new VertexAndRounding(new PointF(0.113f, 0.000f)));
    points.add(new VertexAndRounding(new PointF(0.287f, 0.000f)));
    points.add(new VertexAndRounding(new PointF(0.287f, 0.087f)));
    points.add(new VertexAndRounding(new PointF(0.421f, 0.087f)));
    points.add(new VertexAndRounding(new PointF(0.421f, 0.170f)));
    points.add(new VertexAndRounding(new PointF(0.560f, 0.170f)));
    points.add(new VertexAndRounding(new PointF(0.560f, 0.265f)));
    points.add(new VertexAndRounding(new PointF(0.674f, 0.265f)));
    points.add(new VertexAndRounding(new PointF(0.675f, 0.344f)));
    points.add(new VertexAndRounding(new PointF(0.789f, 0.344f)));
    points.add(new VertexAndRounding(new PointF(0.789f, 0.439f)));
    points.add(new VertexAndRounding(new PointF(0.888f, 0.439f)));
    return customPolygon(points, 1, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getBun() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.796f, 0.500f)));
    points.add(new VertexAndRounding(new PointF(0.853f, 0.518f), CORNER_ROUND_100));
    points.add(new VertexAndRounding(new PointF(0.992f, 0.631f), CORNER_ROUND_100));
    points.add(new VertexAndRounding(new PointF(0.968f, 1.000f), CORNER_ROUND_100));
    return customPolygon(points, 2, 0.5f, 0.5f, true);
  }

  @NonNull
  private static RoundedPolygon getHeart() {
    List<VertexAndRounding> points = new ArrayList<>();
    points.add(new VertexAndRounding(new PointF(0.500f, 0.268f), new CornerRounding(0.016f, 0f)));
    points.add(new VertexAndRounding(new PointF(0.792f, -0.066f), new CornerRounding(0.958f, 0f)));
    points.add(new VertexAndRounding(new PointF(1.064f, 0.276f), CORNER_ROUND_100));
    points.add(new VertexAndRounding(new PointF(0.501f, 0.946f), new CornerRounding(0.129f, 0f)));
    return customPolygon(points, 1, 0.5f, 0.5f, true);
  }

  private static void repeatAroundCenter(
      @NonNull List<VertexAndRounding> template,
      @NonNull List<VertexAndRounding> outList,
      int repeatCount,
      float centerX,
      float centerY,
      boolean mirroring) {
    outList.clear();
    toRadial(template, centerX, centerY);
    float spanPerRepeat = (float) (2 * PI / repeatCount);
    if (mirroring) {
      // Template will be repeated in the original order then in the reverse order.
      int mirroredRepeatCount = repeatCount * 2;
      spanPerRepeat /= 2;
      for (int i = 0; i < mirroredRepeatCount; i++) {
        for (int j = 0; j < template.size(); j++) {
          boolean reverse = i % 2 != 0;
          int indexInTemplate = reverse ? template.size() - 1 - j : j;
          VertexAndRounding templatePoint = template.get(indexInTemplate);
          if (indexInTemplate > 0 || !reverse) {
            float angle =
                spanPerRepeat * i
                    + (reverse
                        ? spanPerRepeat - templatePoint.vertex.x + 2 * template.get(0).vertex.x
                        : templatePoint.vertex.x);
            PointF outVertex = new PointF(angle, templatePoint.vertex.y);
            outList.add(new VertexAndRounding(outVertex, templatePoint.rounding));
          }
        }
      }
    } else {
      for (int i = 0; i < repeatCount; i++) {
        for (VertexAndRounding templatePoint : template) {
          float angle = spanPerRepeat * i + templatePoint.vertex.x;
          PointF outVertex = new PointF(angle, templatePoint.vertex.y);
          outList.add(new VertexAndRounding(outVertex, templatePoint.rounding));
        }
      }
    }
    toCartesian(outList, centerX, centerY);
  }

  @NonNull
  private static RoundedPolygon customPolygon(
      @NonNull List<VertexAndRounding> template,
      int repeat,
      float centerX,
      float centerY,
      boolean mirroring) {
    List<VertexAndRounding> vertexAndRoundings = new ArrayList<>();
    repeatAroundCenter(template, vertexAndRoundings, repeat, centerX, centerY, mirroring);

    float[] verticesXy = toVerticesXyArray(vertexAndRoundings);
    List<CornerRounding> roundings = toRoundingsList(vertexAndRoundings);
    return RoundedPolygonKt.RoundedPolygon(
        verticesXy, CornerRounding.Unrounded, roundings, centerX, centerY);
  }

  private MaterialShapes() {}

  // ============== Utility methods. ==================

  /**
   * Returns a {@link ShapeDrawable} with the shape's path.
   *
   * <p>The shape is always assumed to fit in (0, 0) to (1, 1) square.
   *
   * @param shape A {@link RoundedPolygon} object to be used in the drawable.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static ShapeDrawable createShapeDrawable(@NonNull RoundedPolygon shape) {
    PathShape pathShape = new PathShape(Shapes_androidKt.toPath(shape), 1, 1);
    return new ShapeDrawable(pathShape);
  }

  /**
   * Creates a new {@link RoundedPolygon}, moving and resizing this one, so it's completely inside
   * the destination bounds.
   *
   * <p>If {@code radial} is true, the shape will be scaled to fit in the biggest circle centered in
   * the destination bounds. This is useful when the shape is animated to rotate around its center.
   * Otherwise, the shape will be scaled to fit in the destination bounds. With either option, the
   * shape's original center will be aligned with the destination bounds center.
   *
   * @param shape The original {@link RoundedPolygon}.
   * @param radial Whether to transform the shape to fit in the biggest circle centered in the
   *     destination bounds.
   * @param dstBounds The destination bounds to fit.
   * @return A new {@link RoundedPolygon} that fits in the destination bounds.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static RoundedPolygon normalize(
      @NonNull RoundedPolygon shape, boolean radial, @NonNull RectF dstBounds) {
    float[] srcBoundsArray = new float[4];
    if (radial) {
      // This calculates the axis-aligned bounds of the shape and returns that rectangle. It
      // determines the max dimension of the shape (by calculating the distance from its center to
      // the start and midpoint of each curve) and returns a square which can be used to hold the
      // object in any rotation.
      shape.calculateMaxBounds(srcBoundsArray);
    } else {
      // This calculates the bounds of the shape without rotating the shape.
      shape.calculateBounds(srcBoundsArray);
    }
    RectF srcBounds =
        new RectF(srcBoundsArray[0], srcBoundsArray[1], srcBoundsArray[2], srcBoundsArray[3]);
    float scale =
        min(dstBounds.width() / srcBounds.width(), dstBounds.height() / srcBounds.height());
    // Scales the shape with pivot point at its original center then moves it to align its original
    // center with the destination bounds center.
    Matrix transform = createScaleMatrix(scale, scale);
    transform.preTranslate(-srcBounds.centerX(), -srcBounds.centerY());
    transform.postTranslate(dstBounds.centerX(), dstBounds.centerY());
    return Shapes_androidKt.transformed(shape, transform);
  }

  /**
   * Creates a new {@link RoundedPolygon}, moving and resizing this one, so it's completely inside
   * (0, 0) - (1, 1) square.
   *
   * <p>If {@code radial} is true, the shape will be scaled to fit in the circle centered at (0.5,
   * 0.5) with a radius of 0.5. This is useful when the shape is animated to rotate around its
   * center. Otherwise, the shape will be scaled to fit in the (0, 0) - (1, 1) square. With either
   * option, the shape center will be (0.5, 0.5).
   *
   * @param shape The original {@link RoundedPolygon}.
   * @param radial Whether to transform the shape to fit in the circle centered at (0.5, 0.5) with a
   *     radius of 0.5.
   * @return A new {@link RoundedPolygon} that fits in (0, 0) - (1, 1) square.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static RoundedPolygon normalize(@NonNull RoundedPolygon shape, boolean radial) {
    return normalize(shape, radial, new RectF(0, 0, 1, 1));
  }

  /**
   * Returns a {@link Matrix} with the input scales.
   *
   * @param scaleX Scale in X axis.
   * @param scaleY Scale in Y axis
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  static Matrix createScaleMatrix(float scaleX, float scaleY) {
    Matrix matrix = new Matrix();
    matrix.setScale(scaleX, scaleY);
    return matrix;
  }

  /**
   * Returns a {@link Matrix} with the input rotation in degrees.
   *
   * @param degrees The rotation in degrees.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  static Matrix createRotationMatrix(float degrees) {
    Matrix matrix = new Matrix();
    matrix.setRotate(degrees);
    return matrix;
  }

  /**
   * Returns a {@link Matrix} with the input skews.
   *
   * @param kx The skew in X axis.
   * @param ky The skew in Y axis.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  static Matrix createSkewMatrix(float kx, float ky) {
    Matrix matrix = new Matrix();
    matrix.setSkew(kx, ky);
    return matrix;
  }

  private static void toRadial(
      @NonNull List<VertexAndRounding> vertexAndRoundings, float centerX, float centerY) {
    for (VertexAndRounding vertexAndRounding : vertexAndRoundings) {
      vertexAndRounding.toRadial(centerX, centerY);
    }
  }

  private static void toCartesian(
      @NonNull List<VertexAndRounding> vertexAndRoundings, float centerX, float centerY) {
    for (VertexAndRounding vertexAndRounding : vertexAndRoundings) {
      vertexAndRounding.toCartesian(centerX, centerY);
    }
  }

  @NonNull
  private static float[] toVerticesXyArray(@NonNull List<VertexAndRounding> vertexAndRoundings) {
    float[] verticesXy = new float[vertexAndRoundings.size() * 2];
    for (int i = 0; i < vertexAndRoundings.size(); i++) {
      verticesXy[2 * i] = vertexAndRoundings.get(i).vertex.x;
      verticesXy[2 * i + 1] = vertexAndRoundings.get(i).vertex.y;
    }
    return verticesXy;
  }

  @NonNull
  private static List<CornerRounding> toRoundingsList(
      @NonNull List<VertexAndRounding> vertexAndRoundings) {
    List<CornerRounding> roundings = new ArrayList<>();
    for (int i = 0; i < vertexAndRoundings.size(); i++) {
      roundings.add(vertexAndRoundings.get(i).rounding);
    }
    return roundings;
  }

  static class VertexAndRounding {
    private PointF vertex;
    private CornerRounding rounding;

    private VertexAndRounding(@NonNull PointF vertex) {
      this(vertex, CornerRounding.Unrounded);
    }

    private VertexAndRounding(@NonNull PointF vertex, @NonNull CornerRounding rounding) {
      this.vertex = vertex;
      this.rounding = rounding;
    }

    private void toRadial(float centerX, float centerY) {
      vertex.offset(-centerX, -centerY);
      float angle = (float) Math.atan2(vertex.y, vertex.x);
      float distance = (float) Math.hypot(vertex.x, vertex.y);
      vertex.x = angle;
      vertex.y = distance;
    }

    private void toCartesian(float centerX, float centerY) {
      float x = (float) (vertex.y * Math.cos(vertex.x) + centerX);
      float y = (float) (vertex.y * Math.sin(vertex.x) + centerY);
      vertex.x = x;
      vertex.y = y;
    }
  }
}
