public class Doador extends Pessoa {
    private TipoDoador tipo;

    public Doador(String nome, String documento, String contato, TipoDoador tipo) {
        super(nome, documento, contato);
        if (tipo == null) {
            throw new ValidacaoException("Tipo do doador é obrigatório.");
        }
        this.tipo = tipo;
    }

    public TipoDoador getTipo() {
        return tipo;
    }

    public void atualizarDados(String nome, String documento, String contato, TipoDoador tipo) {
        if (tipo == null) {
            throw new ValidacaoException("Tipo do doador é obrigatório.");
        }
        atualizarDadosBasicos(nome, documento, contato);
        this.tipo = tipo;
    }

    @Override
    public String descricao() {
        return "Doador (" + tipo.getDescricao() + "): " + getNome() + " - doc. " + getDocumento();
    }
}
