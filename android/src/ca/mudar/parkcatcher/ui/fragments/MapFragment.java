
package ca.mudar.parkcatcher.ui.fragments;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import ca.mudar.parkcatcher.Const;
import ca.mudar.parkcatcher.ParkingApp;
import ca.mudar.parkcatcher.R;
import ca.mudar.parkcatcher.models.GeoJSON;
import ca.mudar.parkcatcher.models.Post;
import ca.mudar.parkcatcher.ui.widgets.MyInfoWindowAdapter;
import ca.mudar.parkcatcher.utils.ActivityHelper;
import ca.mudar.parkcatcher.utils.Helper;
import ca.mudar.parkcatcher.utils.LongPressLocationSource;
import ca.mudar.parkcatcher.utils.SearchMessageHandler;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockMapFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class MapFragment extends SherlockMapFragment implements SearchView.OnQueryTextListener,
        SearchMessageHandler.OnMessageHandledListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        // MapListener,
        Runnable {
    protected static final String TAG = "MapFragment";

    protected OnMyLocationChangedListener mListener;

    protected static final int INDEX_OVERLAY_MY_LOCATION = 0x0;
    protected static final int INDEX_OVERLAY_PLACEMARKS = 0x1;

    // protected static final float ZOOM_DEFAULT = 12f;
    private static final float ZOOM_NEAR = 17f;
    private static final float ZOOM_MIN = 16f;
    private static final float HUE_MARKER = 94f;
    private static final float DISTANCE_MARKER_HINT = 50f;

    private GoogleMap mMap;
    private LongPressLocationSource mLongPressLocationSource;

    // protected MapView mMapView;
    // protected MyLocationOverlay mLocationOverlay;
    // protected MapController mMapController;
    protected LocationManager mLocationManager;
    private OnLocationChangedListener onLocationChangedListener;

    // private boolean mIsScrolling = false;
    // private boolean mIsScrollingComputed = false;

    private Location initLocation = null;
    private Location mMapCenter = null;
    private LatLng screenCenter = null;
    private Marker clickedMarker = null;
    private boolean hasHintMarker = true;

    ActivityHelper activityHelper;
    ParkingApp parkingApp;

    final private Handler handler = new SearchMessageHandler(this);
    private JsonAsyncTask jsonAsyncTask = null;

    private MenuItem searchItem;
    private String postalCode;

    /**
     * Container Activity must implement this interface to receive the list item
     * clicks.
     */
    public interface OnMyLocationChangedListener {
        public void OnMyLocationChanged(Location location);

        public void OnMyMapClickListener();
    }

    /**
     * Attach a listener.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMyLocationChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMyLocationChangedListener");
        }
    }

    /**
     * Create map and initialize
     */

    // public void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    //
    //
    // }

    // @Override
    public View onCreateViews(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //
        // mMap = getMap();
        //
        // // mMapView = new MapView(getActivity(), 256); // constructor
        //
        // // mMapView.setMapListener(this);
        //
        // // final Scroller scroller = mMapView.getScroller();
        // //
        // // mMapView.setOnTouchListener(new OnTouchListener() {
        // // @Override
        // // public boolean onTouch(View v, MotionEvent event) {
        // //
        // // switch (event.getAction()) {
        // // case MotionEvent.ACTION_DOWN:
        // // mIsScrolling = true;
        // // Log.i(TAG, "onTouch is ACTION_DOWN");
        // // break;
        // // case MotionEvent.ACTION_UP:
        // // Log.i(TAG, "onTouch is ACTION_UP");
        // //
        // // if (mIsScrolling && scroller != null) {
        // // if (scroller.getDuration() < scroller.timePassed() + 100) {
        // // mIsScrolling = false;
        // // downloadOverlays();
        // // }
        // // }
        // //
        // // break;
        // // case MotionEvent.ACTION_SCROLL:
        // // Log.e(TAG, "onTouch is ACTION_SCROLL");
        // // break;
        // // }
        // //
        // // return false;
        // // }
        // // });
        View root = super.onCreateView(inflater, container, savedInstanceState);
        //
        // activityHelper = ActivityHelper.createInstance(getActivity());
        // parkingApp = (ParkingApp) getActivity().getApplicationContext();
        //
        // setRetainInstance(true);

        // mMap.set

        // CameraUpdate tilted = new CameraUpdate()

        // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        return root;
    }

    /**
     * Initialize map and LocationManager
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        activityHelper = ActivityHelper.createInstance(getActivity());
        parkingApp = (ParkingApp) getActivity().getApplicationContext();
        setUpMapIfNeeded();

        // mMapView.setClickable(true);
        // mMapView.setMultiTouchControls(true);

        // mMapController = mMapView.getController();

        mLocationManager = (LocationManager) getActivity().getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        // CameraPosition cameraPos = mMap.getCameraPosition();
        //
        // final CameraPosition cameraPosFrom = new CameraPosition.Builder()
        // .target(cameraPos.target)
        // .zoom(cameraPos.zoom)
        // .bearing(cameraPos.bearing)
        // .tilt(45f)
        // .build();
        // final CameraPosition cameraPosTo = new CameraPosition.Builder()
        // .target(cameraPos.target)
        // .zoom(cameraPos.zoom)
        // .bearing(cameraPos.bearing)
        // .tilt(0)
        // .build();
        // Log.v(TAG, "onResume moveCamera animateCamera");
        // mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosFrom));
        // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosTo));

        initMap();
    }

    /**
     * Enable user location (GPS) updates on map display.
     */
    @Override
    public void onResume() {
        // mLocationOverlay.enableMyLocation();
        super.onResume();

        setUpMapIfNeeded();

        if (mLongPressLocationSource != null) {
            mLongPressLocationSource.onResume();
        }
        // if (mLocationSource != null) {
        // mLocationSource.onResume();
        // }
    }

    /**
     * Disable user location (GPS) updates on map hide.
     */
    @Override
    public void onPause() {
        // mLocationOverlay.disableMyLocation();
        super.onPause();

        if (mLongPressLocationSource != null) {
            mLongPressLocationSource.onPause();
        }
        // if (mLongPressLocationSource != null) {
        // mLocationSource.onPause();
        // }
    }

    /**
     * Create the fragment's Address Search MenuItem, from code.
     */
    // @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // TWEAK: FC when inflated from XML
        searchItem = (MenuItem) menu.add(getResources().getString(R.string.menu_search));
        searchItem.setIcon(R.drawable.abs__ic_search);
        searchItem.setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        // Create the SearchView
        SearchView searchView = new SearchView(getActivity());
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(this);
        // Collapse when focus lost
        searchView.setOnQueryTextFocusChangeListener(new
                View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            searchItem.collapseActionView();
                        }
                    }
                });

        // Assign the SearchView to the menuItem
        searchItem.setActionView(searchView);
    }

    @Override
    public void onMapClick(LatLng point) {
        // Log.v(TAG, "onMapClick");
        clickedMarker = null;
        mListener.OnMyMapClickListener();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Log.v(TAG, "onMarkerClick");
        clickedMarker = marker;
        mListener.OnMyMapClickListener();
        return false;
    }

    /**
     * Handle the Address Search query
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        postalCode = query;

        searchItem.collapseActionView();
        showDialogProcessing();

        return true;
    }

    /**
     * Required implementation
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        // TODO: use for address suggestions or auto-complete
        return false;
    }

    /**
     * This runnable thread gets the Geocode search value in the background.
     * Results are sent to the handler.
     */
    @Override
    public void run() {

        Location loc = null;
        try {
            /**
             * Geocode search. Takes time and not very reliable!
             */
            loc = Helper.findLocatioFromName(getActivity(), postalCode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Message msg = handler.obtainMessage();
        final Bundle b = new Bundle();

        if (loc == null) {
            /**
             * Send error message to handler.
             */
            b.putInt(Const.KEY_BUNDLE_SEARCH_ADDRESS, Const.BUNDLE_SEARCH_ADDRESS_ERROR);
        }
        else {
            /**
             * Send success message to handler with the found geocoordinates.
             */
            b.putInt(Const.KEY_BUNDLE_SEARCH_ADDRESS, Const.BUNDLE_SEARCH_ADDRESS_SUCCESS);
            b.putDouble(Const.KEY_BUNDLE_ADDRESS_LAT, loc.getLatitude());
            b.putDouble(Const.KEY_BUNDLE_ADDRESS_LNG, loc.getLongitude());
        }
        msg.setData(b);

        handler.sendMessage(msg);
    }

    /**
     * Handle the runnable thread results. This hides the indeterminate progress
     * bar then centers map on found location or displays error message.
     */
    @Override
    public void OnMessageHandled(Message msg) {
        ((SherlockFragmentActivity) getActivity())
                .setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        final Bundle b = msg.getData();

        if (b.getInt(Const.KEY_BUNDLE_SEARCH_ADDRESS) == Const.BUNDLE_SEARCH_ADDRESS_SUCCESS) {
            /**
             * Address is found, center map on location.
             */
            final Location location = new Location(Const.LOCATION_PROVIDER_SEARCH);
            location.setLatitude(b.getDouble(Const.KEY_BUNDLE_ADDRESS_LAT));
            location.setLongitude(b.getDouble(Const.KEY_BUNDLE_ADDRESS_LNG));

            setMapCenterZoomed(location);
        } else {
            /**
             * Address not found! Display error message.
             */
            final String errorMsg = String.format(getResources().getString(
                    R.string.toast_search_error,
                    postalCode));
            ((ParkingApp) getActivity().getApplicationContext()).showToastText(
                    errorMsg, Toast.LENGTH_LONG);
        }
    }

    // @Override
    // public void onLocationChanged(final Location location) {
    // Log.v(TAG, "onLocationChanged");
    // parkingApp.setLocation(location);
    // onLocationChangedListener.onLocationChanged(location);
    //
    // }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (!checkReady()) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = getMap();
            // Check if we were successful in obtaining the map.
            if (checkReady()) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        if (mLongPressLocationSource == null) {
            mLongPressLocationSource = new LongPressLocationSource(mMap);
            mMap.setOnMapLongClickListener(mLongPressLocationSource);
        }

        mMap.setMyLocationEnabled(true);
        mMap.setMapType(MAP_TYPE_NORMAL);

        mMap.setLocationSource(null);

        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(getActivity()));

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mListener.OnMyMapClickListener();
                downloadOverlays();
            }
        });

        UiSettings uiSettings = mMap.getUiSettings();

        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);

    }

    private boolean checkReady() {
        if (mMap == null) {
            parkingApp.showToastText(R.string.toast_map_not_ready, Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }

    private class JsonAsyncTask extends AsyncTask<URL, Void, GeoJSON> {
        @SuppressWarnings("unused")
        protected static final String TAG = "JsonAsyncTask";

        @Override
        protected void onPreExecute() {
            try {
                // Needed to avoid problems when main activity is sent to
                // background
                ((SherlockFragmentActivity) getActivity())
                        .setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected GeoJSON doInBackground(URL... params) {

            URL fetchUrl = params[0];

            // this.screenName is obtained as a user input
            URLConnection urlConnection;
            try {
                urlConnection = fetchUrl.openConnection();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }

            try {
                urlConnection.connect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            JsonReader reader;
            try {
                reader = new JsonReader(
                        new InputStreamReader(urlConnection.getInputStream()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            JsonParser parser = new JsonParser();
            // TODO surround with try/catch
            JsonElement rootElement = parser.parse(reader);

            if (rootElement != null) {
                // Log.v(TAG, "rootElement not null");

                Gson gson = new Gson();
                GeoJSON geoJson = gson.fromJson(rootElement, GeoJSON.class);
                return geoJson;
            }

            return null;
        }

        @Override
        protected void onPostExecute(GeoJSON geoJson) {

            try {
                // Needed to avoid problems when main activity is sent to
                // background
                ((SherlockFragmentActivity) getActivity())
                        .setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return;
            }

            if (geoJson == null || geoJson.getFeatures() == null) {
                return;
            }

            // Log.v(TAG, "geoJson  = " + geoJson.toString());

            final int totalMarkers = geoJson.getFeatures().size();
            // Log.v(TAG, "totalMarkers = " + totalMarkers);

            if (isCancelled()) {
                return;
            }

            mMap.clear();

            if (screenCenter == null || clickedMarker != null) {
                hasHintMarker = false;
            }
            Location locationCenter = new Location(Const.LOCATION_PROVIDER_DEFAULT);
            if (hasHintMarker) {
                locationCenter.setLatitude(screenCenter.latitude);
                locationCenter.setLongitude(screenCenter.longitude);
            }

            for (int i = 0; i < totalMarkers; i++) {

                if (isCancelled()) {
                    return;
                }

                Post post = geoJson.getFeatures().get(i);
                // Log.v(TAG, "post = " + post.toString());

                final Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(post.getLatLng())
                        .snippet(post.getDesc())
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(HUE_MARKER)).visible(true));
                // Log.v(TAG, "myMarker = " + myMarker.getTitle());
                // break;
                if (clickedMarker != null) {
                    if (clickedMarker.getPosition().equals(marker.getPosition())) {
                        marker.showInfoWindow();
                    }
                }
                else if (hasHintMarker) {
                    Location locationMarker = new Location(Const.LOCATION_PROVIDER_DEFAULT);
                    locationMarker.setLatitude(marker.getPosition().latitude);
                    locationMarker.setLongitude(marker.getPosition().longitude);

                    if (locationCenter.distanceTo(locationMarker) < DISTANCE_MARKER_HINT) {
                        marker.showInfoWindow();
                        hasHintMarker = false;
                        clickedMarker = marker;
                    }
                }
            }
        }
    }

    public void downloadOverlaysForced() {
        screenCenter = null;
        downloadOverlays();
    }
    
    private void downloadOverlays() {

        // Log.v(TAG, "current zoom = " + mMap.getCameraPosition().zoom);

        if (mMap.getCameraPosition().zoom < ZOOM_MIN) {
            parkingApp.showToastText(R.string.toast_map_zoom_to_update, Toast.LENGTH_LONG);
            return;
        }

        final Projection projection = mMap.getProjection();

        if (screenCenter == null) {
            screenCenter = mMap.getCameraPosition().target;
        }
        else {
            // TODO: get screen center without repeated use of Projection
            final LatLng cameraTarget = mMap.getCameraPosition().target;
            final Point cameraCenterPoint = projection.toScreenLocation(cameraTarget);
            final int screenWidth = cameraCenterPoint.x * 2;
            final int screenHeight = cameraCenterPoint.y * 2;

            final Point oldCenterPoint = projection.toScreenLocation(screenCenter);

            // Log.v(TAG, "cameraCenterPoint = " + cameraCenterPoint
            // + ". width = " + screenWidth
            // + ". height = " + screenHeight);
            if ((oldCenterPoint.x > screenWidth * 0.75)
                    || (oldCenterPoint.x < screenWidth * 0.25)
                    || (oldCenterPoint.y > screenHeight * 0.75)
                    || (oldCenterPoint.y < screenHeight * 0.25)) {
                // Log.e(TAG, "screenCenter redefined");
                screenCenter = cameraTarget;
            }
            else {
                // Log.v(TAG, "No refresh needed");
                return;
            }

        }



        final VisibleRegion region = projection.getVisibleRegion();
        final LatLngBounds bounds = region.latLngBounds;
        final LatLng SW = bounds.southwest;
        final LatLng NE = bounds.northeast;

        // Log.e(TAG, "new bounds = " + bounds.toString());

        // Get arguments for API call
        final GregorianCalendar parkingCalendar = parkingApp.getParkingCalendar();
        final int day = (parkingCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7
                : parkingCalendar.get(Calendar.DAY_OF_WEEK) - 1);
        final double parkingHour = parkingCalendar.get(Calendar.HOUR_OF_DAY)
                + Math.round(parkingCalendar.get(Calendar.MINUTE) / 0.6) / 100.00d;

        final String fetchUrl = String.format(Const.API_POSTS,
                day, parkingHour, parkingApp.getParkingDuration(),
                NE.latitude, SW.longitude, SW.latitude, NE.longitude);

        // Log.v(TAG, "fetchUrl = " + fetchUrl);

        URL apiURL = null;
        try {
            apiURL = new URL(fetchUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        if (jsonAsyncTask != null) {
            jsonAsyncTask.cancel(true);
        }
        jsonAsyncTask = new JsonAsyncTask();
        jsonAsyncTask.execute(apiURL);

    }

    /**
     * Initialize Map: centre and load rinks
     */
    protected void initMap() {

        // mLocationOverlay = new
        // MyLocationOverlay(getActivity().getApplicationContext(), mMapView);
        // mLocationOverlay.enableCompass();
        // mLocationOverlay.enableMyLocation();
        // mMapView.getOverlays().add(INDEX_OVERLAY_MY_LOCATION,
        // mLocationOverlay);

        // ArrayList<MapMarker> mapMarkers = fetchMapMarkers();
        //
        // Drawable drawable =
        // this.getResources().getDrawable(R.drawable.ic_map_default_marker);
        // MyItemizedOverlay mItemizedOverlay = new MyItemizedOverlay(drawable,
        // mMapView);
        //
        // if (mapMarkers.size() > 0) {
        // for (MapMarker marker : mapMarkers) {
        //
        // MyOverlayItem overlayitem = new MyOverlayItem(marker.geoPoint,
        // marker.name,
        // marker.address, marker.id, marker.extra);
        // mItemizedOverlay.addOverlay(overlayitem);
        // }
        // mMapView.getOverlays().add(INDEX_OVERLAY_PLACEMARKS,
        // mItemizedOverlay);
        // }

    }

    /**
     * Set new map center.
     * 
     * @param mapCenter
     */
    protected void animateToPoint(Location mapCenter) {
        // Log.v(TAG, "animateToPoint ");
        if (mMap == null) {
            return;
        }
        if (mapCenter != null) {
            // Log.v(TAG, "ZOOM_NEAR");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    mapCenter.getLatitude(), mapCenter.getLongitude()), ZOOM_NEAR));
        }
        else {
            // Log.v(TAG, "ZOOM_DEFAULT + initialAnimateToPoint");
            mMap.moveCamera(CameraUpdateFactory.zoomTo(ZOOM_NEAR));
            initialAnimateToPoint();
        }
    }

    /**
     * Initial map center animation on detected user location. If user is more
     * than minimum-distance from the city, center the map on Downtown. Also
     * defines the zoom.
     */
    protected void initialAnimateToPoint() {
        final List<String> enabledProviders = mLocationManager.getProviders(true);

        double coordinates[] = Const.MAPS_DEFAULT_COORDINATES;
        final double lat = coordinates[0];
        final double lng = coordinates[1];

        final Location userLocation = parkingApp.getLocation();
        if (userLocation != null) {
            /**
             * Center on app's user location.
             */
            // Log.v(TAG, "initialAnimateToPoint lat = " +
            // userLocation.getLatitude() + ". Lon = "
            // + userLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
                    userLocation.getLatitude(), userLocation.getLongitude())));
        }
        else {
            /**
             * Center on Downtown.
             */
            // Log.v(TAG, "initialAnimateToPoint. Center on Downtown");
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        }

        if ((mMapCenter == null) && enabledProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            /**
             * Get user current location then display on map.
             */
            // mLocationOverlay.runOnFirstFix(new Runnable() {
            // public void run() {
            // GeoPoint userGeoPoint = mLocationOverlay.getMyLocation();
            //
            // if (mListener != null) {
            // mListener.OnMyLocationChanged(userGeoPoint);
            // }
            //
            // /**
            // * If user is very far from Montreal (> 25km) we center the
            // * map on Downtown.
            // */
            // final float[] resultDistance = new float[1];
            // android.location.Location.distanceBetween(lat, lng,
            // (userGeoPoint.getLatitudeE6() / 1E6),
            // (userGeoPoint.getLongitudeE6() / 1E6), resultDistance);
            //
            // if (resultDistance[0] > Const.MAPS_MIN_DISTANCE) {
            // userGeoPoint = new GeoPoint((int) (lat * 1E6), (int) (lng *
            // 1E6));
            // }
            //
            // mMapCenter = userGeoPoint;
            // if (userGeoPoint != null) {
            // mMapController.animateTo(userGeoPoint);
            // }
            // }
            // });
        }
        else if (mMapCenter != null) {
            /**
             * The AppHelper knows the user location from a previous query, so
             * use the saved value.
             */
            // Log.v(TAG, "initialAnimateToPoint. mMapCenter != null");
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
                    mMapCenter.getLatitude(), mMapCenter.getLongitude())));
        }
    }

    /**
     * Setter for the MapCenter GeoPoint. Centers map on the new location and
     * displays the ViewBallooon.
     * 
     * @param mapCenter The new location
     */
    public void setMapCenter(Location mapCenter) {
        initLocation = mapCenter;
        animateToPoint(mapCenter);

        // if (mapCenter != null) {
        // Overlay overlayPlacemarks =
        // mMapView.getOverlays().get(INDEX_OVERLAY_PLACEMARKS);
        // overlayPlacemarks.onTap(mapCenter, mMapView);
        // }
    }

    /**
     * Sets the map center on the user real location with a near zoom. Used for
     * Tab re-selection.
     */
    public void resetMapCenter() {
        if (!mMap.isMyLocationEnabled()) {
            mMap.setMyLocationEnabled(true);
        }
        // mMap.setLocationSource(mLocationSource);
        mMap.setLocationSource(null);
        setMapCenterZoomed(parkingApp.getLocation());
    }

    /**
     * Sets the map center on the location with a near zoom. Used for Address
     * Search and Tab re-selection.
     */
    private void setMapCenterZoomed(Location mapCenter) {
        // mMapController.setZoom(ZOOM_NEAR);
        setMapCenter(mapCenter);
    }

    /**
     * Called from the activity to handle onKeyUp
     */
    public void searchToggle(boolean isDisplayed) {
        if (isVisible() && isDisplayed) {
            searchItem.expandActionView();
        }
        else {
            searchItem.collapseActionView();
        }
    }

    /**
     * Show the Indeterminate ProgressBar and start the Geocode search thread.
     */
    protected void showDialogProcessing() {
        ((SherlockFragmentActivity) getActivity())
                .setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);

        final Thread thread = new Thread(this);
        thread.start();
    }

}
