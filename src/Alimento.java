import java.util.UUID;

/**
 * Tipo de alimento. A validade e o saldo ficam no Lote, não aqui.
 */
public class Alimento {
    private final UUID id;
    private String nome;
    private Categoria categoria;
    private String unidade;

    public Alimento(String nome, Categoria categoria, String unidade) {
        this.id = UUID.randomUUID();
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Nome do alimento é obrigatório.");
        }
        if (categoria == null) {
            throw new ValidacaoException("Categoria do alimento é obrigatória.");
        }
        if (unidade == null || unidade.isBlank()) {
            throw new ValidacaoException("Unidade do alimento é obrigatória.");
        }
        this.nome = nome.trim();
        this.categoria = categoria;
        this.unidade = unidade.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getUnidade() {
        return unidade;
    }

    @Override
    public String toString() {
        return nome + " (" + categoria.getDescricao() + ", " + unidade + ")";
    }
}
