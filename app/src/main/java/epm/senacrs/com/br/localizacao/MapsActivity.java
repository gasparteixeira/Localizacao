package epm.senacrs.com.br.localizacao;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import epm.senacrs.com.br.localizacao.estrutura.Directions;
import epm.senacrs.com.br.localizacao.estrutura.Legs;
import epm.senacrs.com.br.localizacao.estrutura.Routes;
import epm.senacrs.com.br.localizacao.estrutura.Step;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public MyInterfaceRetrofit service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void atualizarMapa(View view) {
        EditText origem = (EditText) findViewById(R.id.input_origem);
        EditText destino = (EditText) findViewById(R.id.input_destino);

        Log.i("origem", origem.getText().toString());
        Log.i("destino", destino.getText().toString());


        OkHttpClient client = new OkHttpClient.Builder().addInterceptor( new MapsInterceptor() ).build();

        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        service = retrofit.create(MyInterfaceRetrofit.class);
        Call<Directions> call = service.getCaminho(origem.getText().toString(), destino.getText().toString(), this.getString(R.string.google_maps_key_server));
        call.enqueue(
                new Callback<Directions>() {
                    @Override
                    public void onResponse(Call<Directions> call, Response<Directions> response) {

                         if(response.body().status.equals("OK")) {

                             for(Routes route:response.body().routes) {
                                 for(Legs leg: route.legs) {
                                     for(Step step:leg.steps) {
                                         PolylineOptions linha = new PolylineOptions();
                                         linha.add(new LatLng(step.start_location.lat, step.start_location.lng));
                                         linha.add(new LatLng(step.end_location.lat, step.end_location.lng));
                                         linha.color(Color.BLUE);
                                         linha.width(3);
                                         mMap.addPolyline(linha);
                                     }
                                 }
                                 LatLngBounds llb = new LatLngBounds.Builder()
                                         .include(new LatLng(route.bounds.northeast.lat, route.bounds.northeast.lng))
                                         .include(new LatLng(route.bounds.southwest.lat, route.bounds.southwest.lng))
                                         .build();
                                 mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(llb, 50));
                             }

                         } else {
                             Toast.makeText(getBaseContext(), "Sem rotas", Toast.LENGTH_LONG).show();
                         }
                    }

                    @Override
                    public void onFailure(Call<Directions> call, Throwable t) {
                        Toast.makeText(getBaseContext(), "Erro  no retorno do servico", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}
