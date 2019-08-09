package com.mapsindoors.gettingstarted;


import android.content.res.Configuration;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;

import androidx.annotation.NonNull;

/**
 *
 * @author Jose J Varó - Copyright © 2019 MapsPeople A/S. All rights reserved.
 */
public class Application extends android.app.Application
{
    static final String TAG = Application.class.getSimpleName();


    @Override
    public void onCreate()
    {
        super.onCreate();

        // =======================================================================================
        //
        // Enable MapsIndoors SDK debug mode. This will show some extra info on the console,
        // validate input parameters, etc. etc.
        //
        if( BuildConfig.DEBUG ) {
            dbglog.useDebug( BuildConfig.DEBUG );
            dbglog.setCustomTagPrefix( "MIGettingStarted_" );
        }

        // =======================================================================================
        //
        // This should be called only once
        //
        MapsIndoors.initialize(
                // The Application context
                getApplicationContext(),
                // The MapsIndoors API Key to get the data from
                getString( R.string.mapsindoors_api_key )
        );

        // =======================================================================================
        //
        // Your Google Maps API key: Only needed if you are using our routing feature
        //
        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );

        // =======================================================================================
        // [ OPTIONAL ] Listens for Locations data availability
        //
        // This can be also setup where MapControl.init() is invoked, in case you want to run query
        // locations once the map is loaded
        //
/*
        MapsIndoors.addLocationSourceOnStatusChangedListener( ( status, sourceId ) -> {
            if( status == MPLocationSourceStatus.AVAILABLE ) {

                // Once here, you can expect a call to MapsIndoors.getLocations()
                // will be a successful one

            }
        } );
*/
        // =======================================================================================
        //
        // [ OPTIONAL ] Data preload
        //
        // Note that MapControl.init() calls (internally) this method to ensure that the data shown
        // on the map is the latest available. Data is always cached on the device and only updated
        // from the backend services if necessary.
        //
/*
        MapsIndoors.synchronizeContent( error -> {
            // ---------------------------------------------------------------------------------
            // IMPORTANT: Always check if there was any error
            // ---------------------------------------------------------------------------------
            if( error == null ) {

                // All data has been synchronized but Locations data readiness has to be checked
                // using the MapsIndoors.addLocationSourceOnStatusChangedListener() method

            }
            else
            {
                // Prints out the given error object
                if( dbglog.isDebugMode() ) {

                    // ---------------------------------------------------------------------------
                    // The MIError error object holds several useful values:
                    //    error.code    -> A unique error identifier
                    //    error.message -> An exception message, etc.
                    //    error.status  -> If the error is network related, this will keep
                    //                     the original response's status code
                    // ---------------------------------------------------------------------------

                    // If MapsIndoors dev mode has been enabled, error.toString() will printout
                    // the above values concatenated.
                    dbglog.LogE( TAG, "MapsIndoors.synchronizeContent ERROR: " + error.toString() );
                }
            }
        } );
 */
    }

    @Override
    public void onConfigurationChanged( @NonNull final Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );

        MapsIndoors.onApplicationConfigurationChanged( newConfig );
    }

    @Override
    public void onTrimMemory( final int level )
    {
        super.onTrimMemory( level );

        MapsIndoors.onApplicationTrimMemory( level );
    }
}
