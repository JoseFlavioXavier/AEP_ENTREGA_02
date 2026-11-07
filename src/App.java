import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
        }
        SwingUtilities.invokeLater(() -> new TelaPrincipal(new Servicos()).setVisible(true));
    }
}
