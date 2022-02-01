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

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.transition.MaterialContainerTransform;
import io.material.catalog.adaptive.AdaptiveListViewDemoFragment.EmailAdapter.EmailAdapterListener;
import io.material.catalog.adaptive.AdaptiveListViewDemoFragment.EmailAdapter.EmailViewHolder;
import io.material.catalog.adaptive.AdaptiveListViewDemoFragment.EmailData.Email;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** A Fragment that hosts an email list view. */
public class AdaptiveListViewDemoFragment extends Fragment {

  @IdRes private int detailViewContainerId;
  @Nullable private RecyclerView emailList;
  private long currentSelectedEmailId = -1L;

  private final EmailAdapterListener emailAdapterListener =
      new EmailAdapterListener() {
        @Override
        public void onEmailClicked(@NonNull View view, long emailId) {
          AdaptiveListViewDetailDemoFragment fragment =
              AdaptiveListViewDetailDemoFragment.newInstance(emailId);

          if (currentSelectedEmailId != -1) {
            // Highlight selected email when fragments are side by side.
            setEmailSelected(currentSelectedEmailId, false);
            currentSelectedEmailId = emailId;
            setEmailSelected(emailId, true);
          }

          // Create a shared element transition when an email item is opened.
          MaterialContainerTransform enterTransform =
              new MaterialContainerTransform(requireContext(), /* entering= */ true);
          fragment.setSharedElementEnterTransition(enterTransform);

          // Don't add back to stack if fragments are both visible side by side.
          if (detailViewContainerId == R.id.list_view_detail_fragment_container) {
            getParentFragmentManager()
                .beginTransaction()
                .addSharedElement(view, ViewCompat.getTransitionName(view))
                .replace(detailViewContainerId, fragment, AdaptiveListViewDetailDemoFragment.TAG)
                .commit();
          } else {
            getParentFragmentManager()
                .beginTransaction()
                .addSharedElement(view, ViewCompat.getTransitionName(view))
                .replace(detailViewContainerId, fragment, AdaptiveListViewDetailDemoFragment.TAG)
                .addToBackStack(AdaptiveListViewDetailDemoFragment.TAG)
                .commit();
          }
        }
      };

  /**
   * Sets the id of the container that should hold the detail view fragment.
   *
   * @param detailViewContainerId the detail view fragment container id
   */
  public void setDetailViewContainerId(@IdRes int detailViewContainerId) {
    this.detailViewContainerId = detailViewContainerId;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(R.layout.cat_adaptive_list_view_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    // Set up email list's recycler view.
    emailList = view.findViewById(R.id.email_list);
    RecyclerView.LayoutManager layoutManager =
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    emailList.setLayoutManager(layoutManager);
    EmailAdapter adapter = new EmailAdapter(emailAdapterListener);
    adapter.setHasStableIds(true);
    emailList.setAdapter(adapter);
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      currentSelectedEmailId = 0L;
    }
  }

  /** Marks an email item as selected. */
  private void setEmailSelected(long emailId, boolean selected) {
    if (emailList == null) {
      return;
    }
    EmailViewHolder emailViewHolder = (EmailViewHolder) emailList.findViewHolderForItemId(emailId);
    if (emailViewHolder != null) {
      ((MaterialCardView) emailViewHolder.container).setChecked(selected);
    }
    Email email = EmailData.getEmailById(emailId);
    email.setSelected(selected);
  }

  /** A RecyclerView adapter for the email list. */
  static final class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {

    /** Listener for the email adapter. */
    public interface EmailAdapterListener {
      /**
       * Listens to when an email is clicked.
       *
       * @param view the email clicked.
       * @param emailId the email id.
       */
      void onEmailClicked(@NonNull View view, long emailId);
    }

    private final EmailAdapterListener listener;

    EmailAdapter(@NonNull EmailAdapterListener listener) {
      super();
      this.listener = listener;
    }

    /** Provides a reference to the views for each data item. */
    static class EmailViewHolder extends RecyclerView.ViewHolder {

      private final View container;
      private final TextView senderTitle;
      private final TextView emailTitle;
      private final TextView emailPreview;

      public EmailViewHolder(@NonNull View view) {
        super(view);
        container = view.findViewById(R.id.list_view_item_container);
        senderTitle = view.findViewById(R.id.sender_title);
        emailTitle = view.findViewById(R.id.email_title);
        emailPreview = view.findViewById(R.id.email_preview);
      }
    }

    private void updateEmailSelected(@NonNull EmailViewHolder holder) {
      // If on landscape orientation, update email to be selected or unselected.
      if (holder.container.getResources().getConfiguration().orientation
          == Configuration.ORIENTATION_LANDSCAPE) {
        Email email = EmailData.getEmailById(holder.getItemId());
        ((MaterialCardView) holder.container).setChecked(email.isSelected());
      }
    }

    @Override
    public long getItemId(int position) {
      return EmailData.emailData.get(position).getEmailId();
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.cat_adaptive_list_view_fragment_item, parent, false);
      return new EmailViewHolder(view);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull EmailViewHolder holder) {
      super.onViewAttachedToWindow(holder);
      updateEmailSelected(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
      Resources resources = holder.senderTitle.getResources();
      long emailId = getItemId(holder.getBindingAdapterPosition());
      holder.container.setOnClickListener(
          new OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
              listener.onEmailClicked(view, emailId);
            }
          });
      holder.senderTitle.setText(resources.getString(R.string.cat_list_view_sender_title));
      holder.emailTitle.setText(resources.getString(R.string.cat_list_view_email_title));
      holder.emailTitle.append(" " + (emailId + 1));
      holder.emailPreview.setText(resources.getString(R.string.cat_list_view_email_text));
      ViewCompat.setTransitionName(holder.container, holder.emailTitle.toString());
      updateEmailSelected(holder);
    }

    @Override
    public int getItemCount() {
      return 10;
    }
  }

  /** A simple email data class. */
  static final class EmailData {

    private EmailData() {}

    static final List<Email> emailData =
        Arrays.asList(
            new Email(0L, true),
            new Email(1L, false),
            new Email(2L, false),
            new Email(3L, false),
            new Email(4L, false),
            new Email(5L, false),
            new Email(6L, false),
            new Email(7L, false),
            new Email(8L, false),
            new Email(9L, false));

    static Email getEmailById(long emailId) {
      for (Email email : emailData) {
        if (email.id == emailId) {
          return email;
        }
      }
      throw new IllegalArgumentException(
          String.format(Locale.US, "Email %d does not exist.", emailId));
    }

    /** Class that represents an email. */
    static class Email {
      private final long id;
      private boolean isSelected;

      Email(long id, boolean isSelected) {
        this.id = id;
        this.isSelected = isSelected;
      }

      /** Returns the email id. */
      public long getEmailId() {
        return id;
      }

      /** Returns whether the email is selected. */
      public boolean isSelected() {
        return isSelected;
      }

      /** Sets the email to be selected or not. */
      public void setSelected(boolean selected) {
        isSelected = selected;
      }
    }
  }
}
