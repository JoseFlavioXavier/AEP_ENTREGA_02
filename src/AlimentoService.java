import java.util.List;
import java.util.UUID;

public class AlimentoService {
    private final CrudRepository<Alimento> repositorio;
    private final EstoqueService estoque;

    public AlimentoService(CrudRepository<Alimento> repositorio, EstoqueService estoque) {
        if (repositorio == null || estoque == null) {
            throw new ValidacaoException("O serviço de alimentos exige repositório e estoque.");
        }
        this.repositorio = repositorio;
        this.estoque = estoque;
    }

    public Alimento cadastrar(String nome, Categoria categoria, String unidade) {
        exigirNomeUnico(nome, null);
        Alimento alimento = new Alimento(nome, categoria, unidade);
        repositorio.salvar(alimento);
        return alimento;
    }

    public Alimento buscar(UUID id) {
        return repositorio.buscar(id)
                .orElseThrow(() -> new ValidacaoException("Alimento não encontrado."));
    }

    public List<Alimento> listar() {
        return repositorio.listar();
    }

    public void atualizar(UUID id, String nome, Categoria categoria, String unidade) {
        Alimento alimento = buscar(id);
        exigirNomeUnico(nome, id);
        alimento.atualizarDados(nome, categoria, unidade);
    }

    public void excluir(UUID id, boolean confirmado) {
        if (!confirmado) {
            throw new ValidacaoException("A exclusão exige confirmação.");
        }
        Alimento alimento = buscar(id);
        if (estoque.possuiLotesDe(alimento)) {
            throw new ValidacaoException("Alimento com lotes no estoque não pode ser excluído.");
        }
        repositorio.excluir(id);
    }

    private void exigirNomeUnico(String nome, UUID idIgnorado) {
        if (nome == null || nome.isBlank()) {
            return;
        }
        for (Alimento existente : repositorio.listar()) {
            boolean outro = idIgnorado == null || !existente.getId().equals(idIgnorado);
            if (outro && existente.getNome().equalsIgnoreCase(nome.trim())) {
                throw new ValidacaoException("Já existe um alimento com este nome.");
            }
        }
    }
}
