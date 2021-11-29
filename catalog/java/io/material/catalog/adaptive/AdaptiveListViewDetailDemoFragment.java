/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.adaptive;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import io.material.catalog.adaptive.AdaptiveListViewDemoFragment.EmailData;
import io.material.catalog.adaptive.AdaptiveListViewDemoFragment.EmailData.Email;

/** A Fragment that displays an email's details. */
public class AdaptiveListViewDetailDemoFragment extends Fragment {

  public static final String TAG = "AdaptiveListViewDetailDemoFragment";
  private static final String EMAIL_ID_KEY = "email_id_key";

  @NonNull
  public static AdaptiveListViewDetailDemoFragment newInstance(long emailId) {
    AdaptiveListViewDetailDemoFragment fragment = new AdaptiveListViewDetailDemoFragment();
    Bundle bundle = new Bundle();
    bundle.putLong(EMAIL_ID_KEY, emailId);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_adaptive_list_view_detail_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    long emailId = getEmailId();
    TextView emailTitle = view.findViewById(R.id.email_title);
    emailTitle.append(" " + (emailId + 1));
    // Set transition name that matches the list item to be transitioned from for the shared element
    // transition.
    View container = view.findViewById(R.id.list_view_detail_container);
    ViewCompat.setTransitionName(container, emailTitle.toString());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    updateEmailSelected(false);
  }

  @Override
  public void onStart() {
    super.onStart();
    updateEmailSelected(true);
  }

  private void updateEmailSelected(boolean selected) {
    Email email = EmailData.getEmailById(getEmailId());
    email.setSelected(selected);
  }

  private long getEmailId() {
    long emailId = 0L;
    if (getArguments() != null) {
      emailId = getArguments().getLong(EMAIL_ID_KEY, 0L);
    }
    return emailId;
  }
}
