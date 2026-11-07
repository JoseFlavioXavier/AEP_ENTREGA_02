import java.awt.Component;
import javax.swing.JOptionPane;

public final class Mensagens {
    private Mensagens() {
    }

    public static void erro(Component pai, String texto) {
        JOptionPane.showMessageDialog(pai, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component pai, String texto) {
        JOptionPane.showMessageDialog(pai, texto, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component pai, String texto) {
        int resposta = JOptionPane.showConfirmDialog(pai, texto, "Confirmação", JOptionPane.YES_NO_OPTION);
        return resposta == JOptionPane.YES_OPTION;
    }

    public static void executar(Component pai, Runnable acao) {
        try {
            acao.run();
        } catch (ValidacaoException e) {
            erro(pai, e.getMessage());
        }
    }
}
