package pagar.me.locaisproximos.retrofit;

import pagar.me.locaisproximos.model.Estabelecimento;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Esta classe é responsavel por consumir serviço web através da URL passada no parametro utilizando Retrofit
 */
public interface IGoogleAPIService {
    @GET
    Call<Estabelecimento> recuperaLugaresProximos(@Url String url);
}
