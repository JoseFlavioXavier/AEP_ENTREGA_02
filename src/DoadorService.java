import java.util.List;
import java.util.UUID;

public class DoadorService {
    private final CrudRepository<Doador> repositorio;
    private final EstoqueService estoque;

    public DoadorService(CrudRepository<Doador> repositorio, EstoqueService estoque) {
        if (repositorio == null || estoque == null) {
            throw new ValidacaoException("O serviço de doadores exige repositório e estoque.");
        }
        this.repositorio = repositorio;
        this.estoque = estoque;
    }

    public Doador cadastrar(String nome, String documento, String contato, TipoDoador tipo) {
        exigirDocumentoUnico(documento, null);
        Doador doador = new Doador(nome, documento, contato, tipo);
        repositorio.salvar(doador);
        return doador;
    }

    public Doador buscar(UUID id) {
        return repositorio.buscar(id)
                .orElseThrow(() -> new ValidacaoException("Doador não encontrado."));
    }

    public List<Doador> listar() {
        return repositorio.listar();
    }

    public void atualizar(UUID id, String nome, String documento, String contato, TipoDoador tipo) {
        Doador doador = buscar(id);
        exigirDocumentoUnico(documento, id);
        doador.atualizarDados(nome, documento, contato, tipo);
    }

    public void excluir(UUID id, boolean confirmado) {
        if (!confirmado) {
            throw new ValidacaoException("A exclusão exige confirmação.");
        }
        Doador doador = buscar(id);
        if (estoque.possuiLotesDe(doador)) {
            throw new ValidacaoException("Doador com lotes no estoque não pode ser excluído.");
        }
        repositorio.excluir(id);
    }

    private void exigirDocumentoUnico(String documento, UUID idIgnorado) {
        if (documento == null || documento.isBlank()) {
            return;
        }
        String alvo = somenteDigitos(documento);
        for (Doador existente : repositorio.listar()) {
            boolean outro = idIgnorado == null || !existente.getId().equals(idIgnorado);
            if (outro && somenteDigitos(existente.getDocumento()).equals(alvo)) {
                throw new ValidacaoException("Já existe um doador com este documento.");
            }
        }
    }

    private static String somenteDigitos(String texto) {
        return texto.replaceAll("\\D", "");
    }
}
