package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.LowMemoryWatcher;
import io.github.nahuel92.pit4u.highlighter.dto.Mutation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class MutationDataService {
    private static final Logger LOG = Logger.getInstance(MutationDataService.class);
    private final Map<String, List<Mutation>> mutationMap = new HashMap<>();

    MutationDataService() {
        LowMemoryWatcher.register(this::clear);
    }

    public static MutationDataService getInstance(@NotNull final Project project) {
        return project.getService(MutationDataService.class);
    }

    public void loadData(@NotNull final Collection<Mutation> mutations) {
        mutationMap.clear();
        mutationMap.putAll(mutations.stream()
                .collect(Collectors.groupingBy(Mutation::mutatedClass)));
        LOG.debug("Mutation data loaded");
    }

    public Collection<Mutation> getMutationsForClass(final String fqName) {
        return mutationMap.get(fqName);
    }

    public boolean hasActiveMutations() {
        return !mutationMap.isEmpty();
    }

    public void clear() {
        mutationMap.clear();
        LOG.debug("Cleared mutation data");
    }
}