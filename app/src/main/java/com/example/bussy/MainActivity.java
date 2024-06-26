package com.example.bussy;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener {

    //Editar AQUI


    String api = "AIzaSyA23EgCFfnT-Ag74lG__a3VsYUscHRe5bs";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    SearchView svBusqueda;
    ImageButton btnprueba;
    GoogleMap mMap;
    private Marker currentMarker, marcador2;
    private Marker Tecnl, Tecnl2;
    private Handler handler;
    private Runnable runnable;
    private static final int DELAY = 1000;

    String texto11;


    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

    String origen;
    private LatLng destino;

    String origenLat;
    private LatLng direccionLatLng;

    String origenAlt;
    private String destinoString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        btnprueba = findViewById(R.id.btncambio);
        svBusqueda = findViewById(R.id.BarraBusquedaMain);


        ////////////////////////API AUTO COMPLETADO






        //////////////////////////TERMINA API AUTO COMPLETADO


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // 5 segundos en milisegundos

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                // Aquí puedes utilizar la ubicación actual (location)
                origenLat = String.valueOf(location.getLatitude());
                origenAlt = String.valueOf(location.getLongitude());
                origen = origenLat + " , " + origenAlt;

            }
        };
// TODO:  ==============================AQUI INIICIA SEARCHVIEW METODOS ==============================


        svBusqueda.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Convertir el texto a LatLng
                destino = convertirDireccionALatLng(query);
                if (destino != null) {
                    // Convertir LatLng a String
                     destinoString = destino.toString();
                    Toast.makeText(MainActivity.this, "Destino: " + destinoString, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No se pudo encontrar la ubicación.", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


// TODO:  ==============================AQUI INIICIA SEARCHVIEW METODOS AUTOCOMPLETADO==============================


// TODO: ==============================AQUI ACABA SEARCHVIEW METODOS==============================


        Button buttonGenerateRoute = findViewById(R.id.button_generate_route);

        buttonGenerateRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRoute();
            }
        });


        btnprueba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí defines la pantalla a la que quieres cambiar

                Intent intent = new Intent(MainActivity.this, Informacion.class);
                startActivity(intent);

            }
        });



        //txtEditarTexto = findViewById(R.id.txtEditarTexto);



        //ESTAS DOS LINEAS LLAMAN AL METODO "onMapReady"
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //ESTO CONFIGURA LA BARRA DE BUESQUEDA MAIN
        svBusqueda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                svBusqueda.setIconified(false); // Expandir el SearchView
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    /////////////SEARCHVIEW METODOS
    private LatLng convertirDireccionALatLng(String direccion) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(direccion, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    ///////////FIN SEARCHVIEW METODOS


    public void generateRoute() {
        if (mMap == null) {
            Toast.makeText(this, "Mapa no listo todavía. Intenta de nuevo más tarde.", Toast.LENGTH_SHORT).show();
            return;
        }


        DirectionsApiHelper directionsApiHelper = new DirectionsApiHelper(this);
        directionsApiHelper.requestDirections(api, origen,destinoString);
    }

    private String Lat, Long;
    LatLng UbicacionReal;

    public void onLocationChanged(Location location) {
        // Aquí recibes las actualizaciones de ubicación en tiempo real
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        UbicacionReal = new LatLng(location.getLatitude(), location.getLongitude());

        Lat = String.valueOf(latitude);
        Long = String.valueOf(longitude);
    }


    public void displayRoute(JSONObject response) {
        Log.d("DirectionsResponse", response.toString());
        try {
            JSONArray routes = response.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");

                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(PolyUtil.decode(encodedPolyline))
                        .width(5);


                mMap.addPolyline(polylineOptions);

                // Ajustar la cámara para mostrar toda la ruta
                JSONObject bounds = route.getJSONObject("bounds");
                JSONObject northeast = bounds.getJSONObject("northeast");
                JSONObject southwest = bounds.getJSONObject("southwest");
                LatLng northeastLatLng = new LatLng(northeast.getDouble("lat"), northeast.getDouble("lng"));
                LatLng southwestLatLng = new LatLng(southwest.getDouble("lat"), southwest.getDouble("lng"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        new LatLngBounds(southwestLatLng, northeastLatLng), 100));
            } else {
                Toast.makeText(this, "No se encontraron rutas.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error al obtener la ruta.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //AJUSTA EL TAMAñO DEL MARCADOR
        int height = 50;
        int width = 50;
        //R.drawable.n ES LA UBICACION DE LA IMAGEN
        @SuppressLint("UseCompatLoadingForDrawables")
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.n);
        Bitmap b = bitmapdraw.getBitmap();
        //SMALLMARKER ES EL NOMBRE DE LA VARIBALE DEL MARCADOR
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        //LLAMA A LAS CLASES ONMAPCLICK Y CONMAPLONG
        mMap = googleMap;
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);

        //OBTIENE LAS CORDENADAS DONDE SE ABRIRA EL MAPA
        LatLng TecNL = new LatLng(25.664895, -100.245138);
        LatLng TecNL2 = new LatLng(25.66470371562547, -100.24338602091686);

        //CREA UN MARCADOR
        Tecnl = mMap.addMarker(new MarkerOptions().position(TecNL).title(getString(R.string.Ruta_223)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .flat(true).snippet(getString(R.string.Click_aqui_para_mas_informacion)));
        googleMap.setOnInfoWindowClickListener(this);
        Tecnl2 = mMap.addMarker(new MarkerOptions().position(TecNL2).title(getString(R.string.Ruta_224)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .flat(true).snippet(getString(R.string.Click_aqui_para_mas_informacion)));
        googleMap.setOnInfoWindowClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(TecNL));
        mMap.setMinZoomPreference(12.0f); // Zoom mínimo permitido
        mMap.setMaxZoomPreference(16.0f); // Zoom máximo permitido

        //UBICACION TIEMPO REAL
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        mMap.setOnMarkerClickListener(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Aquí puedes manejar la lógica para cambiar de ventana (actividad)
               Intent intent = new Intent(MainActivity.this, MapaExtendido.class);
              startActivity(intent);
            }
        });
    }


    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. verifica si se concedio permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // Cambiar el icono de la posición del usuario
            if (mMap != null) {
                try {
                    // Obtiene el icono personalizado
                    int height = 70;
                    int width = 70;
                    // R.drawable.iconlive ES LA UBICACION DE LA IMAGEN
                    @SuppressLint("UseCompatLoadingForDrawables")
                    BitmapDrawable customIcon = (BitmapDrawable) getResources().getDrawable(R.drawable.iconolive);
                    Bitmap b = customIcon.getBitmap();
                    // SMALLMARKER ES EL NOMBRE DE LA VARIABLE DEL MARCADOR
                    Bitmap smallcustomIcon = Bitmap.createScaledBitmap(b, width, height, false);

                    // Inicia la actualización periódica de la ubicación
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            updateLastKnownLocation(smallcustomIcon);
                            handler.postDelayed(this, 5000); // 5000 milisegundos = 5 segundos
                        }
                    };
                    handler.postDelayed(runnable, 5000); // Inicia la actualización después de 5 segundos
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            return;
        }
    }

    private void updateLastKnownLocation(Bitmap smallcustomIcon) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Crea un objeto LocationManager para obtener la última ubicación conocida
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                // Si ya hay un marcador, elimínalo
                if (marcador2 != null) {
                    marcador2.remove();
                }
                // Crea un marcador en la última ubicación conocida con el icono personalizado
                LatLng conocida = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                marcador2 = mMap.addMarker(new MarkerOptions()
                        .position(conocida)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallcustomIcon)));
            }
        }

    }


    public int Datos() {
        Random rand = new Random();
        int randomNumber = rand.nextInt(51); // Genera un número aleatorio del 0 al 50
        return randomNumber;
    }


    @SuppressLint("SetTextI18n")
    public void onInfoWindowClick(Marker marker) {

        if (marker.getTitle().equals(getString(R.string.Ruta_223))) {
            Toast.makeText(this, "Info window clicked 223",
                    Toast.LENGTH_SHORT).show();
        }

        if (marker.getTitle().equals(getString(R.string.Ruta_224))) {
            Toast.makeText(this, "Info window clicked 224",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(false);

            if (currentMarker != null) {
                currentMarker.remove(); // Eliminar el marcador actual si existe
            }
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.Nuevo_Marcador)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }


    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    private float calcularDistancia(LatLng posicion1, LatLng posicion2) {
        Location location1 = new Location("");
        location1.setLatitude(posicion1.latitude);
        location1.setLongitude(posicion1.longitude);

        Location location2 = new Location("");
        location2.setLatitude(posicion2.latitude);
        location2.setLongitude(posicion2.longitude);

        return location1.distanceTo(location2);
    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }














    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        // Devuelve false para permitir que el mapa maneje el clic en el marcador
        return false;

    }



}