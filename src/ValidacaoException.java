/**
 * Erro de regra de negócio ou de dados inválidos (RN01 a RN05).
 */
public class ValidacaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
