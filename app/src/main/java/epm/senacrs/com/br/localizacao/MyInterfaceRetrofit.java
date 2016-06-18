package epm.senacrs.com.br.localizacao;

import android.widget.EditText;

import epm.senacrs.com.br.localizacao.estrutura.Directions;
import epm.senacrs.com.br.localizacao.estrutura.Routes;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by 631110317 on 18/06/16.
 */
public interface MyInterfaceRetrofit {

    @GET("maps/api/directions/json")
    Call<Directions> getCaminho(@Query("origin") String origin, @Query("destination") String destination, @Query("key") String key);

}
