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
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.graphics.shapes.CornerRounding;
import androidx.graphics.shapes.RoundedPolygon;
import androidx.graphics.shapes.RoundedPolygonKt;
import androidx.graphics.shapes.ShapesKt;
import androidx.graphics.shapes.Shapes_androidKt;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Contract;

/**
 * A utility class providing static methods for creating Material endorsed shapes using the {@code
 * androidx.graphics.shapes} library.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialShapes {
  // Cache various roundings for use below
  private static final CornerRounding CORNER_ROUND_10 =
      new CornerRounding(/* radius= */ .1f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_15 =
      new CornerRounding(/* radius= */ .15f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_20 =
      new CornerRounding(/* radius= */ .2f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_30 =
      new CornerRounding(/* radius= */ .3f, /* smoothing= */ 0f);
  private static final CornerRounding CORNER_ROUND_40 =
      new CornerRounding(/* radius= */ .4f, /* smoothing= */ 0f);
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
  public static final RoundedPolygon FAN =
      normalize(getFan(/* rotateDegrees= */ -45f), /* radial= */ true);

  /** An arrow shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon ARROW = normalize(getArrow(), /* radial= */ true);

  /** A semi-circle shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon SEMI_CIRCLE = normalize(getSemiCircle(), /* radial= */ true);

  /** An oval shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon OVAL =
      normalize(getOval(/* rotateDegrees= */ -45f), /* radial= */ true);

  /** A pill shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon PILL =
      normalize(getPill(/* rotateDegrees= */ -45f), /* radial= */ true);

  /** A triangle shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon TRIANGLE =
      normalize(getTriangle(/* rotateDegrees= */ -90f), /* radial= */ true);

  /** A diamond shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon DIAMOND = normalize(getDiamond(), /* radial= */ true);

  /** A clam-shell shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon CLAM_SHELL = normalize(getClamShell(), /* radial= */ true);

  /** A pentagon shape {@link RoundedPolygon} fitting in a unit circle. */
  public static final RoundedPolygon PENTAGON =
      normalize(getPentagon(/* rotateDegrees= */ -18f), /* radial= */ true);

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
    return Shapes_androidKt.transformed(getSquare(), createSkewMatrix(-.1f, 0f));
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
    return RoundedPolygonKt.RoundedPolygon(
        /* numVertices= */ 4,
        /* radius= */ 1f,
        /* centerX= */ 0f,
        /* centerY= */ 0f,
        CornerRounding.Unrounded,
        /* perVertexRounding= */ Arrays.asList(
            CORNER_ROUND_100, CORNER_ROUND_20, CORNER_ROUND_20, CORNER_ROUND_20));
  }

  @NonNull
  private static RoundedPolygon getFan(float rotateDegrees) {
    return Shapes_androidKt.transformed(getFan(), createRotationMatrix(rotateDegrees));
  }

  @NonNull
  private static RoundedPolygon getArrow() {
    return triangleChip(
        /* innerRadius= */ .3375f,
        /* cornerRounding= */ new CornerRounding(/* radius= */ .25f, /* smoothing= */ .48f));
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
        createScaleMatrix(/* scaleX= */ 1f, /* scaleY= */ .7f));
  }

  @NonNull
  private static RoundedPolygon getOval(float rotateDegrees) {
    return Shapes_androidKt.transformed(getOval(), createRotationMatrix(rotateDegrees));
  }

  @NonNull
  private static RoundedPolygon getPill() {
    return ShapesKt.pill(RoundedPolygon.Companion, /* width= */ 1.25f, /* height= */ 1f);
  }

  @NonNull
  private static RoundedPolygon getPill(float rotateDegrees) {
    return Shapes_androidKt.transformed(getPill(), createRotationMatrix(rotateDegrees));
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
    return Shapes_androidKt.transformed(
        RoundedPolygonKt.RoundedPolygon(
            /* numVertices= */ 4,
            /* radius= */ 1f,
            /* centerX= */ 0f,
            /* centerY= */ 0f,
            /* rounding= */ CORNER_ROUND_30),
        createScaleMatrix(/* scaleX= */ 1f, /* scaleY= */ 1.2f));
  }

  @NonNull
  private static RoundedPolygon getClamShell() {
    float cornerInset = .6f;
    float edgeInset = .4f;
    float height = .7f;
    float[] hexPoints =
        new float[] {
          1f,
          0f,
          cornerInset,
          height,
          edgeInset,
          height,
          -edgeInset,
          height,
          -cornerInset,
          height,
          -1f,
          0f,
          -cornerInset,
          -height,
          -edgeInset,
          -height,
          edgeInset,
          -height,
          cornerInset,
          -height
        };
    List<CornerRounding> perVertexRounding =
        Arrays.asList(
            CORNER_ROUND_30,
            CORNER_ROUND_30,
            CornerRounding.Unrounded,
            CornerRounding.Unrounded,
            CORNER_ROUND_30,
            CORNER_ROUND_30,
            CORNER_ROUND_30,
            CornerRounding.Unrounded,
            CornerRounding.Unrounded,
            CORNER_ROUND_30);
    return RoundedPolygonKt.RoundedPolygon(
        hexPoints, /* rounding= */ CornerRounding.Unrounded, perVertexRounding);
  }

  @NonNull
  private static RoundedPolygon getPentagon() {
    return RoundedPolygonKt.RoundedPolygon(
        /* numVertices= */ 5,
        /* radius= */ 1f,
        /* centerX= */ 0f,
        /* centerY= */ 0f,
        /* rounding= */ CORNER_ROUND_30);
  }

  @NonNull
  private static RoundedPolygon getPentagon(float rotateDegrees) {
    return Shapes_androidKt.transformed(getPentagon(), createRotationMatrix(rotateDegrees));
  }

  @NonNull
  private static RoundedPolygon getGem() {
    // This shape looks like a rounded hexagon with a small offset in the second and last vertices.
    int numVertices = 6;
    float radius = 1f;
    float[] points = new float[numVertices * 2];
    for (int i = 0; i < numVertices; i++) {
      PointF vertex = radialToCartesian(new PointF(radius, (float) (2 * PI / numVertices * i)));
      points[i * 2] = vertex.x;
      points[i * 2 + 1] = vertex.y;
    }
    // Offsets the second vertex (in the first quadrant) by (-0.1, -0.1) towards the center.
    points[2] -= .1f;
    points[3] -= .1f;
    // Offsets the last vertex (in the fourth quadrant) by (-0.1, 0.1) towards the center.
    points[10] -= .1f;
    points[11] += .1f;
    return RoundedPolygonKt.RoundedPolygon(points, CORNER_ROUND_40);
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
    return ShapesKt.star(
        RoundedPolygon.Companion,
        /* numVerticesPerRadius= */ 8,
        /* radius= */ 1f,
        /* innerRadius= */ 0.65f,
        /* rounding= */ CORNER_ROUND_10);
  }

  @NonNull
  private static RoundedPolygon getCookie4() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 4,
            /* radius= */ 1f,
            /* innerRadius= */ 0.5f,
            /* rounding= */ CORNER_ROUND_30),
        createRotationMatrix(-45f));
  }

  @NonNull
  private static RoundedPolygon getCookie6() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 6,
            /* radius= */ 1f,
            /* innerRadius= */ 0.75f,
            /* rounding= */ CORNER_ROUND_30),
        createRotationMatrix(-90f));
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
    float inset = .46f;
    float h = 1.2f;
    float[] points = new float[] {-1f, -h, 1f, -h, 1f, h, 0f, inset, -1f, h};
    List<CornerRounding> perVertexRounding =
        Arrays.asList(
            CORNER_ROUND_100, CORNER_ROUND_100, CORNER_ROUND_50, CORNER_ROUND_100, CORNER_ROUND_50);
    return RoundedPolygonKt.RoundedPolygon(
        points, /* rounding= */ CornerRounding.Unrounded, perVertexRounding);
  }

  @NonNull
  private static RoundedPolygon getClover4() {
    // No inner rounding.
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 4,
            /* radius= */ 1f,
            /* innerRadius= */ 0.2f,
            /* rounding= */ CORNER_ROUND_40,
            /* innerRounding= */ CornerRounding.Unrounded),
        createRotationMatrix(45f));
  }

  @NonNull
  private static RoundedPolygon getClover8() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 8,
            /* radius= */ 1f,
            /* innerRadius= */ 0.65f,
            /* rounding= */ CORNER_ROUND_30,
            /* innerRounding= */ CornerRounding.Unrounded),
        createRotationMatrix(360 / 16f));
  }

  @NonNull
  private static RoundedPolygon getBurst() {
    return ShapesKt.star(
        RoundedPolygon.Companion,
        /* numVerticesPerRadius= */ 12,
        /* radius= */ 1f,
        /* innerRadius= */ 0.7f);
  }

  @NonNull
  private static RoundedPolygon getSoftBurst() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 10,
            /* radius= */ 1f,
            /* innerRadius= */ 0.65f,
            /* rounding= */ CORNER_ROUND_10,
            /* innerRounding= */ CORNER_ROUND_10),
        createRotationMatrix(360 / 20f));
  }

  @NonNull
  private static RoundedPolygon getBoom() {
    return Shapes_androidKt.transformed(
        ShapesKt.star(
            RoundedPolygon.Companion,
            /* numVerticesPerRadius= */ 15,
            /* radius= */ 1f,
            /* innerRadius= */ 0.42f),
        createRotationMatrix(360 / 60f));
  }

  @NonNull
  private static RoundedPolygon getSoftBoom() {
    float[] pointsXyTemplate =
        new float[] {
          0.456f, 0.224f,
          0.460f, 0.170f,
          0.500f, 0.100f,
          0.540f, 0.170f,
          0.544f, 0.224f,
          0.538f, 0.308f
        };
    CornerRounding[] roundingsTemplate =
        new CornerRounding[] {
          new CornerRounding(/* radius= */ 0.020f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.143f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.025f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.143f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.190f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0f, /* smoothing= */ 0f)
        };

    float[] pointsXy = new float[pointsXyTemplate.length * 16];
    CornerRounding[] roundings = new CornerRounding[roundingsTemplate.length * 16];

    repeatAroundCenter(
        pointsXyTemplate,
        pointsXy,
        roundingsTemplate,
        roundings,
        16,
        0.5f,
        0.5f,
        -360f / 32,
        false);

    return RoundedPolygonKt.RoundedPolygon(
        pointsXy,
        /* rounding= */ CornerRounding.Unrounded,
        Arrays.asList(roundings),
        /* centerX= */ 0.5f,
        /* centerY= */ 0.5f);
  }

  @NonNull
  private static RoundedPolygon getFlower() {
    return ShapesKt.star(
        RoundedPolygon.Companion,
        /* numVerticesPerRadius= */ 8,
        /* radius= */ 1f,
        /* innerRadius= */ 0.588f,
        /* rounding= */ new CornerRounding(/* radius= */ 0.12f, /* smoothing= */ 0.48f),
        /* innerRounding= */ CornerRounding.Unrounded);
  }

  @NonNull
  private static RoundedPolygon getPuffy() {
    float[] pointsXyTemplate =
        new float[] {
          0.501f, 0.260f,
          0.526f, 0.188f,
          0.676f, 0.226f,
          0.660f, 0.300f,
          0.734f, 0.230f,
          0.838f, 0.350f,
          0.782f, 0.418f,
          0.874f, 0.414f
        };
    CornerRounding[] roundingsTemplate =
        new CornerRounding[] {
          CornerRounding.Unrounded,
          new CornerRounding(/* radius= */ 0.095f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.095f, /* smoothing= */ 0f),
          CornerRounding.Unrounded,
          new CornerRounding(/* radius= */ 0.095f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.095f, /* smoothing= */ 0f),
          CornerRounding.Unrounded,
          new CornerRounding(/* radius= */ 0.095f, /* smoothing= */ 0f)
        };

    float[] pointsXy = new float[pointsXyTemplate.length * 4];
    CornerRounding[] roundings = new CornerRounding[roundingsTemplate.length * 4];

    repeatAroundCenter(
        pointsXyTemplate, pointsXy, roundingsTemplate, roundings, 4, 0.5f, 0.5f, 0f, true);

    return RoundedPolygonKt.RoundedPolygon(
        pointsXy,
        /* rounding= */ CornerRounding.Unrounded,
        Arrays.asList(roundings),
        /* centerX= */ 0.5f,
        /* centerY= */ 0.5f);
  }

  @NonNull
  private static RoundedPolygon getPuffyDiamond() {
    float[] pointsXyTemplate =
        new float[] {
          0.390f, 0.260f,
          0.390f, 0.130f,
          0.610f, 0.130f,
          0.610f, 0.260f,
          0.740f, 0.260f
        };
    CornerRounding[] roundingsTemplate =
        new CornerRounding[] {
          new CornerRounding(/* radius= */ 0.000f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.104f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.104f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.000f, /* smoothing= */ 0f),
          new CornerRounding(/* radius= */ 0.104f, /* smoothing= */ 0f)
        };

    float[] pointsXy = new float[pointsXyTemplate.length * 4];
    CornerRounding[] roundings = new CornerRounding[roundingsTemplate.length * 4];

    repeatAroundCenter(
        pointsXyTemplate, pointsXy, roundingsTemplate, roundings, 4, 0.5f, 0.5f, -24.63f, false);

    return RoundedPolygonKt.RoundedPolygon(
        pointsXy,
        /* rounding= */ CornerRounding.Unrounded,
        Arrays.asList(roundings),
        /* centerX= */ 0.5f,
        /* centerY= */ 0.5f);
  }

  @NonNull
  private static RoundedPolygon getPixelCircle() {
    PointF start = new PointF();
    PointF[] offsets =
        new PointF[] {
          new PointF(0.4f, -1f),
          new PointF(0f, 0.14f),
          new PointF(0.28f, 0f),
          new PointF(0f, 0.16f),
          new PointF(0.16f, 0f),
          new PointF(0f, 0.3f),
          new PointF(0.16f, 0f)
        };
    float[] pointsXyTemplate = new float[offsets.length * 2];
    for (int i = 0; i < offsets.length; i++) {
      start.offset(offsets[i].x, offsets[i].y);
      pointsXyTemplate[i * 2] = start.x;
      pointsXyTemplate[i * 2 + 1] = start.y;
    }
    float[] pointsXy = new float[pointsXyTemplate.length * 4];
    repeatAroundCenter(pointsXyTemplate, pointsXy, null, null, 4, 0f, 0f, 0f, true);
    return RoundedPolygonKt.RoundedPolygon(pointsXy, /* rounding= */ CornerRounding.Unrounded);
  }

  @NonNull
  private static RoundedPolygon getPixelTriangle() {
    PointF start = new PointF();
    PointF[] offsets =
        new PointF[] {
          new PointF(),
          new PointF(56f, 0f),
          new PointF(0f, 28f),
          new PointF(44f, 0f),
          new PointF(0f, 26f),
          new PointF(44f, 0f),
          new PointF(0f, 32f),
          new PointF(38f, 0f),
          new PointF(0f, 26f),
          new PointF(38f, 0f),
          new PointF(0f, 32f),
          new PointF(32f, 0f)
        };
    float[] pointsXyTemplate = new float[offsets.length * 2];
    for (int i = 0; i < offsets.length; i++) {
      start.offset(offsets[i].x, offsets[i].y);
      pointsXyTemplate[i * 2] = start.x;
      pointsXyTemplate[i * 2 + 1] = start.y;
    }
    // Moves start to the center of the shape.
    start.offset(-start.x / 2f, 19f);
    float[] pointsXy = new float[pointsXyTemplate.length * 2];
    repeatAroundCenter(pointsXyTemplate, pointsXy, null, null, 2, start.x, start.y, -90, true);
    return RoundedPolygonKt.RoundedPolygon(
        pointsXy,
        /* rounding= */ CornerRounding.Unrounded,
        /* perVertexRounding= */ null,
        /* centerX= */ start.x,
        /* centerY= */ start.y);
  }

  @NonNull
  private static RoundedPolygon getBun() {
    float inset = .4f;
    float[] sandwichPoints =
        new float[] {
          1f, 1f, inset, 1f, -inset, 1f, -1f, 1f, -1f, 0f, -inset, 0f, -1f, 0f, -1f, -1f, -inset,
          -1f, inset, -1f, 1f, -1f, 1f, 0f, inset, 0f, 1f, 0f
        };
    CornerRounding[] roundings =
        new CornerRounding[] {
          CORNER_ROUND_100,
          CornerRounding.Unrounded,
          CornerRounding.Unrounded,
          CORNER_ROUND_100,
          CORNER_ROUND_100,
          CornerRounding.Unrounded,
          CORNER_ROUND_100,
          CORNER_ROUND_100,
          CornerRounding.Unrounded,
          CornerRounding.Unrounded,
          CORNER_ROUND_100,
          CORNER_ROUND_100,
          CornerRounding.Unrounded,
          CORNER_ROUND_100
        };
    return RoundedPolygonKt.RoundedPolygon(
        sandwichPoints, CornerRounding.Unrounded, Arrays.asList(roundings));
  }

  @NonNull
  private static RoundedPolygon getHeart() {
    float[] points =
        new float[] {
          .2f, 0f, -.4f, .5f, -1f, 1f, -1.5f, .5f, -1f, 0f, -1.5f, -.5f, -1f, -1f, -.4f, -.5f
        };
    CornerRounding[] roundings =
        new CornerRounding[] {
          CornerRounding.Unrounded,
          CornerRounding.Unrounded,
          CORNER_ROUND_100,
          CORNER_ROUND_100,
          CornerRounding.Unrounded,
          CORNER_ROUND_100,
          CORNER_ROUND_100,
          CornerRounding.Unrounded,
        };
    return Shapes_androidKt.transformed(
        RoundedPolygonKt.RoundedPolygon(points, CornerRounding.Unrounded, Arrays.asList(roundings)),
        createRotationMatrix(90f));
  }

  @NonNull
  private static RoundedPolygon triangleChip(float innerRadius, CornerRounding cornerRounding) {
    PointF[] radialPoints =
        new PointF[] {
          new PointF(0.888f, (float) Math.toRadians(270f)),
          new PointF(1f, (float) Math.toRadians(30f)),
          new PointF(innerRadius, (float) Math.toRadians(90f)),
          new PointF(1f, (float) Math.toRadians(150f))
        };
    PointF[] cartesianPoints = new PointF[radialPoints.length];
    for (int i = 0; i < radialPoints.length; i++) {
      cartesianPoints[i] = radialToCartesian(radialPoints[i]);
    }
    float[] cartesianPointsXy = new float[cartesianPoints.length * 2];
    for (int i = 0; i < cartesianPoints.length; i++) {
      cartesianPointsXy[2 * i] = cartesianPoints[i].x;
      cartesianPointsXy[2 * i + 1] = cartesianPoints[i].y;
    }
    return RoundedPolygonKt.RoundedPolygon(cartesianPointsXy, cornerRounding);
  }

  private static void repeatAroundCenter(
      @NonNull float[] srcPointsXy,
      @NonNull float[] dstPointsXy,
      @Nullable CornerRounding[] srcRoundings,
      @Nullable CornerRounding[] dstRoundings,
      int reps,
      float centerX,
      float centerY,
      float srcRotateOffset,
      boolean alternate) {
    float rotationOffset = 360f / reps;
    int srcPointsCount = srcPointsXy.length / 2;
    // Moves src points centered at (0, 0) and rotates src points to start from 0 degree.
    Matrix preTransform = createRotationMatrix(-srcRotateOffset);
    preTransform.preTranslate(-centerX, -centerY);
    preTransform.mapPoints(srcPointsXy);
    // Start repeat src points to dst points.
    int dstRoundingsWriteIndex = 0;
    int dstPointsXyWriteIndex = 0;
    for (int i = 0; i < reps; i++) {
      // Flips the odd indexed iteration direction, if alternate is true.
      boolean flip = alternate && (i % 2 == 1);
      for (int j = 0; j < srcPointsCount; j++) {
        // Reverts the src points order in dst points, if flips.
        int indexRounding = flip ? srcPointsCount - 1 - j : j;
        int indexX = indexRounding * 2;
        int indexY = indexX + 1;
        if (srcRoundings != null && dstRoundings != null) {
          dstRoundings[dstRoundingsWriteIndex++] = srcRoundings[indexRounding];
        }
        // Flips the src points along y axis, if flips.
        dstPointsXy[dstPointsXyWriteIndex++] = srcPointsXy[indexX] * (flip ? -1 : 1);
        dstPointsXy[dstPointsXyWriteIndex++] = srcPointsXy[indexY];
      }
      // Rotates 1 more offset rotation, if flips.
      createRotationMatrix(rotationOffset * (flip ? i + 1 : i))
          .mapPoints(
              dstPointsXy,
              srcPointsXy.length * i,
              dstPointsXy,
              srcPointsXy.length * i,
              srcPointsCount);
    }
    // Rotates dst points with the same offset and moves them back to the original center.
    Matrix postTransform = createRotationMatrix(srcRotateOffset);
    postTransform.postTranslate(centerX, centerY);
    postTransform.mapPoints(dstPointsXy);
  }

  @NonNull
  @Contract("_ -> new")
  private static PointF radialToCartesian(@NonNull PointF radialPoint) {
    float radius = radialPoint.x;
    float angle = radialPoint.y;
    return new PointF((float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)));
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
}
