/*
    Park Catcher Montréal
    Find a free parking in the nearest residential street when driving in
    Montréal. A Montréal Open Data project.

    Copyright (C) 2012 Mudar Noufal <mn@mudar.ca>

    This file is part of Park Catcher Montréal.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.mudar.parkcatcher.utils;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import ca.mudar.parkcatcher.Const;

public class LongPressLocationSource implements
        LocationSource,
        OnMapLongClickListener {
    private static final String TAG = "LongPressLocationSource";

    private OnLocationChangedListener mListener;
    /**
     * Flag to keep track of the activity's lifecycle. This is not strictly
     * necessary in this case because onMapLongPress events don't occur while
     * the activity containing the map is paused but is included to demonstrate
     * best practices (e.g., if a background service were to be used).
     */
    private boolean mEnabled;
    private final GoogleMap googleMap;

    public LongPressLocationSource(GoogleMap map) {
        this.googleMap = map;
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        googleMap.setLocationSource(this);

        if (mEnabled && mListener != null) {
            Location location = new Location(Const.LocationProviders.LONG_PRESS);
            location.setLatitude(point.latitude);
            location.setLongitude(point.longitude);
            mListener.onLocationChanged(location);
        }
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        if (!enabled) {
            googleMap.setLocationSource(null);
        }
    }

}
