import java.util.UUID;

/**
 * Classe abstrata com os dados comuns de Doador e Instituicao (RN01).
 */
public abstract class Pessoa {
    private final UUID id;
    private String nome;
    private String documento;
    private String contato;

    protected Pessoa(String nome, String documento, String contato) {
        this.id = UUID.randomUUID();
        atualizarDadosBasicos(nome, documento, contato);
    }

    /**
     * Valida tudo antes de alterar qualquer campo: se algo for inválido,
     * os dados anteriores são preservados (RN05 / RNF03).
     */
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

    /** Cada subclasse descreve a si mesma (polimorfismo). */
    public abstract String descricao();

    @Override
    public String toString() {
        return descricao();
    }
}
