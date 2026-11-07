import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Distribuicao {
    private final UUID id;
    private final LocalDateTime data;
    private final Instituicao destino;
    private final List<ItemDistribuicao> itens = new ArrayList<>();
    private boolean concluida;

    public Distribuicao(Instituicao destino) {
        this(destino, LocalDateTime.now());
    }

    public Distribuicao(Instituicao destino, LocalDateTime data) {
        if (destino == null) {
            throw new ValidacaoException("A distribuição exige uma instituição destinatária.");
        }
        if (data == null) {
            throw new ValidacaoException("A distribuição exige uma data.");
        }
        this.id = UUID.randomUUID();
        this.destino = destino;
        this.data = data;
    }

    public ItemDistribuicao adicionarItem(Lote lote, BigDecimal quantidade) {
        if (concluida) {
            throw new ValidacaoException("Distribuição concluída não pode receber novos itens.");
        }
        if (lote == null) {
            throw new ValidacaoException("Item da distribuição exige um lote.");
        }
        Lote.exigirPositiva(quantidade);
        ItemDistribuicao item = new ItemDistribuicao(quantidade, lote);
        itens.add(item);
        return item;
    }

    public void concluir() {
        if (concluida) {
            throw new ValidacaoException("Distribuição já concluída.");
        }
        if (itens.isEmpty()) {
            throw new ValidacaoException("A distribuição precisa ter pelo menos um item.");
        }
        Map<Lote, BigDecimal> totalPorLote = new LinkedHashMap<>();
        for (ItemDistribuicao item : itens) {
            totalPorLote.merge(item.getLote(), item.getQuantidade(), BigDecimal::add);
        }
        for (Map.Entry<Lote, BigDecimal> e : totalPorLote.entrySet()) {
            e.getKey().validarRetirada(e.getValue(), data.toLocalDate());
        }
        for (Map.Entry<Lote, BigDecimal> e : totalPorLote.entrySet()) {
            e.getKey().retirar(e.getValue(), data.toLocalDate());
        }
        concluida = true;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getData() {
        return data;
    }

    public Instituicao getDestino() {
        return destino;
    }

    public List<ItemDistribuicao> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public boolean isConcluida() {
        return concluida;
    }
}
