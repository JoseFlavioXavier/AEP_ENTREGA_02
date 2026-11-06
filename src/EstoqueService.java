import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Coordena os lotes do galpão (agregação: os lotes também são referenciados
 * pelas doações e distribuições).
 */
public class EstoqueService {
    private final List<Lote> lotes = new ArrayList<>();

    public void registrarDoacao(Doacao doacao) {
        if (doacao.isRegistrada()) {
            throw new ValidacaoException("Esta doação já foi registrada.");
        }
        if (doacao.getItens().isEmpty()) {
            throw new ValidacaoException("A doação precisa ter pelo menos um item.");
        }
        for (ItemDoacao item : doacao.getItens()) {
            lotes.add(item.getLote());
        }
        doacao.marcarRegistrada();
    }

    public void distribuir(Distribuicao distribuicao) {
        for (ItemDistribuicao item : distribuicao.getItens()) {
            if (!lotes.contains(item.getLote())) {
                throw new ValidacaoException("Lote não pertence ao estoque.");
            }
        }
        distribuicao.concluir();
    }

    public void registrarPerda(Perda perda) {
        if (!lotes.contains(perda.getLote())) {
            throw new ValidacaoException("Lote não pertence ao estoque.");
        }
        perda.processar();
    }

    /** Lotes em ordem de validade (UC03). */
    public List<Lote> listarPorValidade() {
        List<Lote> copia = new ArrayList<>(lotes);
        copia.sort(Comparator.comparing(Lote::getValidade)
                .thenComparing(l -> l.getAlimento().getNome()));
        return copia;
    }

    /** Apenas lotes com saldo e dentro da validade na data informada. */
    public List<Lote> listarDisponiveis(LocalDate data) {
        List<Lote> disponiveis = new ArrayList<>();
        for (Lote l : listarPorValidade()) {
            if (l.getQuantidade().signum() > 0 && !l.estaVencido(data)) {
                disponiveis.add(l);
            }
        }
        return disponiveis;
    }
}
