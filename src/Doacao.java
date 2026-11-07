import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Doacao {
    private final UUID id;
    private final LocalDateTime data;
    private final Doador doador;
    private final List<ItemDoacao> itens = new ArrayList<>();
    private boolean registrada;

    public Doacao(Doador doador) {
        this(doador, LocalDateTime.now());
    }

    public Doacao(Doador doador, LocalDateTime data) {
        if (doador == null) {
            throw new ValidacaoException("A doação exige um doador.");
        }
        if (data == null) {
            throw new ValidacaoException("A doação exige uma data.");
        }
        this.id = UUID.randomUUID();
        this.doador = doador;
        this.data = data;
    }

    public ItemDoacao adicionarItem(Alimento alimento, BigDecimal quantidade, LocalDate validade) {
        if (registrada) {
            throw new ValidacaoException("Doação já registrada não pode receber novos itens.");
        }
        if (alimento == null || validade == null) {
            throw new ValidacaoException("Item da doação exige alimento e validade.");
        }
        if (validade.isBefore(data.toLocalDate())) {
            throw new LoteVencidoException(alimento.getNome(), validade);
        }
        Lote lote = new Lote(alimento, doador, quantidade, validade);
        ItemDoacao item = new ItemDoacao(quantidade, lote);
        itens.add(item);
        return item;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getData() {
        return data;
    }

    public Doador getDoador() {
        return doador;
    }

    public List<ItemDoacao> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public boolean isRegistrada() {
        return registrada;
    }

    void marcarRegistrada() {
        this.registrada = true;
    }
}
