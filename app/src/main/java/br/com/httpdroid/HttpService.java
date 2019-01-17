package br.com.httpdroid;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import br.com.httpdroid.enums.StatusCode;
import br.com.httpdroid.interfaces.IMethods;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Criado por Felipe Samuel em 07/01/2019.
 */
public class HttpService<Object> implements IMethods<Object> {

    private String baseURL = "";
    private String endPoint = "";

    /**
     * Ao setar uma url a esta string, o efeito da variável
     *
     * @see this#baseURL é anulado
     */
    private String customUrl = "";


    private OkHttpClient http;
    private MediaType mediaType;
    private String responseStringReturned;
    /**
     * Propriedade Gson que será utilizada para realizar a serialização dos objetos
     */
    private Gson gson;

    /**
     * Nome do campo a ser enviado no método de upload
     */
    private String fileFieldName = "arquivo";

    /**
     * Propriedade "ID" do objeto passado como parâmetro no método de PUT
     */
    private String propertyIdName = "id";
    private boolean useInternalPropertyId = true;


    /**
     * Objeto de resposta da requisição
     */
    private Response response;
    /**
     * Este parametro requer bastante atenção, pois será o tipo de retorno que a classe usará
     * para converter a resposta vinda do servidor. Pode ser uma lista, objeto, ou qualquer outro
     * retorno através da classe Type
     */
    private Type returnType;

    /**
     * Todos os headers que serão enviados na requisição
     */
    private Map<String, String> headers = new HashMap<>();
    /**
     * Filtros utilizados na requisição
     */
    private Map<String, String> filters;

    /**
     * Campos adicionais a serem enviados junto ao upload de arquivo
     */
    private Map<String, String> aditionalFormFields;

    /**
     * Parametro de autenticação do próprio OkHttp
     */
    private Authenticator authenticator;
    /**
     * Parâmetro de interceptação do próprio OkHttp
     */
    private Interceptor interceptor;
    /**
     * Parâmetro de cache do próprio OkHttp
     */
    private Cache cache;

    /**
     * Tempo máximo para a conexão
     */
    private int connectTimeout = 0;

    /**
     * Tempo máximo para a escrita durante a conexão
     */
    private int writeTimeout = 0;
    /**
     * Tempo máximo para a leitura durante a conexão
     */
    private int readTimeout = 0;

    /**
     * Unidade de medida do tempo para a conexão
     */
    private TimeUnit timeUnit;

    /**
     * Executa a requisição novamente após ocorrer uma falha
     */
    private boolean retryEnabled = false;

