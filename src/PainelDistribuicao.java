import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PainelDistribuicao extends JPanel implements Atualizavel {
    private final Servicos servicos;
    private final JComboBox<Instituicao> destino = new JComboBox<>();
    private final JTextField quantidade = new JTextField(8);
    private final DefaultTableModel modeloLotes =
            Visual.modeloSomenteLeitura("Alimento", "Origem", "Saldo", "Validade");
    private final JTable tabelaLotes = new JTable(modeloLotes);
    private final DefaultTableModel modeloItens = Visual.modeloSomenteLeitura("Alimento", "Quantidade", "Validade");
    private final JTable tabelaItens = new JTable(modeloItens);
    private final List<Lote> disponiveis = new ArrayList<>();
    private final List<ItemPendente> pendentes = new ArrayList<>();

    public PainelDistribuicao(Servicos servicos) {
        super(new BorderLayout(12, 12));
        this.servicos = servicos;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        Visual.rotulos(destino);
        tabelaLotes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel cabecalho = new JPanel(new GridLayout(2, 1));
        cabecalho.setBorder(BorderFactory.createTitledBorder("Nova distribuição"));
        JPanel linhaDestino = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaDestino.add(new JLabel("Instituição destinatária"));
        linhaDestino.add(destino);
        JPanel linhaItem = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaItem.add(new JLabel("Quantidade"));
        linhaItem.add(quantidade);
        JButton adicionar = new JButton("Adicionar lote selecionado");
        adicionar.addActionListener(e -> adicionarItem());
        linhaItem.add(adicionar);
        cabecalho.add(linhaDestino);
        cabecalho.add(linhaItem);

        JPanel painelLotes = new JPanel(new BorderLayout());
        painelLotes.setBorder(BorderFactory.createTitledBorder("Lotes disponíveis (por validade)"));
        painelLotes.add(new JScrollPane(tabelaLotes), BorderLayout.CENTER);
        JPanel painelItens = new JPanel(new BorderLayout());
        painelItens.setBorder(BorderFactory.createTitledBorder("Itens da distribuição"));
        painelItens.add(new JScrollPane(tabelaItens), BorderLayout.CENTER);
        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelLotes, painelItens);
        divisao.setResizeWeight(0.5);

        JButton remover = new JButton("Remover item");
        JButton concluir = new JButton("Concluir distribuição");
        remover.addActionListener(e -> removerItem());
        concluir.addActionListener(e -> concluir());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rodape.add(remover);
        rodape.add(concluir);

        add(cabecalho, BorderLayout.NORTH);
        add(divisao, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
        recarregar();
    }

    @Override
    public void recarregar() {
        Visual.recarregar(destino, servicos.getInstituicoes().listar());
        disponiveis.clear();
        disponiveis.addAll(servicos.getEstoque().listarDisponiveis(LocalDate.now()));
        modeloLotes.setRowCount(0);
        for (Lote l : disponiveis) {
            modeloLotes.addRow(new Object[] {l.getAlimento().getNome(), l.getOrigem().getNome(),
                    l.getQuantidade() + " " + l.getAlimento().getUnidade(), Entrada.formatar(l.getValidade())});
        }
    }

    private void adicionarItem() {
        Mensagens.executar(this, () -> {
            int linha = tabelaLotes.getSelectedRow();
            if (linha < 0) {
                throw new ValidacaoException("Selecione um lote na tabela.");
            }
            Lote lote = disponiveis.get(linha);
            BigDecimal qtd = Entrada.decimal(quantidade.getText(), "Quantidade");
            lote.validarRetirada(qtd, LocalDate.now());
            pendentes.add(new ItemPendente(lote, qtd));
            quantidade.setText("");
            atualizarItens();
        });
    }

    private void removerItem() {
        int linha = tabelaItens.getSelectedRow();
        if (linha < 0) {
            Mensagens.erro(this, "Selecione um item na tabela de itens.");
            return;
        }
        pendentes.remove(linha);
        atualizarItens();
    }

    private void concluir() {
        Mensagens.executar(this, () -> {
            Instituicao instituicao = (Instituicao) destino.getSelectedItem();
            if (instituicao == null) {
                throw new ValidacaoException("Selecione uma instituição destinatária.");
            }
            Distribuicao distribuicao = new Distribuicao(instituicao);
            for (ItemPendente p : pendentes) {
                distribuicao.adicionarItem(p.lote, p.quantidade);
            }
            servicos.getEstoque().distribuir(distribuicao);
            pendentes.clear();
            atualizarItens();
            recarregar();
            Mensagens.info(this, "Distribuição concluída para " + instituicao.getNome() + ".");
        });
    }

    private void atualizarItens() {
        modeloItens.setRowCount(0);
        for (ItemPendente p : pendentes) {
            Lote l = p.lote;
            modeloItens.addRow(new Object[] {l.getAlimento().getNome(),
                    p.quantidade + " " + l.getAlimento().getUnidade(), Entrada.formatar(l.getValidade())});
        }
    }

    private static class ItemPendente {
        private final Lote lote;
        private final BigDecimal quantidade;

        ItemPendente(Lote lote, BigDecimal quantidade) {
            this.lote = lote;
            this.quantidade = quantidade;
        }
    }
}
