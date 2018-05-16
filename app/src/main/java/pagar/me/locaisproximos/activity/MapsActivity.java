package pagar.me.locaisproximos.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import pagar.me.locaisproximos.R;
import pagar.me.locaisproximos.retrofit.Common;
import pagar.me.locaisproximos.retrofit.IGoogleAPIService;
import pagar.me.locaisproximos.model.Estabelecimento;
import pagar.me.locaisproximos.model.Results;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Activity responsável por renderizar o Mapa
 *  Esta activity contém uma SearchView para pesquisar os locais
 *
 *  Date 09/05/2018
 *  @author Lucas Ferreira
 */
public class MapsActivity
        extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Maps
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location ultimaLocalizacao;
    private Marker marker;
    private LocationRequest locationRequest;

    //Var
    private double latitude, logitude;
    private static final int CODIGO_PERMISSAO = 9999;

    //Classes
    private IGoogleAPIService iGoogleAPIService;

    //Widgets
    private Toolbar toolbar;
    private MaterialSearchView searchView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Inicia os componentes
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchViewId);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Locais");
        setSupportActionBar(toolbar);

        //Inicia o Serviço
        iGoogleAPIService = Common.getGoogleAPIService();

        //Checa a permissão em tempo real
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checaPermissaoLocalizacao();
        }
    }

    /**
     * Método responsável por buscar estabelecimento através de uma palavra chave
     *
     * @param estabelecimento
     */
    private void buscarEstabelecimentos(String estabelecimento) {
        mMap.clear();
        String url = getUrl(latitude, logitude, estabelecimento);
        iGoogleAPIService.recuperaLugaresProximos(url)
                .enqueue(new Callback<Estabelecimento>() {
                    @Override
                    public void onResponse(Call<Estabelecimento> call, Response<Estabelecimento> response) {
                        if (response.isSuccessful()) {
                            for (int i = 0; i < response.body().getResults().length; i++) {

                                MarkerOptions markerOptions = new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i];

                                double lat = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double lng = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());

                                String nomeEstabelecimento = googlePlace.getName();
                                String endereco = googlePlace.getVicinity();

                                LatLng latLng = new LatLng(lat, lng);
                                markerOptions.position(latLng);
                                markerOptions.title(nomeEstabelecimento);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                //Adiciona marcadores em locais proximos
                                mMap.addMarker(markerOptions);

                                //Movendo a camera
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Estabelecimento> call, Throwable t) {

                    }
                });
    }

    /**
     * Método responsável por montar uma url com os parametros para busca de estabelecimentos
     * @param latitude
     * @param logitude
     * @param estabelecimento
     * @return
     */
    private String getUrl(double latitude, double logitude, String estabelecimento) {
        StringBuilder googlePlaceURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceURL.append("location=" + latitude + "," + logitude);
        googlePlaceURL.append("&radius=" + 1000);
        googlePlaceURL.append("&type=" + estabelecimento);
        googlePlaceURL.append("&language=pt");
        googlePlaceURL.append("&sensor=true");
        googlePlaceURL.append("&key=" + getString(R.string.browser_key));
        Log.i("INFO", googlePlaceURL.toString());
        return googlePlaceURL.toString();
    }

    /**
     * Método responsável por checar se a permissão de localização atual foi consedida
     * @return
     */
    private boolean checaPermissaoLocalizacao() {

        boolean permitido = false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, CODIGO_PERMISSAO);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, CODIGO_PERMISSAO);

                permitido = false;
            }
        } else {
            permitido = true;
        }

        return permitido;
    }

    /**
     * Método responsável por habilitar a localização caso a permissão tenha sido consedida
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CODIGO_PERMISSAO:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if (googleApiClient != null){
                            iniciaGoogleAPIClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(this, "Permissões negadas", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Precisamos iniciar o GooglePlayServices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                iniciaGoogleAPIClient();
                mMap.setMyLocationEnabled(true);

            }
        } else {
            iniciaGoogleAPIClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    // Método responsável por iniciar o Google API Client
    public synchronized void iniciaGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    /**
     * Este método é chamado toda vez que a localização é alterada
     * É neste método que iremos buscar uma nova localização através da searchView
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        ultimaLocalizacao = location;
        if (marker != null) {
            marker.remove();
        }

        latitude = location.getLatitude();
        logitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, logitude);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Posição atual")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        marker = mMap.addMarker(markerOptions);

        //Movendo a camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarEstabelecimentos(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });


    }

    /**
     * Método responsável por inflar o menu para que possamos adicionar a searchView
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.app_bar_search);
        searchView.setMenuItem(item);

        return true;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        progressBar.setVisibility(View.GONE);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