    /**
     * Inicializa todos os objetos da classe, e cria uma instancia de OkHttp
     * Método obrigatório!
     */
    public HttpService<Object> build() {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();

        if (this.connectTimeout != 0) {
            okHttpBuilder.connectTimeout(this.connectTimeout, this.timeUnit);
        }
        if (this.writeTimeout != 0) {
            okHttpBuilder.writeTimeout(this.writeTimeout, this.timeUnit);
        }
        if (this.readTimeout != 0) {
            okHttpBuilder.readTimeout(this.readTimeout, this.timeUnit);
        }
        if (this.cache != null) {
            okHttpBuilder.cache(this.cache);
        }
        okHttpBuilder.retryOnConnectionFailure(this.retryEnabled);

        if (this.authenticator != null) {
            okHttpBuilder.authenticator(this.authenticator);
        }
        if (this.interceptor != null) {
            okHttpBuilder.addInterceptor(this.interceptor);
        } else {
            okHttpBuilder.addInterceptor(chain -> {
                Request requestOriginal = chain.request();

                Request.Builder requestBuilder = requestOriginal
                        .newBuilder()
                        .method(requestOriginal.method(), requestOriginal.body());

                if (filters != null) {
                    requestBuilder.url(getUrlWithFilters());
                }
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.header(entry.getKey(), entry.getValue());
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }

        if (this.mediaType == null) {
            this.mediaType = getDefaultMediaType();
        }
        if (this.gson == null) {
            this.gson = getDefaultGsonConverter();
        }

        this.http = okHttpBuilder.build();
        return this;
    }

    public HttpService<Object> baseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    public HttpService<Object> addCustomUrl(String customUrl) {
        this.customUrl = customUrl;
        return this;
    }

    public HttpService<Object> endPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public HttpService<Object> addReturnType(Type type) {
        this.returnType = type;
        return this;
    }

    public HttpService<Object> addMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public HttpService<Object> setIdPropertyName(String propertyName) {
        this.propertyIdName = propertyName;
        return this;
    }

    public HttpService<Object> addGsonConverter(Gson gson) {
        this.gson = gson;
        return this;
    }

    public HttpService<Object> addAuthorizationHeader(String authorization) {
        this.headers.put("Authorization", authorization);
        return this;
    }

    public HttpService<Object> addHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpService<Object> addFilters(Map<String, String> filters) {
        this.filters = filters;
        return this;
    }

    public HttpService<Object> addAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    public HttpService<Object> addInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public HttpService<Object> addConnectionTimeout(int timeout, TimeUnit timeUnit) {
        this.connectTimeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    public HttpService<Object> addWriteTimeout(int timeout, TimeUnit timeUnit) {
        this.writeTimeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    public HttpService<Object> addReadTimeout(int timeout, TimeUnit timeUnit) {
        this.readTimeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    public HttpService<Object> addCache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public HttpService<Object> isRetryEnabled(boolean retryEnable) {
        this.retryEnabled = retryEnable;
        return this;
    }

    public HttpService<Object> isInternalPropertyIdEnabled(boolean enabled) {
        this.useInternalPropertyId = false;
        return this;
    }

    /**
     * For upload files only
     */
    public HttpService<Object> addFileFieldName(String fileFieldName) {
        this.fileFieldName = fileFieldName;
        return this;
    }

    public HttpService<Object> addAditionalFormFields(Map<String, String> map) {
        this.aditionalFormFields = map;
        return this;
    }


    /**
     * Retorna um objeto a partir do id
     */
    @Override
    public Object get(int id) throws IOException {
        Request request = new Request.Builder()
                .url(this.getUrlWithIdParam(id))
                .build();

        this.response = http.newCall(request)
                .execute();
        this.responseStringReturned = this.response.body().string();
        return this.gson.fromJson(this.responseStringReturned, this.returnType);
    }

    /**
     * Retorna uma lista de objetos, levando em consideração os filtros adicionados
     */
    @Override
    public ArrayList<Object> get() throws IOException {
        Request request = new Request.Builder()
                .url(this.getUrl())
                .build();

        this.response = http.newCall(request)
                .execute();

        this.responseStringReturned = this.response.body().string();
        return this.gson.fromJson(this.responseStringReturned, this.returnType);
    }

    @Override
    public Object post(Object object) throws IOException {
        String json = this.gson.toJson(object);
        RequestBody body = RequestBody.create(this.mediaType, json);

        Request request = new Request.Builder()
                .url(this.getUrl())
                .post(body)
                .build();

        this.response = http.newCall(request)
                .execute();
        this.responseStringReturned = this.response.body().string();
        return this.gson.fromJson(this.responseStringReturned, this.returnType);
    }

    @Override
    public Object put(Object object) throws IOException, NoSuchFieldException, IllegalAccessException {
        String json = this.gson.toJson(object);

        String url = this.getUrl();
        if (this.useInternalPropertyId) {
            int id = getIntProperty(object);
            if (id != 0) {
                url = this.getUrlWithIdParam(id);
            }
        }


        RequestBody body = RequestBody.create(this.mediaType, json);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        this.response = http.newCall(request)
                .execute();
        this.responseStringReturned = this.response.body().string();
        return this.gson.fromJson(this.responseStringReturned, this.returnType);

    }

    @Override
    public boolean delete(int id) throws IOException {
        Request request = new Request.Builder()
                .url(this.getUrlWithIdParam(id))
                .delete()
                .build();

        this.response = http.newCall(request)
                .execute();
        this.responseStringReturned = this.response.body().string();
        if (this.response.code() == StatusCode.DELETADO.getCode()) {
            return true;
        } else {
            return this.gson.fromJson(this.responseStringReturned, this.returnType);
        }
    }

    @Override
    public Object upload(File file) throws IOException {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(this.fileFieldName, file.getName(),
                        RequestBody.create(this.mediaType, file));

        if (this.aditionalFormFields != null) {
            for (Map.Entry<String, String> field : this.aditionalFormFields.entrySet()) {
                bodyBuilder.addFormDataPart(field.getKey(), field.getValue());
            }
        }
        RequestBody requestBody = bodyBuilder.build();
        Request request = new Request.Builder()
                .url(this.getUrl())
                .post(requestBody)
                .build();


        this.response = this.http
                .newCall(request)
                .execute();
        this.responseStringReturned = this.response.body().string();
        return this.gson.fromJson(this.responseStringReturned, this.returnType);
    }

    private int getIntProperty(Object object)
            throws NoSuchFieldException, IllegalAccessException {
        int value = 0;

        Field field = object.getClass().getDeclaredField(this.propertyIdName);
        field.setAccessible(true);
        value = (int) field.get(object);
        return value;
    }

    private String getUrl() {
        if (this.isNullOrEmpty(this.customUrl)) {
            if (!this.baseURL.substring(this.baseURL.length() - 1).equals("/")) {
                this.baseURL += "/";
            }
            return this.baseURL + this.endPoint;
        } else {
            return this.customUrl;
        }
    }

    private MediaType getDefaultMediaType() {
        return MediaType.parse("application/json; charset=utf-8");
    }

    private Gson getDefaultGsonConverter() {
        return new Gson();
    }

    private String getUrlWithIdParam(int id) {
        return this.getUrl() + "/" + id;
    }


    /**
     * Converte a resposta da requisição em qualquer outro tipo (Diferente do returnType)
     * passado como parâmetro
     */
    public <T> T convertResponseTo(Type type) {
       return gson.fromJson(this.responseStringReturned, type);
    }

    private HttpUrl getUrlWithFilters() {
        HttpUrl.Builder urlBuilder = HttpUrl
                .parse(getUrl())
                .newBuilder();

        if (filters != null) {
            for (Map.Entry<String, String> filter : filters.entrySet()) {
                urlBuilder.addQueryParameter(filter.getKey(), filter.getValue());
            }
        }
        return urlBuilder.build();
    }

    /**
     * Get Properties
     */
    public int getStatusCode() {
        return this.response.code();
    }

    public Response getResponse() {
        return this.response;
    }

    public boolean isSuccessful() {
        return this.response.isSuccessful();
    }

    public String getResponseString() {
        return this.responseStringReturned;

    }

    public String getHeader(String header, String defaultValue) {
        return this.response.header(header, defaultValue);
    }

    public static boolean isNullOrEmpty(String texto) {
        return texto == null || texto.isEmpty();
    }
}
