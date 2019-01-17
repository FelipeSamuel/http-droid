package br.com.httpdroid.testes;

import android.os.AsyncTask;
import android.util.Log;


import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.httpdroid.HttpService;
import br.com.httpdroid.testes.models.Imagem;
import br.com.httpdroid.testes.models.Post;
import br.com.httpdroid.testes.models.Usuario;
import okhttp3.MediaType;

/**
 * Criado por Felipe Samuel em 07/01/2019.
 */
public class Teste {

    private final String jsonPlaceholderBaseURL = "https://jsonplaceholder.typicode.com/";
    private final String imgurBaseURL = "https://api.imgur.com/3/";
    private final String pathFileTestToUpload = "/sdcard/Download/eu.png";
    private final String imgurID = "5e31b5dddfd3bdc";

    public void async(String metodo) {
        new TesteAsync(metodo).execute();
    }

    public void testeUpload() {

        Map<String, String> camposAdicionais = new HashMap<>();
        camposAdicionais.put("title", new File(this.pathFileTestToUpload).getName());
        camposAdicionais.put("description", "Descrição da Imagem");

        HttpService<Imagem> http = new HttpService<Imagem>()
                .addMediaType(MediaType.get("image/png"))
                .baseURL(this.imgurBaseURL)
                .endPoint("upload")
                .addReturnType(Imagem.class)
                .addFileFieldName("image")
                .addAuthorizationHeader("Client-ID " + this.imgurID)
                .addAditionalFormFields(camposAdicionais)
                .build();

        try {
            Imagem imagem = http.upload(new File(this.pathFileTestToUpload));
            Log.i("RETORNO", "----------------------------------------");
            Log.i("RETORNO", "LINK: " + imagem.getData().getLink());
            Log.i("RETORNO", "TITULO: " + imagem.getData().getTitle());
            Log.i("RETORNO", "DESCRICAO: " + imagem.getData().getDescription());
            Log.i("RETORNO", "TAMANHO: " + String.valueOf(imagem.getData().getSize()));
            Log.i("RETORNO", "----------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testeLista() {
        HttpService<Post> http = new HttpService<Post>()
                .baseURL(this.jsonPlaceholderBaseURL)
                .endPoint("posts")
                .addReturnType(new TypeToken<List<Post>>() {}.getType())
                .build();

        try {
            ArrayList<Post> postList = http.get();
            for (Post o : postList) {
                Log.i("RETORNO", o.getTitle());
                Log.i("RETORNO", o.getBody());
                Log.i("RETORNO", String.valueOf(o.getId()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testeListaFiltros() {
        HashMap<String, String> filtros = new HashMap<>();
        filtros.put("id", "2");

        HttpService<Post> http = new HttpService<Post>()
                .baseURL(this.jsonPlaceholderBaseURL)
                .endPoint("posts")
                .addFilters(filtros)
                .addReturnType(new TypeToken<List<Post>>() {}.getType())
                .build();

        try {
            List<Post> posts = http.get();
            for (Post p : posts) {
                Log.i("RETORNO", "------------POST------------");
                Log.i("RETORNO", "ID: " + p.getId());
                Log.i("RETORNO", "Título: " + p.getTitle());
                Log.i("RETORNO", "Texto: " + p.getBody());
                Log.i("RETORNO", "---------FIM DO POST--------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void testeObjeto() {
        HttpService<Post> http = new HttpService<Post>()
                .baseURL(this.jsonPlaceholderBaseURL)
                .endPoint("posts")
                .addReturnType(Post.class)
                .build();
        try {
            Post o = http.get(3);
            Log.i("RETORNO", o.getTitle());
            Log.i("RETORNO", o.getBody());
            Log.i("RETORNO", String.valueOf(o.getId()));
            Log.i("RETORNO", String.valueOf(http.getStatusCode()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testePost() {
        HttpService<Usuario> http = new HttpService<Usuario>()
                .baseURL(this.imgurBaseURL)
                .endPoint("users")
                .addReturnType(Usuario.class)
                .build();

        Usuario usuarioCadastrar = new Usuario();
        usuarioCadastrar.setName("Felipe Samuel");
        usuarioCadastrar.setJob("Programador");

        try {
            Usuario u = http.post(usuarioCadastrar);
            Log.i("RETORNO", "STATUS: " + String.valueOf(http.getStatusCode()));
            Log.i("RETORNO", "Nome:  " + u.getName());
            Log.i("RETORNO", "Função:  " + u.getJob());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testePut() {
        HttpService<Usuario> http = new HttpService<Usuario>()
                .baseURL(this.imgurBaseURL)
                .endPoint("users")
                .addReturnType(Usuario.class)
                .build();
        Usuario usuarioAtualizar = new Usuario();
        usuarioAtualizar.setId(2);
        usuarioAtualizar.setName("Felipe Samuel da Silva");
        usuarioAtualizar.setJob("Programador");

        try {
            Usuario u = http.put(usuarioAtualizar);
            Log.i("RETORNO", "STATUS: " + String.valueOf(http.getStatusCode()));
            Log.i("RETORNO", "Nome:  " + u.getName());
            Log.i("RETORNO", "Função:  " + u.getJob());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    public void testeDelete() {
        HttpService<Usuario> http = new HttpService<Usuario>()
                .baseURL(this.imgurBaseURL)
                .endPoint("users")
                .addReturnType(Boolean.class)
                .build();

        try {
            boolean u = http.delete(3);
            Log.i("RETORNO", "STATUS: " + String.valueOf(http.getStatusCode()));
            Log.i("RETORNO", "Apagou?  " + u);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void testeComAutorizacaoHeader() {
        HttpService<Usuario> http = new HttpService<Usuario>()
                .baseURL(this.imgurBaseURL)
                .endPoint("users")
                .addAuthorizationHeader("token")
                .addReturnType(Boolean.class)
                .build();
        try {
            boolean u = http.delete(3);
            Log.i("RETORNO", "STATUS: " + String.valueOf(http.getStatusCode()));
            Log.i("RETORNO", "Apagou?  " + u);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testeComHeaders() {

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Foody");

        HttpService<Usuario> http = new HttpService<Usuario>()
                .baseURL(this.imgurBaseURL)
                .endPoint("users")
                .addHeaders(headers)
                .addReturnType(Boolean.class)
                .build();

        try {
            boolean u = http.delete(3);
            Log.i("RETORNO", "STATUS: " + String.valueOf(http.getStatusCode()));
            Log.i("RETORNO", "Apagou?  " + u);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testeComUrlPersonalizada(){
        HttpService<Post> http = new HttpService<Post>()
                .addCustomUrl("https://jsonplaceholder.typicode.com/posts")
                .addReturnType(new TypeToken<List<Post>>() { }.getType())
                .build();

        try {
            ArrayList<Post> postList = http.get();
            for (Post o : postList) {
                Log.i("RETORNO", o.getTitle());
                Log.i("RETORNO", o.getBody());
                Log.i("RETORNO", String.valueOf(o.getId()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class TesteAsync extends AsyncTask<Void, Void, Void> {

        private String metodo;

        public TesteAsync(String metodo) {
            this.metodo = metodo;
        }

        @Override
        protected Void doInBackground(Void... result) {
            switch (this.metodo) {
                case "testeLista":
                    testeLista();
                    break;
                case "testeObjeto":
                    testeObjeto();
                    break;
                case "testePost":
                    testePost();
                    break;
                case "testePut":
                    testePut();
                    break;
                case "testeDelete":
                    testeDelete();
                    break;
                case "testeComAutorizacaoHeader":
                    testeComAutorizacaoHeader();
                    break;
                case "testeComHeaders":
                    testeComHeaders();
                    break;
                case "testeListaFiltros":
                    testeListaFiltros();
                    break;
                case "testeUpload":
                    testeUpload();
                    break;
                case "testeComUrlPersonalizada":
                    testeComUrlPersonalizada();
                    break;

            }
            return null;
        }
    }
}
