package com.napas.landmarkremark.ui.landmark;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.napas.landmarkremark.R;
import com.napas.landmarkremark.model.Landmark;
import com.napas.landmarkremark.ui.base.BaseFragment;
import com.napas.landmarkremark.util.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLandmarkFragment extends BaseFragment {

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.et_note)
    EditText etNote;
    @BindView(R.id.btn_add)
    Button btnAdd;
    private ILandmarkListener mListener;

    private Landmark mCurrentPlace;

    public static AddLandmarkFragment newInstance(Landmark currentPlace) {
        AddLandmarkFragment fragment = new AddLandmarkFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_CURRENT_PLACE, currentPlace);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ILandmarkListener) {
            mListener = (ILandmarkListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement ILandmarkListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mCurrentPlace = bundle.getParcelable(Constants.KEY_CURRENT_PLACE);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_landmark, container, false);
        setUnBinder(ButterKnife.bind(this, view));
        initView();
        return view;
    }

    @OnClick(R.id.btn_add)
    public void onBtnAddClicked() {
        hideKeyboard();
        // input validation
        String note = etNote.getText().toString().trim();
        if (TextUtils.isEmpty(note)) {
            Toast.makeText(getContext(), R.string.please_enter_your_note, Toast.LENGTH_SHORT).show();
            return;
        }

        // add landmark to database
        mCurrentPlace.setNote(note);
        addLandmarkToDatabase(mCurrentPlace);

        // go back to the main page
        mListener.onLandmarkAdded();
    }

    private void initView() {
        if (mCurrentPlace == null) return;

        tvName.setText(mCurrentPlace.getName());
        tvAddress.setText(mCurrentPlace.getAddress());
    }

    private void addLandmarkToDatabase(Landmark landmark) {
        // get reference to 'landmarks' node
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.KEY_LANDMARKS);
        // create object id
        String landmarkId = dbRef.push().getKey();
        // pushing landmark to 'landmarks' node using the landmarkId
        dbRef.child(landmarkId).setValue(landmark);
    }
}
