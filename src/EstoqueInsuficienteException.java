import java.math.BigDecimal;

public class EstoqueInsuficienteException extends ValidacaoException {
    private static final long serialVersionUID = 1L;

    public EstoqueInsuficienteException(String alimento, BigDecimal saldo, BigDecimal pedido) {
        super("Estoque insuficiente de " + alimento + ": saldo " + saldo + ", pedido " + pedido + ".");
    }
}
