package android.support.design.stateful;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.SimpleArrayMap;
import android.view.View.BaseSavedState;

/**
 * Class for widgets that want to save and restore their own state in {@link
 * android.view.View#onSaveInstanceState()}. Supports widgets whose state is composed or delegated
 * out to multiple components.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ExtendableSavedState extends BaseSavedState {

  public final SimpleArrayMap<String, Bundle> extendableStates;

  public ExtendableSavedState(Parcelable superState) {
    super(superState);
    extendableStates = new SimpleArrayMap<>();
  }

  private ExtendableSavedState(Parcel in) {
    super(in);

    int size = in.readInt();

    String[] keys = new String[size];
    in.readStringArray(keys);

    Bundle[] states = new Bundle[size];
    in.readTypedArray(states, Bundle.CREATOR);

    extendableStates = new SimpleArrayMap<>(size);
    for (int i = 0; i < size; i++) {
      extendableStates.put(keys[i], states[i]);
    }
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    super.writeToParcel(out, flags);

    int size = extendableStates.size();
    out.writeInt(size);

    String[] keys = new String[size];
    Bundle[] states = new Bundle[size];

    for (int i = 0; i < size; i++) {
      keys[i] = extendableStates.keyAt(i);
      states[i] = extendableStates.valueAt(i);
    }

    out.writeStringArray(keys);
    out.writeTypedArray(states, 0);
  }

  @Override
  public String toString() {
    return "ExtendableSavedState{"
        + Integer.toHexString(System.identityHashCode(this))
        + " states="
        + extendableStates
        + "}";
  }

  public static final Parcelable.Creator<ExtendableSavedState> CREATOR =
      new Parcelable.Creator<ExtendableSavedState>() {

        @Override
        public ExtendableSavedState createFromParcel(Parcel in) {
          return new ExtendableSavedState(in);
        }

        @Override
        public ExtendableSavedState[] newArray(int size) {
          return new ExtendableSavedState[size];
        }
      };
}
