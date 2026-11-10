import java.util.UUID;

public class Alimento implements Identificavel {
    private final UUID id;
    private String nome;
    private Categoria categoria;
    private UnidadeMedida unidade;

    public Alimento(String nome, Categoria categoria, UnidadeMedida unidade) {
        this.id = UUID.randomUUID();
        atualizarDados(nome, categoria, unidade);
    }

    public final void atualizarDados(String nome, Categoria categoria, UnidadeMedida unidade) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Nome do alimento é obrigatório.");
        }
        if (categoria == null) {
            throw new ValidacaoException("Categoria do alimento é obrigatória.");
        }
        if (unidade == null) {
            throw new ValidacaoException("Unidade de medida do alimento é obrigatória.");
        }
        this.nome = nome.trim();
        this.categoria = categoria;
        this.unidade = unidade;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    @Override
    public String toString() {
        return nome + " (" + categoria.getDescricao() + ", " + unidade + ")";
    }
}
