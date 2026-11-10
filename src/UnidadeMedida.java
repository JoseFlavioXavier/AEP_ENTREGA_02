public enum UnidadeMedida {
    KG("kg", "Quilograma (kg)"),
    G("g", "Grama (g)"),
    L("l", "Litro (l)"),
    ML("ml", "Mililitro (ml)"),
    UN("un", "Unidade (un)");

    private final String sigla;
    private final String descricao;

    UnidadeMedida(String sigla, String descricao) {
        this.sigla = sigla;
        this.descricao = descricao;
    }

    public String getSigla() {
        return sigla;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return sigla;
    }
}
