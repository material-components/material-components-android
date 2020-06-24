/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.transition;

import io.material.catalog.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.os.Build.VERSION_CODES;
import androidx.core.view.animation.PathInterpolatorCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnChangeListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;
import io.material.catalog.feature.ContainerTransformConfiguration;

/**
 * A helper class which manages all configuration UI presented in {@link
 * TransitionContainerTransformDemoFragment}.
 */
public class ContainerTransformConfigurationHelper {

  private static final String CUBIC_CONTROL_FORMAT = "%.3f";
  private static final String DURATION_FORMAT = "%.0f";

  private boolean arcMotionEnabled;
  private long enterDuration;
  private long returnDuration;
  private Interpolator interpolator;
  private int fadeModeButtonId;
  private boolean drawDebugEnabled;

  private ContainerTransformConfiguration containerTransformConfiguration;

  private static final SparseIntArray FADE_MODE_MAP = new SparseIntArray();

  static {
    FADE_MODE_MAP.append(R.id.fade_in_button, MaterialContainerTransform.FADE_MODE_IN);
    FADE_MODE_MAP.append(R.id.fade_out_button, MaterialContainerTransform.FADE_MODE_OUT);
    FADE_MODE_MAP.append(R.id.fade_cross_button, MaterialContainerTransform.FADE_MODE_CROSS);
    FADE_MODE_MAP.append(R.id.fade_through_button, MaterialContainerTransform.FADE_MODE_THROUGH);
  }

  public ContainerTransformConfigurationHelper(
      ContainerTransformConfiguration containerTransformConfiguration) {
    this.containerTransformConfiguration = containerTransformConfiguration;
    setUpDefaultValues();
  }

  /**
   * Show configuration chooser associated with a container transform from {@link
   * TransitionContainerTransformDemoFragment}.
   */
  void showConfigurationChooser(Context context, @Nullable OnDismissListener onDismissListener) {
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
    bottomSheetDialog.setContentView(
        createConfigurationBottomSheetView(context, bottomSheetDialog));
    bottomSheetDialog.setOnDismissListener(onDismissListener);
    bottomSheetDialog.show();
  }

  /** Set up the androidx transition according to the config helper's parameters. */
  void configure(MaterialContainerTransform transform, boolean entering) {
    transform.setDuration(entering ? getEnterDuration() : getReturnDuration());
    transform.setInterpolator(getInterpolator());
    if (isArcMotionEnabled()) {
      transform.setPathMotion(new MaterialArcMotion());
    }
    transform.setFadeMode(getFadeMode());
    transform.setDrawDebugEnabled(isDrawDebugEnabled());
  }

  /** Set up the platform transition according to the config helper's parameters. */
  @RequiresApi(VERSION_CODES.LOLLIPOP)
  void configure(
      com.google.android.material.transition.platform.MaterialContainerTransform transform,
      boolean entering) {
    transform.setDuration(entering ? getEnterDuration() : getReturnDuration());
    transform.setInterpolator(getInterpolator());
    if (isArcMotionEnabled()) {
      transform.setPathMotion(
          new com.google.android.material.transition.platform.MaterialArcMotion());
    }
    transform.setFadeMode(getFadeMode());
    transform.setDrawDebugEnabled(isDrawDebugEnabled());
  }

  /**
   * Whether or not to a custom container transform should use {@link
   * com.google.android.material.transition.MaterialArcMotion}.
   */
  boolean isArcMotionEnabled() {
    return arcMotionEnabled;
  }

  /** The enter duration to be used by a custom container transform. */
  long getEnterDuration() {
    return enterDuration;
  }

  /** The return duration to be used by a custom container transform. */
  long getReturnDuration() {
    return returnDuration;
  }

  /** The interpolator to be used by a custom container transform. */
  Interpolator getInterpolator() {
    return interpolator;
  }

  /** The fade mode used by a custom container transform. */
  int getFadeMode() {
    return FADE_MODE_MAP.get(fadeModeButtonId);
  }

  /** Whether or not the custom transform should draw debugging lines. */
  boolean isDrawDebugEnabled() {
    return drawDebugEnabled;
  }

  private void setUpDefaultValues() {
    arcMotionEnabled = containerTransformConfiguration.isArcMotionEnabled();
    enterDuration = containerTransformConfiguration.getEnterDuration();
    returnDuration = containerTransformConfiguration.getReturnDuration();
    interpolator = containerTransformConfiguration.getInterpolator();
    fadeModeButtonId = R.id.fade_in_button;
    drawDebugEnabled = false;
  }

