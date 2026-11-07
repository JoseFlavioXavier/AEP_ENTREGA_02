import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.table.DefaultTableModel;

public final class Visual {
    private Visual() {
    }

    public static DefaultTableModel modeloSomenteLeitura(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    public static void rotulos(JComboBox<?> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                setText(texto(valor));
                return this;
            }
        });
    }

    public static <T> void recarregar(JComboBox<T> combo, List<T> itens) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        for (T item : itens) {
            combo.addItem(item);
        }
        if (anterior != null && itens.contains(anterior)) {
            combo.setSelectedItem(anterior);
        }
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Pessoa) {
            return ((Pessoa) valor).getNome();
        }
        if (valor instanceof TipoDoador) {
            return ((TipoDoador) valor).getDescricao();
        }
        if (valor instanceof Categoria) {
            return ((Categoria) valor).getDescricao();
        }
        return valor.toString();
    }
}
