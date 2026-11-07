import java.util.UUID;

public abstract class Pessoa implements Identificavel {
    private final UUID id;
    private String nome;
    private String documento;
    private String contato;

    protected Pessoa(String nome, String documento, String contato) {
        this.id = UUID.randomUUID();
        atualizarDadosBasicos(nome, documento, contato);
    }

    protected final void atualizarDadosBasicos(String nome, String documento, String contato) {
        String n = exigirTexto(nome, "Nome");
        String d = exigirTexto(documento, "Documento");
        String c = exigirTexto(contato, "Contato");
        this.nome = n;
        this.documento = d;
        this.contato = c;
    }

    protected static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException(campo + " é obrigatório.");
        }
        return valor.trim();
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDocumento() {
        return documento;
    }

    public String getContato() {
        return contato;
    }

    public abstract String descricao();

    @Override
    public String toString() {
        return descricao();
    }
}
