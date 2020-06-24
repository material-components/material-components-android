package io.material.catalog.card;

import io.material.catalog.R;

/*
 * Copyright 2018 The Android Open Source Project
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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import io.material.catalog.feature.DemoFragment;

/**
 * A fragment showing {@link MaterialCardView} states
 */
public class CardStatesFragment extends DemoFragment {

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_card_states;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater
        .inflate(R.layout.cat_card_states_fragment, viewGroup, false /* attachToRoot */);
    RadioGroup radioGroup = view.findViewById(R.id.cat_card_radio_group);
    final MaterialCardView card = view.findViewById(R.id.card);
    final MaterialCardView checkableCard = view.findViewById(R.id.checkable_card);

    checkableCard.setOnLongClickListener(
        v -> {
          checkableCard.setChecked(!checkableCard.isChecked());
          return true;
        });

    radioGroup.setOnCheckedChangeListener(
        (group, checkedId) -> {
          card.setHovered(checkedId == R.id.hovered);
          card.setPressed(checkedId == R.id.pressed);
          checkableCard.setHovered(checkedId == R.id.hovered);
          checkableCard.setPressed(checkedId == R.id.pressed);
        });

    return view;
  }
}
