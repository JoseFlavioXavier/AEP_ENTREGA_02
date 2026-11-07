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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PainelDoacao extends JPanel implements Atualizavel {
    private final Servicos servicos;
    private final JComboBox<Doador> doador = new JComboBox<>();
    private final JComboBox<Alimento> alimento = new JComboBox<>();
    private final JTextField quantidade = new JTextField(8);
    private final JTextField validade = new JTextField(10);
    private final DefaultTableModel modelo = Visual.modeloSomenteLeitura("Alimento", "Quantidade", "Validade");
    private final JTable tabela = new JTable(modelo);
    private final List<ItemPendente> pendentes = new ArrayList<>();

    public PainelDoacao(Servicos servicos) {
        super(new BorderLayout(12, 12));
        this.servicos = servicos;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        Visual.rotulos(doador);
        Visual.rotulos(alimento);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel cabecalho = new JPanel(new GridLayout(2, 1));
        cabecalho.setBorder(BorderFactory.createTitledBorder("Nova doação"));
        JPanel linhaOrigem = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaOrigem.add(new JLabel("Doador"));
        linhaOrigem.add(doador);
        linhaOrigem.add(new JLabel("Alimento"));
        linhaOrigem.add(alimento);
        JPanel linhaItem = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaItem.add(new JLabel("Quantidade"));
        linhaItem.add(quantidade);
        linhaItem.add(new JLabel("Validade (dd/mm/aaaa)"));
        linhaItem.add(validade);
        JButton adicionar = new JButton("Adicionar item");
        adicionar.addActionListener(e -> adicionarItem());
        linhaItem.add(adicionar);
        cabecalho.add(linhaOrigem);
        cabecalho.add(linhaItem);

        JButton remover = new JButton("Remover item");
        JButton registrar = new JButton("Registrar doação");
        remover.addActionListener(e -> removerItem());
        registrar.addActionListener(e -> registrar());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rodape.add(remover);
        rodape.add(registrar);

        add(cabecalho, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
        recarregar();
    }

    @Override
    public void recarregar() {
        Visual.recarregar(doador, servicos.getDoadores().listar());
        Visual.recarregar(alimento, servicos.getAlimentos().listar());
    }

    private void adicionarItem() {
        Mensagens.executar(this, () -> {
            Alimento escolhido = (Alimento) alimento.getSelectedItem();
            if (escolhido == null) {
                throw new ValidacaoException("Cadastre um alimento antes de registrar doações.");
            }
            BigDecimal qtd = Entrada.decimal(quantidade.getText(), "Quantidade");
            LocalDate data = Entrada.data(validade.getText(), "Validade");
            if (qtd.signum() <= 0) {
                throw new ValidacaoException("A quantidade deve ser positiva.");
            }
            if (data.isBefore(LocalDate.now())) {
                throw new LoteVencidoException(escolhido.getNome(), data);
            }
            pendentes.add(new ItemPendente(escolhido, qtd, data));
            quantidade.setText("");
            validade.setText("");
            atualizarTabela();
        });
    }

    private void removerItem() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Mensagens.erro(this, "Selecione um item na tabela.");
            return;
        }
        pendentes.remove(linha);
        atualizarTabela();
    }

    private void registrar() {
        Mensagens.executar(this, () -> {
            Doador origem = (Doador) doador.getSelectedItem();
            if (origem == null) {
                throw new ValidacaoException("Selecione um doador.");
            }
            Doacao doacao = new Doacao(origem);
            for (ItemPendente p : pendentes) {
                doacao.adicionarItem(p.alimento, p.quantidade, p.validade);
            }
            servicos.getEstoque().registrarDoacao(doacao);
            int lotes = doacao.getItens().size();
            pendentes.clear();
            atualizarTabela();
            Mensagens.info(this, "Doação registrada: " + lotes + " lote(s) no estoque.");
        });
    }

    private void atualizarTabela() {
        modelo.setRowCount(0);
        for (ItemPendente p : pendentes) {
            modelo.addRow(new Object[] {p.alimento.getNome(), p.quantidade + " " + p.alimento.getUnidade(),
                    Entrada.formatar(p.validade)});
        }
    }

    private static class ItemPendente {
        private final Alimento alimento;
        private final BigDecimal quantidade;
        private final LocalDate validade;

        ItemPendente(Alimento alimento, BigDecimal quantidade, LocalDate validade) {
            this.alimento = alimento;
            this.quantidade = quantidade;
            this.validade = validade;
        }
    }
}
