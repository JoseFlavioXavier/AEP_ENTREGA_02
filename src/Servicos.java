public class Servicos {
    private final EstoqueService estoque = new EstoqueService();
    private final DoadorService doadores = new DoadorService(new InMemoryRepository<>(), estoque);
    private final InstituicaoService instituicoes = new InstituicaoService(new InMemoryRepository<>(), estoque);
    private final AlimentoService alimentos = new AlimentoService(new InMemoryRepository<>(), estoque);

    public EstoqueService getEstoque() {
        return estoque;
    }

    public DoadorService getDoadores() {
        return doadores;
    }

    public InstituicaoService getInstituicoes() {
        return instituicoes;
    }

    public AlimentoService getAlimentos() {
        return alimentos;
    }
}