  /** Create a bottom sheet dialog that displays controls to configure a container transform. */
  private View createConfigurationBottomSheetView(Context context, BottomSheetDialog dialog) {
    View layout =
        LayoutInflater.from(context).inflate(R.layout.cat_transition_configuration_layout, null);
    setUpBottomSheetPathMotionButtonGroup(layout);
    setUpBottomSheetEnterDurationSlider(layout);
    setUpBottomSheetReturnDurationSlider(layout);
    setUpBottomSheetInterpolation(layout);
    setUpBottomSheetFadeModeButtonGroup(layout);
    setUpBottomSheetDebugging(layout);
    setUpBottomSheetConfirmationButtons(layout, dialog);
    return layout;
  }

  /** Update whether to use arc motion based on the selected radio button */
  private void setUpBottomSheetPathMotionButtonGroup(View view) {
    MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.path_motion_button_group);
    if (toggleGroup != null) {
      // Set initial value.
      toggleGroup.check(arcMotionEnabled ? R.id.arc_motion_button : R.id.linear_motion_button);
      toggleGroup.addOnButtonCheckedListener(
          (group, checkedId, isChecked) -> {
            if (checkedId == R.id.arc_motion_button) {
              arcMotionEnabled = isChecked;
            }
          });
    }
  }

  /** Update the fade mode based on the selected radio button */
  private void setUpBottomSheetFadeModeButtonGroup(View view) {
    MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.fade_mode_button_group);
    if (toggleGroup != null) {
      // Set initial value.
      toggleGroup.check(fadeModeButtonId);
      toggleGroup.addOnButtonCheckedListener(
          (group, checkedId, isChecked) -> {
            if (isChecked) {
              fadeModeButtonId = checkedId;
            }
          });
    }
  }

  /** Update enter duration and duration text when the slider value changes. */
  private void setUpBottomSheetEnterDurationSlider(View view) {
    setUpBottomSheetDurationSlider(
        view,
        R.id.enter_duration_slider,
        R.id.enter_duration_value,
        enterDuration,
        (slider, value, fromUser) -> enterDuration = (long) value);
  }

  /** Update return duration and duration text when the slider value changes. */
  private void setUpBottomSheetReturnDurationSlider(View view) {
    setUpBottomSheetDurationSlider(
        view,
        R.id.return_duration_slider,
        R.id.return_duration_value,
        returnDuration,
        (slider, value, fromUser) -> returnDuration = (long) value);
  }

  @SuppressLint("DefaultLocale")
  private void setUpBottomSheetDurationSlider(
      View view,
      @IdRes int sliderResId,
      @IdRes int labelResId,
      float duration,
      OnChangeListener listener) {
    Slider durationSlider = view.findViewById(sliderResId);
    TextView durationValue = view.findViewById(labelResId);
    if (durationSlider != null && durationValue != null) {
      // Set initial value.
      durationSlider.setValue(duration);
      durationValue.setText(String.format(DURATION_FORMAT, durationSlider.getValue()));
      // Update the duration and durationValue's text whenever the slider is slid.
      durationSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            listener.onValueChange(slider, value, fromUser);
            durationValue.setText(String.format(DURATION_FORMAT, value));
          });
    }
  }

  /** Set up interpolation */
  private void setUpBottomSheetInterpolation(View view) {
    RadioGroup interpolationGroup = view.findViewById(R.id.interpolation_radio_group);
    ViewGroup customContainer = view.findViewById(R.id.custom_curve_container);
    if (interpolationGroup != null && customContainer != null) {

      setTextInputClearOnTextChanged(view.findViewById(R.id.x1_text_input_layout));
      setTextInputClearOnTextChanged(view.findViewById(R.id.x2_text_input_layout));
      setTextInputClearOnTextChanged(view.findViewById(R.id.y1_text_input_layout));
      setTextInputClearOnTextChanged(view.findViewById(R.id.y2_text_input_layout));

      // Check the correct current radio button and fill in custom bezier fields if applicable.
      if (interpolator instanceof FastOutSlowInInterpolator) {
        interpolationGroup.check(R.id.radio_fast_out_slow_in);
      } else {
        interpolationGroup.check(R.id.radio_custom);
        CustomCubicBezier currentInterp = (CustomCubicBezier) interpolator;
        setTextFloat(view.findViewById(R.id.x1_edit_text), currentInterp.controlX1);
        setTextFloat(view.findViewById(R.id.y1_edit_text), currentInterp.controlY1);
        setTextFloat(view.findViewById(R.id.x2_edit_text), currentInterp.controlX2);
        setTextFloat(view.findViewById(R.id.y2_edit_text), currentInterp.controlY2);
      }

      // Enable/disable custom bezier fields depending on initial checked radio button.
      setViewGroupDescendantsEnabled(
          customContainer, interpolationGroup.getCheckedRadioButtonId() == R.id.radio_custom);

      // Watch for any changes to the selected radio button and update custom bezier enabled state.
      // The custom bezier values will be captured when the configuration is applied.
      interpolationGroup.setOnCheckedChangeListener(
          (group, checkedId) ->
              setViewGroupDescendantsEnabled(customContainer, checkedId == R.id.radio_custom));
    }
  }

  @SuppressLint("DefaultLocale")
  private static void setTextFloat(EditText editText, float value) {
    editText.setText(String.format(CUBIC_CONTROL_FORMAT, value));
  }

  @Nullable
  private static Float getTextFloat(@Nullable EditText editText) {
    if (editText == null) {
      return null;
    }

    String text = editText.getText().toString();
    try {
      return Float.valueOf(text);
    } catch (Exception e) {
      return null;
    }
  }

  private static void setTextInputLayoutError(TextInputLayout layout) {
    layout.setError(" ");
  }

  private static void setTextInputClearOnTextChanged(TextInputLayout layout) {
    layout
        .getEditText()
        .addTextChangedListener(
            new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
              }

              @Override
              public void afterTextChanged(Editable s) {}
            });
  }

  private static boolean isValidCubicBezierControlValue(@Nullable Float value) {
    return value != null && value >= 0 && value <= 1;
  }

  private static boolean areValidCubicBezierControls(
      View view, Float x1, Float y1, Float x2, Float y2) {
    boolean isValid = true;
    if (!isValidCubicBezierControlValue(x1)) {
      isValid = false;
      setTextInputLayoutError(view.findViewById(R.id.x1_text_input_layout));
    }
    if (!isValidCubicBezierControlValue(y1)) {
      isValid = false;
      setTextInputLayoutError(view.findViewById(R.id.y1_text_input_layout));
    }
    if (!isValidCubicBezierControlValue(x2)) {
      isValid = false;
      setTextInputLayoutError(view.findViewById(R.id.x2_text_input_layout));
    }
    if (!isValidCubicBezierControlValue(y2)) {
      isValid = false;
      setTextInputLayoutError(view.findViewById(R.id.y2_text_input_layout));
    }

    return isValid;
  }

  /** Set up whether or not to draw debugging paint */
  private void setUpBottomSheetDebugging(View view) {
    CheckBox debugCheckbox = view.findViewById(R.id.draw_debug_checkbox);
    if (debugCheckbox != null) {
      debugCheckbox.setChecked(drawDebugEnabled);
      debugCheckbox.setOnCheckedChangeListener(
          (buttonView, isChecked) -> drawDebugEnabled = isChecked);
    }
  }

  /** Set up buttons to apply and validate configuration values and dismiss the bottom sheet */
  private void setUpBottomSheetConfirmationButtons(View view, BottomSheetDialog dialog) {
    view.findViewById(R.id.apply_button)
        .setOnClickListener(
            v -> {
              // Capture and update interpolation
              RadioGroup interpolationGroup = view.findViewById(R.id.interpolation_radio_group);
              if (interpolationGroup != null
                  && interpolationGroup.getCheckedRadioButtonId() == R.id.radio_custom) {
                Float x1 = getTextFloat(view.findViewById(R.id.x1_edit_text));
                Float y1 = getTextFloat(view.findViewById(R.id.y1_edit_text));
                Float x2 = getTextFloat(view.findViewById(R.id.x2_edit_text));
                Float y2 = getTextFloat(view.findViewById(R.id.y2_edit_text));

                if (areValidCubicBezierControls(view, x1, y1, x2, y2)) {
                  interpolator = new CustomCubicBezier(x1, y1, x2, y2);
                  dialog.dismiss();
                }
              } else {
                interpolator = new FastOutSlowInInterpolator();
                dialog.dismiss();
              }
            });

    view.findViewById(R.id.clear_button)
        .setOnClickListener(
            v -> {
              setUpDefaultValues();
              dialog.dismiss();
            });
  }

  private static void setViewGroupDescendantsEnabled(ViewGroup viewGroup, boolean enabled) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View view = viewGroup.getChildAt(i);
      view.setEnabled(enabled);
      if (view instanceof ViewGroup) {
        setViewGroupDescendantsEnabled((ViewGroup) view, enabled);
      }
    }
  }

  /** A custom cubic bezier interpolator which exposes it control points. */
  private static class CustomCubicBezier implements Interpolator {

    final float controlX1;
    final float controlY1;
    final float controlX2;
    final float controlY2;

    private final Interpolator interpolator;

    CustomCubicBezier(float controlX1, float controlY1, float controlX2, float controlY2) {
      this.controlX1 = controlX1;
      this.controlY1 = controlY1;
      this.controlX2 = controlX2;
      this.controlY2 = controlY2;

      this.interpolator = PathInterpolatorCompat.create(controlX1, controlY1, controlX2, controlY2);
    }

    @Override
    public float getInterpolation(float input) {
      return interpolator.getInterpolation(input);
    }

    String getDescription(Context context) {
      return context.getString(
          R.string.cat_transition_config_custom_interpolator_desc,
          controlX1,
          controlY1,
          controlX2,
          controlY2);
    }
  }
}
