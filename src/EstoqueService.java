import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EstoqueService {
    private final List<Lote> lotes = new ArrayList<>();
    private final List<Distribuicao> distribuicoes = new ArrayList<>();

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
        distribuicoes.add(distribuicao);
    }

    public void registrarPerda(Perda perda) {
        if (!lotes.contains(perda.getLote())) {
            throw new ValidacaoException("Lote não pertence ao estoque.");
        }
        perda.processar();
    }

    public List<Lote> listarPorValidade() {
        List<Lote> copia = new ArrayList<>(lotes);
        copia.sort(Comparator.comparing(Lote::getValidade)
                .thenComparing(l -> l.getAlimento().getNome()));
        return copia;
    }

    public List<Lote> listarDisponiveis(LocalDate data) {
        List<Lote> disponiveis = new ArrayList<>();
        for (Lote l : listarPorValidade()) {
            if (l.getQuantidade().signum() > 0 && !l.estaVencido(data)) {
                disponiveis.add(l);
            }
        }
        return disponiveis;
    }

    public List<Distribuicao> listarDistribuicoes() {
        return Collections.unmodifiableList(distribuicoes);
    }

    public boolean possuiLotesDe(Doador doador) {
        for (Lote lote : lotes) {
            if (lote.getOrigem().getId().equals(doador.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean possuiLotesDe(Alimento alimento) {
        for (Lote lote : lotes) {
            if (lote.getAlimento().getId().equals(alimento.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean possuiDistribuicoesPara(Instituicao instituicao) {
        for (Distribuicao d : distribuicoes) {
            if (d.getDestino().getId().equals(instituicao.getId())) {
                return true;
            }
        }
        return false;
    }
}
