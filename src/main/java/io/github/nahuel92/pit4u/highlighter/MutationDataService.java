package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.LowMemoryWatcher;
import io.github.nahuel92.pit4u.highlighter.dto.Mutation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class MutationDataService implements Disposable {
    private static final Logger LOG = Logger.getInstance(MutationDataService.class);
    private final Map<String, List<Mutation>> mutationMap = new ConcurrentHashMap<>();

    MutationDataService() {
        LowMemoryWatcher.register(this::clear, this);
    }

    @NotNull
    public static MutationDataService getInstance(@NotNull final Project project) {
        return project.getService(MutationDataService.class);
    }

    public void loadData(@NotNull final Collection<@NotNull Mutation> mutations) {
        mutationMap.clear();
        mutationMap.putAll(mutations.stream().collect(Collectors.groupingBy(Mutation::mutatedClass)));
        LOG.debug("Mutation data loaded");
    }

    @NotNull
    @Unmodifiable
    public Collection<@NotNull Mutation> getMutationsForClass(@NotNull final String fqName) {
        final var mutations = mutationMap.getOrDefault(fqName, List.of());
        return List.copyOf(mutations);
    }

    @Override
    public void dispose() {
        clear();
    }

    public boolean hasActiveMutations() {
        return !mutationMap.isEmpty();
    }

    public void clear() {
        mutationMap.clear();
        LOG.debug("Cleared mutation data");
    }
}