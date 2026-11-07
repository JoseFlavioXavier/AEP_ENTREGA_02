import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TelaPrincipal extends JFrame {
    public TelaPrincipal(Servicos servicos) {
        super("Banco de Alimentos de Londrina");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Doadores", new PainelDoadores(servicos.getDoadores()));
        abas.addTab("Instituições", new PainelInstituicoes(servicos.getInstituicoes()));
        abas.addTab("Alimentos", new PainelAlimentos(servicos.getAlimentos()));
        abas.addTab("Doação", new PainelDoacao(servicos));
        abas.addTab("Distribuição", new PainelDistribuicao(servicos));
        abas.addTab("Estoque", new PainelEstoque(servicos));
        abas.addChangeListener(e -> {
            Component atual = abas.getSelectedComponent();
            if (atual instanceof Atualizavel) {
                ((Atualizavel) atual).recarregar();
            }
        });

        add(abas);
        setSize(1000, 620);
        setLocationRelativeTo(null);
    }
}
