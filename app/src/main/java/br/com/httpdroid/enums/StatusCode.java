package br.com.httpdroid.enums;

/**
 * Criado por Felipe Samuel em 08/01/2019.
 */
public enum StatusCode {
    DELETADO(204),
    CRIADO(201),
    ACEITO(202),
    OK(200),

    MAU_REQUISICAO(400),
    NAO_AUTENTICADO(401),
    NAO_AUTORIZADO(403),
    NAO_ENCONTRADO(404),
    METODO_NAO_PERMITIDO(405),
    MIDIA_NAO_SUPORTADA(415),
    ERRO_INTERNO_NO_SERVIDOR(500);

    private int code;

    StatusCode(int code){
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }
}
