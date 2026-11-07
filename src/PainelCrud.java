import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public abstract class PainelCrud<T extends Identificavel> extends JPanel implements Atualizavel {
    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final JPanel formulario = new JPanel(new GridBagLayout());
    private final List<T> exibidos = new ArrayList<>();
    private int proximaLinha;
    private boolean atualizando;

    protected PainelCrud(String titulo, String... colunas) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        modelo = Visual.modeloSomenteLeitura(colunas);
        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !atualizando) {
                selecionar();
            }
        });

        JButton salvar = new JButton("Salvar");
        JButton novo = new JButton("Novo");
        JButton excluir = new JButton("Excluir");
        salvar.addActionListener(e -> salvar());
        novo.addActionListener(e -> novo());
        excluir.addActionListener(e -> excluir());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(salvar);
        botoes.add(novo);
        botoes.add(excluir);

        JPanel lateral = new JPanel(new BorderLayout(0, 8));
        lateral.setBorder(BorderFactory.createTitledBorder(titulo));
        lateral.add(formulario, BorderLayout.NORTH);
        lateral.add(botoes, BorderLayout.CENTER);

        add(lateral, BorderLayout.WEST);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    protected final void adicionarCampo(String rotulo, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = proximaLinha++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 8);
        c.gridx = 0;
        formulario.add(new JLabel(rotulo), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(campo, c);
    }

    protected abstract List<T> listarRegistros();

    protected abstract Object[] linha(T registro);

    protected abstract void preencher(T registro);

    protected abstract void limparCampos();

    protected abstract void criar();

    protected abstract void alterar(T registro);

    protected abstract void remover(T registro);

    @Override
    public void recarregar() {
        T selecionado = registroSelecionado();
        atualizando = true;
        modelo.setRowCount(0);
        exibidos.clear();
        exibidos.addAll(listarRegistros());
        int indice = -1;
        for (int i = 0; i < exibidos.size(); i++) {
            T registro = exibidos.get(i);
            modelo.addRow(linha(registro));
            if (selecionado != null && registro.getId().equals(selecionado.getId())) {
                indice = i;
            }
        }
        if (indice >= 0) {
            tabela.setRowSelectionInterval(indice, indice);
        }
        atualizando = false;
    }

    private T registroSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= exibidos.size()) {
            return null;
        }
        return exibidos.get(linha);
    }

    private void selecionar() {
        T registro = registroSelecionado();
        if (registro != null) {
            preencher(registro);
        }
    }

    private void salvar() {
        Mensagens.executar(this, () -> {
            T atual = registroSelecionado();
            if (atual == null) {
                criar();
            } else {
                alterar(atual);
            }
            recarregar();
            novo();
        });
    }

    private void excluir() {
        T atual = registroSelecionado();
        if (atual == null) {
            Mensagens.erro(this, "Selecione um registro na tabela.");
            return;
        }
        if (!Mensagens.confirmar(this, "Deseja realmente excluir este registro?")) {
            return;
        }
        Mensagens.executar(this, () -> {
            remover(atual);
            recarregar();
            novo();
        });
    }

    private void novo() {
        atualizando = true;
        tabela.clearSelection();
        atualizando = false;
        limparCampos();
    }
}
