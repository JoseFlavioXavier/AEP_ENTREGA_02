public enum Categoria {
    GRAOS("Grãos"),
    ENLATADOS("Enlatados"),
    LATICINIOS("Laticínios"),
    HORTIFRUTI("Hortifrúti"),
    PROTEINAS("Proteínas"),
    BEBIDAS("Bebidas"),
    OUTROS("Outros");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
