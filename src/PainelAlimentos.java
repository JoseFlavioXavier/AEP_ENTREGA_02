import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class PainelAlimentos extends PainelCrud<Alimento> {
    private final AlimentoService servico;
    private final JTextField nome = new JTextField(20);
    private final JComboBox<Categoria> categoria = new JComboBox<>(Categoria.values());
    private final JTextField unidade = new JTextField(20);

    public PainelAlimentos(AlimentoService servico) {
        super("Alimento", "Nome", "Categoria", "Unidade");
        this.servico = servico;
        Visual.rotulos(categoria);
        adicionarCampo("Nome", nome);
        adicionarCampo("Categoria", categoria);
        adicionarCampo("Unidade", unidade);
        recarregar();
    }

    @Override
    protected List<Alimento> listarRegistros() {
        return servico.listar();
    }

    @Override
    protected Object[] linha(Alimento a) {
        return new Object[] {a.getNome(), a.getCategoria().getDescricao(), a.getUnidade()};
    }

    @Override
    protected void preencher(Alimento a) {
        nome.setText(a.getNome());
        categoria.setSelectedItem(a.getCategoria());
        unidade.setText(a.getUnidade());
    }

    @Override
    protected void limparCampos() {
        nome.setText("");
        categoria.setSelectedIndex(0);
        unidade.setText("");
    }

    @Override
    protected void criar() {
        servico.cadastrar(nome.getText(), (Categoria) categoria.getSelectedItem(), unidade.getText());
    }

    @Override
    protected void alterar(Alimento a) {
        servico.atualizar(a.getId(), nome.getText(), (Categoria) categoria.getSelectedItem(), unidade.getText());
    }

    @Override
    protected void remover(Alimento a) {
        servico.excluir(a.getId(), true);
    }
}
