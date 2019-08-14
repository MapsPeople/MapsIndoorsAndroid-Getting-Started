# Getting Started

A very basic project showing how to initialize our library and render a solution on the map

## Usage

Clone the project and open it in Android Studio.

## Requirements

First, you need to generate a google maps key for android (follow the instructions here: [link](https://developers.google.com/maps/documentation/android-sdk/signup)). Once you get the key, open the file "[google_maps_api.xml](./app/src/main/res/values/google_maps_api.xml)" (under  "res/values"), then replace __"YOUR_KEY_HERE"__ with it.

## Projects

* **Using MapView**: A basic MapsIndoors project were a `View` (a [MapView](https://developers.google.com/android/reference/com/google/android/gms/maps/MapView)) is used to embed the Google Map in the layout.
* **Using SupportMapFragment**: A basic MapsIndoors project were a `Fragment` (a [SupportMapFragment](https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment)) is used to embed the Google Map in the layout.

## Authors

* Jose J Varó
* Mohammed Amine Naimi

## License

MapsIndoors SDK is released under a commercial license. The MapsIndoors Demo Samples code is released under [The MIT LICENSE](LICENSE).
