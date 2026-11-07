import java.util.List;
import java.util.UUID;

public class InstituicaoService {
    private final CrudRepository<Instituicao> repositorio;
    private final EstoqueService estoque;

    public InstituicaoService(CrudRepository<Instituicao> repositorio, EstoqueService estoque) {
        if (repositorio == null || estoque == null) {
            throw new ValidacaoException("O serviço de instituições exige repositório e estoque.");
        }
        this.repositorio = repositorio;
        this.estoque = estoque;
    }

    public Instituicao cadastrar(String nome, String documento, String contato, String endereco, int capacidade) {
        exigirDocumentoUnico(documento, null);
        Instituicao instituicao = new Instituicao(nome, documento, contato, endereco, capacidade);
        repositorio.salvar(instituicao);
        return instituicao;
    }

    public Instituicao buscar(UUID id) {
        return repositorio.buscar(id)
                .orElseThrow(() -> new ValidacaoException("Instituição não encontrada."));
    }

    public List<Instituicao> listar() {
        return repositorio.listar();
    }

    public void atualizar(UUID id, String nome, String documento, String contato, String endereco, int capacidade) {
        Instituicao instituicao = buscar(id);
        exigirDocumentoUnico(documento, id);
        instituicao.atualizarDados(nome, documento, contato, endereco, capacidade);
    }

    public void excluir(UUID id, boolean confirmado) {
        if (!confirmado) {
            throw new ValidacaoException("A exclusão exige confirmação.");
        }
        Instituicao instituicao = buscar(id);
        if (estoque.possuiDistribuicoesPara(instituicao)) {
            throw new ValidacaoException("Instituição com distribuições registradas não pode ser excluída.");
        }
        repositorio.excluir(id);
    }

    private void exigirDocumentoUnico(String documento, UUID idIgnorado) {
        if (documento == null || documento.isBlank()) {
            return;
        }
        String alvo = documento.replaceAll("\\D", "");
        for (Instituicao existente : repositorio.listar()) {
            boolean outra = idIgnorado == null || !existente.getId().equals(idIgnorado);
            if (outra && existente.getDocumento().replaceAll("\\D", "").equals(alvo)) {
                throw new ValidacaoException("Já existe uma instituição com este documento.");
            }
        }
    }
}
