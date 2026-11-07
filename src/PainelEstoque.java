import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PainelEstoque extends JPanel implements Atualizavel {
    private final Servicos servicos;
    private final DefaultTableModel modelo = Visual.modeloSomenteLeitura(
            "Alimento", "Categoria", "Origem", "Saldo", "Validade", "Dias p/ vencer", "Situação");
    private final JTable tabela = new JTable(modelo);
    private final JTextField quantidade = new JTextField(8);
    private final JTextField motivo = new JTextField(20);
    private final List<Lote> exibidos = new ArrayList<>();

    public PainelEstoque(Servicos servicos) {
        super(new BorderLayout(12, 12));
        this.servicos = servicos;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel rodape = new JPanel(new GridLayout(2, 1));
        rodape.setBorder(BorderFactory.createTitledBorder("Registrar perda do lote selecionado"));
        JPanel linhaDados = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaDados.add(new JLabel("Quantidade"));
        linhaDados.add(quantidade);
        linhaDados.add(new JLabel("Motivo"));
        linhaDados.add(motivo);
        JPanel linhaBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton perda = new JButton("Registrar perda");
        JButton atualizar = new JButton("Atualizar");
        perda.addActionListener(e -> registrarPerda());
        atualizar.addActionListener(e -> recarregar());
        linhaBotoes.add(perda);
        linhaBotoes.add(atualizar);
        rodape.add(linhaDados);
        rodape.add(linhaBotoes);

        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
        recarregar();
    }

    @Override
    public void recarregar() {
        LocalDate hoje = LocalDate.now();
        exibidos.clear();
        exibidos.addAll(servicos.getEstoque().listarPorValidade());
        modelo.setRowCount(0);
        for (Lote l : exibidos) {
            long dias = ChronoUnit.DAYS.between(hoje, l.getValidade());
            modelo.addRow(new Object[] {l.getAlimento().getNome(), l.getAlimento().getCategoria().getDescricao(),
                    l.getOrigem().getNome(), l.getQuantidade() + " " + l.getAlimento().getUnidade(),
                    Entrada.formatar(l.getValidade()), dias, situacao(l, dias)});
        }
    }

    private String situacao(Lote lote, long dias) {
        if (lote.getQuantidade().signum() == 0) {
            return "Esgotado";
        }
        if (dias < 0) {
            return "Vencido";
        }
        if (dias <= 7) {
            return "Vence em até 7 dias";
        }
        return "OK";
    }

    private void registrarPerda() {
        Mensagens.executar(this, () -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                throw new ValidacaoException("Selecione um lote na tabela.");
            }
            BigDecimal qtd = Entrada.decimal(quantidade.getText(), "Quantidade");
            Perda perda = new Perda(exibidos.get(linha), qtd, motivo.getText());
            servicos.getEstoque().registrarPerda(perda);
            quantidade.setText("");
            motivo.setText("");
            recarregar();
        });
    }
}
