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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.switchmaterial.SwitchMaterial;

class SwitchRowView extends FrameLayout {

  private CharSequence subtitle;
  private CharSequence subtitleOn;
  private CharSequence subtitleOff;

  private final TextView titleView;
  private final TextView subtitleView;
  private final SwitchMaterial switchMaterial;

  public SwitchRowView(@NonNull Context context) {
    this(context, null);
  }

  public SwitchRowView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SwitchRowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchRowView);

    CharSequence title = a.getText(R.styleable.SwitchRowView_title);
    subtitle = a.getText(R.styleable.SwitchRowView_subtitle);
    subtitleOn = a.getText(R.styleable.SwitchRowView_subtitleOn);
    subtitleOff = a.getText(R.styleable.SwitchRowView_subtitleOff);

    a.recycle();

    LayoutInflater.from(context).inflate(R.layout.switch_row_view, this);
    titleView = findViewById(R.id.switch_row_title);
    subtitleView = findViewById(R.id.switch_row_subtitle);
    switchMaterial = findViewById(R.id.switch_row_switch);

    titleView.setText(title);
    updateSubtitle();
    switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> updateSubtitle());
  }

  public CharSequence getTitle() {
    return titleView.getText();
  }

  public void setTitle(CharSequence title) {
    titleView.setText(title);
  }

  public CharSequence getSubtitle() {
    return subtitle;
  }

  public void setSubtitle(CharSequence subtitle) {
    this.subtitle = subtitle;
    updateSubtitle();
  }

  public CharSequence getSubtitleOn() {
    return subtitleOn;
  }

  public void setSubtitleOn(CharSequence subtitleOn) {
    this.subtitleOn = subtitleOn;
    updateSubtitle();
  }

  public CharSequence getSubtitleOff() {
    return subtitleOff;
  }

  public void setSubtitleOff(CharSequence subtitleOff) {
    this.subtitleOff = subtitleOff;
    updateSubtitle();
  }

  private void updateSubtitle() {
    subtitleView.setText(getSubtitleText(switchMaterial.isChecked()));
  }

  private CharSequence getSubtitleText(boolean isChecked) {
    if (isChecked && subtitleOn != null) {
      return subtitleOn;
    }
    if (!isChecked && subtitleOff != null) {
      return subtitleOff;
    }
    return subtitle;
  }
}
