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

/**
 * A class that solves the HCT equation.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class HctSolver {
  private HctSolver() {}

  static final double[][] SCALED_DISCOUNT_FROM_LINRGB =
      new double[][] {
        new double[] {
          0.001200833568784504, 0.002389694492170889, 0.0002795742885861124,
        },
        new double[] {
          0.0005891086651375999, 0.0029785502573438758, 0.0003270666104008398,
        },
        new double[] {
          0.00010146692491640572, 0.0005364214359186694, 0.0032979401770712076,
        },
      };

  static final double[][] LINRGB_FROM_SCALED_DISCOUNT =
      new double[][] {
        new double[] {
          1373.2198709594231, -1100.4251190754821, -7.278681089101213,
        },
        new double[] {
          -271.815969077903, 559.6580465940733, -32.46047482791194,
        },
        new double[] {
          1.9622899599665666, -57.173814538844006, 308.7233197812385,
        },
      };

  static final double[] Y_FROM_LINRGB = new double[] {0.2126, 0.7152, 0.0722};

  static final double[] CRITICAL_PLANES =
      new double[] {
        0.015176349177441876,
        0.045529047532325624,
        0.07588174588720938,
        0.10623444424209313,
        0.13658714259697685,
        0.16693984095186062,
        0.19729253930674434,
        0.2276452376616281,
        0.2579979360165119,
        0.28835063437139563,
        0.3188300904430532,
        0.350925934958123,
        0.3848314933096426,
        0.42057480301049466,
        0.458183274052838,
        0.4976837250274023,
        0.5391024159806381,
        0.5824650784040898,
        0.6277969426914107,
        0.6751227633498623,
        0.7244668422128921,
        0.775853049866786,
        0.829304845476233,
        0.8848452951698498,
        0.942497089126609,
        1.0022825574869039,
        1.0642236851973577,
        1.1283421258858297,
        1.1946592148522128,
        1.2631959812511864,
        1.3339731595349034,
        1.407011200216447,
        1.4823302800086415,
        1.5599503113873272,
        1.6398909516233677,
        1.7221716113234105,
        1.8068114625156377,
        1.8938294463134073,
        1.9832442801866852,
        2.075074464868551,
        2.1693382909216234,
        2.2660538449872063,
        2.36523901573795,
        2.4669114995532007,
        2.5710888059345764,
        2.6777882626779785,
        2.7870270208169257,
        2.898822059350997,
        3.0131901897720907,
        3.1301480604002863,
        3.2497121605402226,
        3.3718988244681087,
        3.4967242352587946,
        3.624204428461639,
        3.754355295633311,
        3.887192587735158,
        4.022731918402185,
        4.160988767090289,
        4.301978482107941,
        4.445716283538092,
        4.592217266055746,
        4.741496401646282,
        4.893568542229298,
        5.048448422192488,
        5.20615066083972,
        5.3666897647573375,
        5.5300801301023865,
        5.696336044816294,
        5.865471690767354,
        6.037501145825082,
        6.212438385869475,
        6.390297286737924,
        6.571091626112461,
        6.7548350853498045,
        6.941541251256611,
        7.131223617812143,
        7.323895587840543,
        7.5195704746346665,
        7.7182615035334345,
        7.919981813454504,
        8.124744458384042,
        8.332562408825165,
        8.543448553206703,
        8.757415699253682,
        8.974476575321063,
        9.194643831691977,
        9.417930041841839,
        9.644347703669503,
        9.873909240696694,
        10.106627003236781,
        10.342513269534024,
        10.58158024687427,
        10.8238400726681,
        11.069304815507364,
        11.317986476196008,
        11.569896988756009,
        11.825048221409341,
        12.083451977536606,
        12.345119996613247,
        12.610063955123938,
        12.878295467455942,
        13.149826086772048,
        13.42466730586372,
        13.702830557985108,
        13.984327217668513,
        14.269168601521828,
        14.55736596900856,
        14.848930523210871,
        15.143873411576273,
        15.44220572664832,
        15.743938506781891,
        16.04908273684337,
        16.35764934889634,
        16.66964922287304,
        16.985093187232053,
        17.30399201960269,
        17.62635644741625,
        17.95219714852476,
        18.281524751807332,
        18.614349837764564,
        18.95068293910138,
        19.290534541298456,
        19.633915083172692,
        19.98083495742689,
        20.331304511189067,
        20.685334046541502,
        21.042933821039977,
        21.404114048223256,
        21.76888489811322,
        22.137256497705877,
        22.50923893145328,
        22.884842241736916,
        23.264076429332462,
        23.6469514538663,
        24.033477234264016,
        24.42366364919083,
        24.817520537484558,
        25.21505769858089,
        25.61628489293138,
        26.021211842414342,
        26.429848230738664,
        26.842203703840827,
        27.258287870275353,
        27.678110301598522,
        28.10168053274597,
        28.529008062403893,
        28.96010235337422,
        29.39497283293396,
        29.83362889318845,
        30.276079891419332,
        30.722335150426627,
        31.172403958865512,
        31.62629557157785,
        32.08401920991837,
        32.54558406207592,
        33.010999283389665,
        33.4802739966603,
        33.953417292456834,
        34.430438229418264,
        34.911345834551085,
        35.39614910352207,
        35.88485700094671,
        36.37747846067349,
        36.87402238606382,
        37.37449765026789,
        37.87891309649659,
        38.38727753828926,
        38.89959975977785,
        39.41588851594697,
        39.93615253289054,
        40.460400508064545,
        40.98864111053629,
        41.520882981230194,
        42.05713473317016,
        42.597404951718396,
        43.141702194811224,
        43.6900349931913,
        44.24241185063697,
        44.798841244188324,
        45.35933162437017,
        45.92389141541209,
        46.49252901546552,
        47.065252796817916,
        47.64207110610409,
        48.22299226451468,
        48.808024568002054,
        49.3971762874833,
        49.9904556690408,
        50.587870934119984,
        51.189430279724725,
        51.79514187861014,
        52.40501387947288,
        53.0190544071392,
        53.637271562750364,
        54.259673423945976,
        54.88626804504493,
        55.517063457223934,
        56.15206766869424,
        56.79128866487574,
        57.43473440856916,
        58.08241284012621,
        58.734331877617365,
        59.39049941699807,
        60.05092333227251,
        60.715611475655585,
        61.38457167773311,
        62.057811747619894,
        62.7353394731159,
        63.417162620860914,
        64.10328893648692,
        64.79372614476921,
        65.48848194977529,
        66.18756403501224,
        66.89098006357258,
        67.59873767827808,
        68.31084450182222,
        69.02730813691093,
        69.74813616640164,
        70.47333615344107,
        71.20291564160104,
        71.93688215501312,
        72.67524319850172,
        73.41800625771542,
        74.16517879925733,
        74.9167682708136,
        75.67278210128072,
        76.43322770089146,
        77.1981124613393,
        77.96744375590167,
        78.74122893956174,
        79.51947534912904,
        80.30219030335869,
        81.08938110306934,
        81.88105503125999,
        82.67721935322541,
        83.4778813166706,
        84.28304815182372,
        85.09272707154808,
        85.90692527145302,
        86.72564993000343,
        87.54890820862819,
        88.3767072518277,
        89.2090541872801,
        90.04595612594655,
        90.88742016217518,
        91.73345337380438,
        92.58406282226491,
        93.43925555268066,
        94.29903859396902,
        95.16341895893969,
        96.03240364439274,
        96.9059996312159,
        97.78421388448044,
        98.6670533535366,
        99.55452497210776,
      };

  /**
   * Sanitizes a small enough angle in radians.
   *
   * @param angle An angle in radians; must not deviate too much from 0.
   * @return A coterminal angle between 0 and 2pi.
   */
  static double sanitizeRadians(double angle) {
    return (angle + Math.PI * 8) % (Math.PI * 2);
  }

  /**
   * Delinearizes an RGB component, returning a floating-point number.
   *
   * @param rgbComponent 0.0 <= rgb_component <= 100.0, represents linear R/G/B channel
   * @return 0.0 <= output <= 255.0, color channel converted to regular RGB space
   */
  static double trueDelinearized(double rgbComponent) {
    double normalized = rgbComponent / 100.0;
    double delinearized = 0.0;
    if (normalized <= 0.0031308) {
      delinearized = normalized * 12.92;
    } else {
      delinearized = 1.055 * Math.pow(normalized, 1.0 / 2.4) - 0.055;
    }
    return delinearized * 255.0;
  }

  static double chromaticAdaptation(double component) {
    double af = Math.pow(Math.abs(component), 0.42);
    return MathUtils.signum(component) * 400.0 * af / (af + 27.13);
  }

  /**
   * Returns the hue of a linear RGB color in CAM16.
   *
   * @param linrgb The linear RGB coordinates of a color.
   * @return The hue of the color in CAM16, in radians.
   */
  static double hueOf(double[] linrgb) {
    double[] scaledDiscount = MathUtils.matrixMultiply(linrgb, SCALED_DISCOUNT_FROM_LINRGB);
    double rA = chromaticAdaptation(scaledDiscount[0]);
    double gA = chromaticAdaptation(scaledDiscount[1]);
    double bA = chromaticAdaptation(scaledDiscount[2]);
    // redness-greenness
    double a = (11.0 * rA + -12.0 * gA + bA) / 11.0;
    // yellowness-blueness
    double b = (rA + gA - 2.0 * bA) / 9.0;
    return Math.atan2(b, a);
  }

  static boolean areInCyclicOrder(double a, double b, double c) {
    double deltaAB = sanitizeRadians(b - a);
    double deltaAC = sanitizeRadians(c - a);
    return deltaAB < deltaAC;
  }

  /**
   * Solves the lerp equation.
   *
   * @param source The starting number.
   * @param mid The number in the middle.
   * @param target The ending number.
   * @return A number t such that lerp(source, target, t) = mid.
   */
  static double intercept(double source, double mid, double target) {
    return (mid - source) / (target - source);
  }

  static double[] lerpPoint(double[] source, double t, double[] target) {
    return new double[] {
      source[0] + (target[0] - source[0]) * t,
      source[1] + (target[1] - source[1]) * t,
      source[2] + (target[2] - source[2]) * t,
    };
  }

  /**
   * Intersects a segment with a plane.
   *
   * @param source The coordinates of point A.
   * @param coordinate The R-, G-, or B-coordinate of the plane.
   * @param target The coordinates of point B.
   * @param axis The axis the plane is perpendicular with. (0: R, 1: G, 2: B)
   * @return The intersection point of the segment AB with the plane R=coordinate, G=coordinate, or
   *     B=coordinate
   */
  static double[] setCoordinate(double[] source, double coordinate, double[] target, int axis) {
    double t = intercept(source[axis], coordinate, target[axis]);
    return lerpPoint(source, t, target);
  }

  static boolean isBounded(double x) {
    return 0.0 <= x && x <= 100.0;
  }

  /**
   * Returns the nth possible vertex of the polygonal intersection.
   *
   * @param y The Y value of the plane.
   * @param n The zero-based index of the point. 0 <= n <= 11.
   * @return The nth possible vertex of the polygonal intersection of the y plane and the RGB cube,
   *     in linear RGB coordinates, if it exists. If this possible vertex lies outside of the cube,
   *     [-1.0, -1.0, -1.0] is returned.
   */
  static double[] nthVertex(double y, int n) {
    double kR = Y_FROM_LINRGB[0];
    double kG = Y_FROM_LINRGB[1];
    double kB = Y_FROM_LINRGB[2];
    double coordA = n % 4 <= 1 ? 0.0 : 100.0;
    double coordB = n % 2 == 0 ? 0.0 : 100.0;
    if (n < 4) {
      double g = coordA;
      double b = coordB;
      double r = (y - g * kG - b * kB) / kR;
      if (isBounded(r)) {
        return new double[] {r, g, b};
      } else {
        return new double[] {-1.0, -1.0, -1.0};
      }
    } else if (n < 8) {
      double b = coordA;
      double r = coordB;
      double g = (y - r * kR - b * kB) / kG;
      if (isBounded(g)) {
        return new double[] {r, g, b};
      } else {
        return new double[] {-1.0, -1.0, -1.0};
      }
    } else {
      double r = coordA;
      double g = coordB;
      double b = (y - r * kR - g * kG) / kB;
      if (isBounded(b)) {
        return new double[] {r, g, b};
      } else {
        return new double[] {-1.0, -1.0, -1.0};
      }
    }
  }

  /**
   * Finds the segment containing the desired color.
   *
   * @param y The Y value of the color.
   * @param targetHue The hue of the color.
   * @return A list of two sets of linear RGB coordinates, each corresponding to an endpoint of the
   *     segment containing the desired color.
   */
  static double[][] bisectToSegment(double y, double targetHue) {
    double[] left = new double[] {-1.0, -1.0, -1.0};
    double[] right = left;
    double leftHue = 0.0;
    double rightHue = 0.0;
    boolean initialized = false;
    boolean uncut = true;
    for (int n = 0; n < 12; n++) {
      double[] mid = nthVertex(y, n);
      if (mid[0] < 0) {
        continue;
      }
      double midHue = hueOf(mid);
      if (!initialized) {
        left = mid;
        right = mid;
        leftHue = midHue;
        rightHue = midHue;
        initialized = true;
        continue;
      }
      if (uncut || areInCyclicOrder(leftHue, midHue, rightHue)) {
        uncut = false;
        if (areInCyclicOrder(leftHue, targetHue, midHue)) {
          right = mid;
          rightHue = midHue;
        } else {
          left = mid;
          leftHue = midHue;
        }
      }
    }
    return new double[][] {left, right};
  }

  static double[] midpoint(double[] a, double[] b) {
    return new double[] {
      (a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2,
    };
  }

  static int criticalPlaneBelow(double x) {
    return (int) Math.floor(x - 0.5);
  }

  static int criticalPlaneAbove(double x) {
    return (int) Math.ceil(x - 0.5);
  }

  /**
   * Finds a color with the given Y and hue on the boundary of the cube.
   *
   * @param y The Y value of the color.
   * @param targetHue The hue of the color.
   * @return The desired color, in linear RGB coordinates.
   */
  static double[] bisectToLimit(double y, double targetHue) {
    double[][] segment = bisectToSegment(y, targetHue);
    double[] left = segment[0];
    double leftHue = hueOf(left);
    double[] right = segment[1];
    for (int axis = 0; axis < 3; axis++) {
      if (left[axis] != right[axis]) {
        int lPlane = -1;
        int rPlane = 255;
        if (left[axis] < right[axis]) {
          lPlane = criticalPlaneBelow(trueDelinearized(left[axis]));
          rPlane = criticalPlaneAbove(trueDelinearized(right[axis]));
        } else {
          lPlane = criticalPlaneAbove(trueDelinearized(left[axis]));
          rPlane = criticalPlaneBelow(trueDelinearized(right[axis]));
        }
        for (int i = 0; i < 8; i++) {
          if (Math.abs(rPlane - lPlane) <= 1) {
            break;
          } else {
            int mPlane = (int) Math.floor((lPlane + rPlane) / 2.0);
            double midPlaneCoordinate = CRITICAL_PLANES[mPlane];
            double[] mid = setCoordinate(left, midPlaneCoordinate, right, axis);
            double midHue = hueOf(mid);
            if (areInCyclicOrder(leftHue, targetHue, midHue)) {
              right = mid;
              rPlane = mPlane;
            } else {
              left = mid;
              leftHue = midHue;
              lPlane = mPlane;
            }
          }
        }
      }
    }
    return midpoint(left, right);
  }

  static double inverseChromaticAdaptation(double adapted) {
    double adaptedAbs = Math.abs(adapted);
    double base = Math.max(0, 27.13 * adaptedAbs / (400.0 - adaptedAbs));
    return MathUtils.signum(adapted) * Math.pow(base, 1.0 / 0.42);
  }

  /**
   * Finds a color with the given hue, chroma, and Y.
   *
   * @param hueRadians The desired hue in radians.
   * @param chroma The desired chroma.
   * @param y The desired Y.
   * @return The desired color as a hexadecimal integer, if found; 0 otherwise.
   */
  static int findResultByJ(double hueRadians, double chroma, double y) {
    // Initial estimate of j.
    double j = Math.sqrt(y) * 11.0;
    // ===========================================================
    // Operations inlined from Cam16 to avoid repeated calculation
    // ===========================================================
    ViewingConditions viewingConditions = ViewingConditions.DEFAULT;
    double tInnerCoeff = 1 / Math.pow(1.64 - Math.pow(0.29, viewingConditions.getN()), 0.73);
    double eHue = 0.25 * (Math.cos(hueRadians + 2.0) + 3.8);
    double p1 = eHue * (50000.0 / 13.0) * viewingConditions.getNc() * viewingConditions.getNcb();
    double hSin = Math.sin(hueRadians);
    double hCos = Math.cos(hueRadians);
    for (int iterationRound = 0; iterationRound < 5; iterationRound++) {
      // ===========================================================
      // Operations inlined from Cam16 to avoid repeated calculation
      // ===========================================================
      double jNormalized = j / 100.0;
      double alpha = chroma == 0.0 || j == 0.0 ? 0.0 : chroma / Math.sqrt(jNormalized);
      double t = Math.pow(alpha * tInnerCoeff, 1.0 / 0.9);
      double ac =
          viewingConditions.getAw()
              * Math.pow(jNormalized, 1.0 / viewingConditions.getC() / viewingConditions.getZ());
      double p2 = ac / viewingConditions.getNbb();
      double gamma = 23.0 * (p2 + 0.305) * t / (23.0 * p1 + 11 * t * hCos + 108.0 * t * hSin);
      double a = gamma * hCos;
      double b = gamma * hSin;
      double rA = (460.0 * p2 + 451.0 * a + 288.0 * b) / 1403.0;
      double gA = (460.0 * p2 - 891.0 * a - 261.0 * b) / 1403.0;
      double bA = (460.0 * p2 - 220.0 * a - 6300.0 * b) / 1403.0;
      double rCScaled = inverseChromaticAdaptation(rA);
      double gCScaled = inverseChromaticAdaptation(gA);
      double bCScaled = inverseChromaticAdaptation(bA);
      double[] linrgb =
          MathUtils.matrixMultiply(
              new double[] {rCScaled, gCScaled, bCScaled}, LINRGB_FROM_SCALED_DISCOUNT);
      // ===========================================================
      // Operations inlined from Cam16 to avoid repeated calculation
      // ===========================================================
      if (linrgb[0] < 0 || linrgb[1] < 0 || linrgb[2] < 0) {
        return 0;
      }
      double kR = Y_FROM_LINRGB[0];
      double kG = Y_FROM_LINRGB[1];
      double kB = Y_FROM_LINRGB[2];
      double fnj = kR * linrgb[0] + kG * linrgb[1] + kB * linrgb[2];
      if (fnj <= 0) {
        return 0;
      }
      if (iterationRound == 4 || Math.abs(fnj - y) < 0.002) {
        if (linrgb[0] > 100.01 || linrgb[1] > 100.01 || linrgb[2] > 100.01) {
          return 0;
        }
        return ColorUtils.argbFromLinrgb(linrgb);
      }
      // Iterates with Newton method,
      // Using 2 * fn(j) / j as the approximation of fn'(j)
      j = j - (fnj - y) * j / (2 * fnj);
    }
    return 0;
  }

  /**
   * Finds an sRGB color with the given hue, chroma, and L*, if possible.
   *
   * @param hueDegrees The desired hue, in degrees.
   * @param chroma The desired chroma.
   * @param lstar The desired L*.
   * @return A hexadecimal representing the sRGB color. The color has sufficiently close hue,
   *     chroma, and L* to the desired values, if possible; otherwise, the hue and L* will be
   *     sufficiently close, and chroma will be maximized.
   */
  public static int solveToInt(double hueDegrees, double chroma, double lstar) {
    if (chroma < 0.0001 || lstar < 0.0001 || lstar > 99.9999) {
      return ColorUtils.argbFromLstar(lstar);
    }
    hueDegrees = MathUtils.sanitizeDegreesDouble(hueDegrees);
    double hueRadians = hueDegrees / 180 * Math.PI;
    double y = ColorUtils.yFromLstar(lstar);
    int exactAnswer = findResultByJ(hueRadians, chroma, y);
    if (exactAnswer != 0) {
      return exactAnswer;
    }
    double[] linrgb = bisectToLimit(y, hueRadians);
    return ColorUtils.argbFromLinrgb(linrgb);
  }

  /**
   * Finds an sRGB color with the given hue, chroma, and L*, if possible.
   *
   * @param hueDegrees The desired hue, in degrees.
   * @param chroma The desired chroma.
   * @param lstar The desired L*.
   * @return An CAM16 object representing the sRGB color. The color has sufficiently close hue,
   *     chroma, and L* to the desired values, if possible; otherwise, the hue and L* will be
   *     sufficiently close, and chroma will be maximized.
   */
  public static Cam16 solveToCam(double hueDegrees, double chroma, double lstar) {
    return Cam16.fromInt(solveToInt(hueDegrees, chroma, lstar));
  }
}
