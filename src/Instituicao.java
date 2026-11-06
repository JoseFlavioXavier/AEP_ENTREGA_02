public class Instituicao extends Pessoa {
    private String endereco;
    private int capacidade;

    public Instituicao(String nome, String documento, String contato, String endereco, int capacidade) {
        super(nome, documento, contato);
        this.endereco = exigirTexto(endereco, "Endereço");
        if (capacidade <= 0) {
            throw new ValidacaoException("Capacidade deve ser positiva.");
        }
        this.capacidade = capacidade;
    }

    public String getEndereco() {
        return endereco;
    }

    public int getCapacidade() {
        return capacidade;
    }

    @Override
    public String descricao() {
        return "Instituição: " + getNome() + " - " + getEndereco() + " (atende " + capacidade + " pessoas)";
    }
}
