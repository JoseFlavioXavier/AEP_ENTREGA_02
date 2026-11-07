import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrudRepository<T extends Identificavel> {
    void salvar(T entidade);

    Optional<T> buscar(UUID id);

    List<T> listar();

    void excluir(UUID id);
}
