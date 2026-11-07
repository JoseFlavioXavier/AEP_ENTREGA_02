public class Instituicao extends Pessoa {
    private String endereco;
    private int capacidade;

    public Instituicao(String nome, String documento, String contato, String endereco, int capacidade) {
        super(nome, documento, contato);
        this.endereco = exigirTexto(endereco, "Endereço");
        this.capacidade = exigirCapacidade(capacidade);
    }

    public void atualizarDados(String nome, String documento, String contato, String endereco, int capacidade) {
        String novoEndereco = exigirTexto(endereco, "Endereço");
        int novaCapacidade = exigirCapacidade(capacidade);
        atualizarDadosBasicos(nome, documento, contato);
        this.endereco = novoEndereco;
        this.capacidade = novaCapacidade;
    }

    private static int exigirCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new ValidacaoException("Capacidade deve ser positiva.");
        }
        return capacidade;
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
