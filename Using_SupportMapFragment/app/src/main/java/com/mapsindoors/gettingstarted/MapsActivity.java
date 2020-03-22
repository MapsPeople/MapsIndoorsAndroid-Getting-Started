package com.mapsindoors.gettingstarted;


import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 *
 * @author Jose J Varó - Copyright © 2019 MapsPeople A/S. All rights reserved.
 * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment">SupportMapFragment</a>
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    static final String TAG = MapsActivity.class.getSimpleName();

    // A fixed location over MapsPeople HQ offices
    static final LatLng MAPSPEOPLE_HQ_LOCATION = new LatLng( 57.0579639,9.9504609 );

    // Initial zoom level we'll use to set the camera before we setup MapControl
    static final float MAPSPEOPLE_HQ_INITIAL_ZOOM_LEVEL = 14.0f;

    // Close enough zoom level to see most of our HQ office
    static final float MAPSPEOPLE_HQ_CLOSE_UP_ZOOM_LEVEL = 20.5f;

    // Our current floor (index)
    //
    // A building floor has an index (int) and a name (String) values.
    // The name is mostly used for the UI, etc.
    //
    // - To get a Building's list of floors: myBuilding.getFloors()
    // - To get a Floor's name:              myFloor.getDisplayName()
    // - To get a Floor's index:             myFloor.getZIndex()
    static final int MAPSPEOPLE_HQ_FLOOR_INDEX = 10;


    SupportMapFragment mapFragment;

    // The Google Maps instance
    GoogleMap gMap;

    // MapControl: in a nutshell, it takes care of the visual part of the MapsIndoors SDK such as
    // map marker icons, map interactivity, etc.
    MapControl mapControl;

    // Avoid animating the camera more than once at start
    boolean iVeBeenHereAlready;



    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
        if( mapFragment != null ) {
            mapFragment.getMapAsync( this );
        }
    }

    @Override
    protected void onDestroy()
    {
        // =======================================================================================
        //
        //        IMPORTANT: Cleanup MapControl if we are to kill this activity
        //
        // =======================================================================================
        if( mapControl != null ) {
            mapControl.onDestroy();
            mapControl = null;
        }

        super.onDestroy();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady( @NonNull GoogleMap googleMap )
    {
        gMap = googleMap;

        // If you want to set the camera before MapControl is done initializing, you can do it here
        // In this case, we are giving a known coordinate.
        gMap.moveCamera( CameraUpdateFactory.newLatLngZoom(
                MAPSPEOPLE_HQ_LOCATION,
                MAPSPEOPLE_HQ_INITIAL_ZOOM_LEVEL
        ));

        // From here on, we take over
        setupMapControl();
    }

    /**
     * Sets up MapControl
     */
    void setupMapControl()
    {
        if( (gMap == null) || (mapFragment == null) || (mapFragment.getView() == null) ) {
            return;
        }

        // =======================================================================================
        //
        // Create an instance of MapControl. Currently, we support a SINGLE MapControl instance
        //
        mapControl = new MapControl( this );

        // =======================================================================================
        // Set the GoogleMap object and give the view where MapsIndoors will render its
        // UI elements like the floor selector, etc.
        mapControl.setGoogleMap( gMap, mapFragment.getView() );

        // =======================================================================================
        //
        // An example of setting a marker click listener
        //
        // ==========
        // IMPORTANT:
        // ==========
        //
        //     Before setting a listener to the GoogleMap object FIRST CHECK IF MapControl HAS IT.
        //
        //     If available, you MUST SET THE LISTENERS ON MapControl (as a proxy) so we can catch
        //     those events before forwarding them to the GoogleMap object.
        //
        //     For example, without the camera events MapControl would not be able to render the
        //     floor plans, POIs, etc.
        //
        mapControl.setOnMarkerClickListener( marker -> {
            // Check if the marker we've clicked on is a MPLocation object ...
            final MPLocation loc = mapControl.getLocation( marker );
            if( loc != null ) {
                // ...yes, so just show an info window over it
                marker.showInfoWindow();
            }
            return true;
        });

        // Disable icon clustering
        // This is set from our CMS. It will group overlapping marker icons based on their type
        mapControl.setLocationClusteringEnabled( false );

        // Disable icon overlap default resolution
        // The default behaviour is to, if two marker icons overlap (collide) one will set to
        // hide
        mapControl.setLocationHideOnIconOverlapEnabled( false );

        iVeBeenHereAlready = false;

        // =======================================================================================
        //
        // [ OPTIONAL ] Listens for Locations data availability
        //
        MapsIndoors.addLocationSourceOnStatusChangedListener( ( status, sourceId ) -> {
            if( status == MPLocationSourceStatus.AVAILABLE ) {
                // ------------------------------------------------------------------------------
                // Once here, a call to MapsIndoors.getLocations() will be a successful one
                //
                // Note that this could be invoked MORE THAN ONCE if:
                //
                // - MapsIndoors.synchronizeContent() has been called
                // - A custom Locations Data Source has updated
                // ------------------------------------------------------------------------------
                mapsIndoorsOnLocationsAvailable();
            }
        } );

        // =======================================================================================
        //
        // Initialize MapControl to setup and render the solution data on the map
        //
        mapControl.init( error -> {

            // IMPORTANT: Always check if there was any error after MapControl returns
            if( error == null ) {
                // ------------------------------------------------------------------------------
                // All, but Locations data, is ready here: categories, buildings, app config, etc.
                //
                // For Locations data availability, one should set its status changed listener:
                // MapsIndoors.addLocationSourceOnStatusChangedListener( [callback] );
                // ------------------------------------------------------------------------------
                onMapControlInitIsReady();

            }
            else
            {
                // Prints out the given error object
                if( dbglog.isDeveloperMode() ) {

                    // ---------------------------------------------------------------------------
                    // The MIError error object holds several useful values:
                    //    error.code    -> A unique error identifier
                    //    error.message -> An exception message, etc.
                    //    error.status  -> If the error is network related, this will keep
                    //                     the original response's status code
                    // ---------------------------------------------------------------------------

                    // If MapsIndoors dev mode has been enabled, error.toString() will printout
                    // the above values concatenated.
                    dbglog.LogE( TAG, "MapControl.init ERROR: " + error.toString() );
                }
            }
        } );
    }

    /**
     * Invoked once MapControl.init() has finished
     *
     * All data but Locations is available here
     */
    void onMapControlInitIsReady()
    {
    }

    /**
     * Invoked once the Locations data (POI data) becomes available
     */
    void mapsIndoorsOnLocationsAvailable()
    {
        if( !iVeBeenHereAlready ) {
            iVeBeenHereAlready = true;

            runOnUiThread( () -> {
                // #######################################################################################
                //
                //    IMPORTANT: Set the first floor you want to start with
                //
                //  NO INDOOR TILES (FLOOR PLANS) WILL BE SHOWN UNLESS YOU CALL THIS METHOD WITH A VALID
                //  FLOOR INDEX
                //
                //  The floor selector/picker is shown when the camera gets close to the building, at
                //  around zoom level 18 (as of SDK version 3.1.3-beta-4)
                //
                // #######################################################################################
                mapControl.selectFloor( MAPSPEOPLE_HQ_FLOOR_INDEX );

                // Animate the camera to our HQ office
                gMap.animateCamera( CameraUpdateFactory.newLatLngZoom(
                        MAPSPEOPLE_HQ_LOCATION,
                        MAPSPEOPLE_HQ_CLOSE_UP_ZOOM_LEVEL
                ));
            } );
        }

/*
        // ---------------------------------------------------------------------------------------
        //
        // Get ALL locations:
        //
        final List<MPLocation> allLocations = MapsIndoors.getLocations();
*/

/*
        // ---------------------------------------------------------------------------------------
        //
        // Search locations (on device search):
        //
        // Setup a search query object
        final MPQuery q = new MPQuery.Builder().

                // Restrict searches to the "roomId" property
                addQueryProperty( LocationPropertyNames.ROOM_ID ).

                // Restrict searches to the location's name
                addQueryProperty( LocationPropertyNames.NAME ).

                // Restrict searches to a set of properties (currently only roomId and name are processed)
                setQueryProperties( Arrays.asList(LocationPropertyNames.ROOM_ID, LocationPropertyNames.NAME) ).

                // From a specific coordinate - Using raw lat/lng values
                setNear( 57.0579391,9.9503805 ).

                // ... or from a specific coordinate - Using a GMS LatLng object
                setNear( new LatLng( 57.0579391,9.9503805 ) ).

                // ... or from a specific coordinate - Using a MapsIndoors Point object
                setNear( new Point( 57.0579391,9.9503805 ) ).

                // ... or from a specific coordinate + floorIndex - Using a MapsIndoors Point object
                setNear( new Point( 57.0579391,9.9503805, 0 ) ).

                // Set the query string
                // Examples when using our MapsPeople demo API Key could be:
                // - "Bike shed" (a name)
                // - "Kitchen"   (a name)
                // - "1.27.04"   (a roomId of a storage room)
                setQuery( "[ Your Query String ]" ).

                build();

        // Setup a search filter object
        final MPFilter f = new MPFilter.Builder().

                // Only meeting rooms
                setCategories( Collections.singletonList( "MEETING_ROOM" ) ).

                // Only locations that are at the ground floor
                // This should be a valid floor index value
                setFloorIndex( Floor.DEFAULT_GROUND_FLOOR_INDEX ).

                // Limit the result count to 1
                setTake( 1 ).

                // ...and so on...
                build();

        // Run the async search
        MapsIndoors.getLocationsAsync( q, f, ( locations, error ) -> {

            // Always check for errors
            if( error == null ) {

                // ---------------------------------------------------------------------------------------
                // Your search results, if any, here
                // ---------------------------------------------------------------------------------------
            }
        });
 */
    }
}
