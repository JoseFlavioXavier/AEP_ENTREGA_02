public enum TipoDoador {
    PESSOA_FISICA("Pessoa física"),
    PESSOA_JURIDICA("Pessoa jurídica");

    private final String descricao;

    TipoDoador(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
