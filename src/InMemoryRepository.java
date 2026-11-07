import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryRepository<T extends Identificavel> implements CrudRepository<T> {
    private final Map<UUID, T> registros = new LinkedHashMap<>();

    @Override
    public void salvar(T entidade) {
        if (entidade == null) {
            throw new ValidacaoException("Não é possível salvar um registro nulo.");
        }
        registros.put(entidade.getId(), entidade);
    }

    @Override
    public Optional<T> buscar(UUID id) {
        return Optional.ofNullable(registros.get(id));
    }

    @Override
    public List<T> listar() {
        return new ArrayList<>(registros.values());
    }

    @Override
    public void excluir(UUID id) {
        registros.remove(id);
    }
}
