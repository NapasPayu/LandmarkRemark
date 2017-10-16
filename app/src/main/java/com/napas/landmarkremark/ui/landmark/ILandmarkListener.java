package com.napas.landmarkremark.ui.landmark;

import com.napas.landmarkremark.model.Landmark;

public interface ILandmarkListener {

    void onAddClicked(Landmark currentPlace);

    void onLandmarkAdded();

}
