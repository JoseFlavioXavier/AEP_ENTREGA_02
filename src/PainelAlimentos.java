import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class PainelAlimentos extends PainelCrud<Alimento> {
    private final AlimentoService servico;
    private final JTextField nome = new JTextField(20);
    private final JComboBox<Categoria> categoria = new JComboBox<>(Categoria.values());
    private final JComboBox<UnidadeMedida> unidade = new JComboBox<>(UnidadeMedida.values());

    public PainelAlimentos(AlimentoService servico) {
        super("Alimento", "Nome", "Categoria", "Unidade de medida");
        this.servico = servico;
        Visual.rotulos(categoria);
        Visual.rotulos(unidade);
        adicionarCampo("Nome", nome);
        adicionarCampo("Categoria", categoria);
        adicionarCampo("Unidade de medida", unidade);
        recarregar();
    }

    @Override
    protected List<Alimento> listarRegistros() {
        return servico.listar();
    }

    @Override
    protected Object[] linha(Alimento a) {
        return new Object[] {a.getNome(), a.getCategoria().getDescricao(), a.getUnidade().getDescricao()};
    }

    @Override
    protected void preencher(Alimento a) {
        nome.setText(a.getNome());
        categoria.setSelectedItem(a.getCategoria());
        unidade.setSelectedItem(a.getUnidade());
    }

    @Override
    protected void limparCampos() {
        nome.setText("");
        categoria.setSelectedIndex(0);
        unidade.setSelectedIndex(0);
    }

    @Override
    protected void criar() {
        servico.cadastrar(nome.getText(), (Categoria) categoria.getSelectedItem(),
                (UnidadeMedida) unidade.getSelectedItem());
    }

    @Override
    protected void alterar(Alimento a) {
        servico.atualizar(a.getId(), nome.getText(), (Categoria) categoria.getSelectedItem(),
                (UnidadeMedida) unidade.getSelectedItem());
    }

    @Override
    protected void remover(Alimento a) {
        servico.excluir(a.getId(), true);
    }
}
