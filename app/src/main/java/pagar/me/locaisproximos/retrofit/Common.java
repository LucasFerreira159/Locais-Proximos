package pagar.me.locaisproximos.retrofit;

import pagar.me.locaisproximos.retrofit.IGoogleAPIService;
import pagar.me.locaisproximos.retrofit.RetrofitClient;

/**
 * Classe responsável para que possamos configurar o caminho da requisição
 */
public class Common {

    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static IGoogleAPIService getGoogleAPIService(){
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
    }
}
