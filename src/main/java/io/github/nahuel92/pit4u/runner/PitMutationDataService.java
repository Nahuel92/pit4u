package io.github.nahuel92.pit4u.runner;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.github.nahuel92.pit4u.highlighter.Mutation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class PitMutationDataService {
    private static final Logger log = Logger.getInstance(PitMutationDataService.class);
    private final Map<String, List<Mutation>> mutationMap = new HashMap<>();

    public static PitMutationDataService getInstance(final Project project) {
        return project.getService(PitMutationDataService.class);
    }

    public void loadData(final Collection<Mutation> mutations) {
        mutationMap.clear();
        mutationMap.putAll(mutations.stream()
                .collect(Collectors.groupingBy(Mutation::mutatedClass)));
    }

    public List<Mutation> getMutationsForClass(final String fqName) {
        return mutationMap.get(fqName);
    }

    public void clear() {
        mutationMap.clear();
    }
}