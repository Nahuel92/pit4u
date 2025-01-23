package io.github.nahuel92.pit4u.runner;

import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.view.JavaCoverageViewExtension;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class MyCoverageEngine extends CoverageEngine {
    private static final Logger log = Logger.getInstance(MyCoverageEngine.class);

    @Override
    @Nullable
    public CoverageSuite createCoverageSuite(@NotNull final CoverageRunner runner,
                                             @NotNull final String name,
                                             @NotNull final CoverageFileProvider fileProvider,
                                             @Nullable final String[] filters,
                                             long lastCoverageTimeStamp,
                                             @Nullable final String suiteToMerge,
                                             boolean coverageByTestEnabled,
                                             boolean branchCoverage,
                                             boolean trackTestFolders,
                                             final Project project) {
        if (!(runner instanceof MyCoverageRunner s)) {
            return null;
        }
        return new MyCoverageSuite(name, project, s, fileProvider, lastCoverageTimeStamp, this);
    }

    @Override
    @Nullable
    public CoverageSuite createCoverageSuite(@NotNull final CoverageRunner covRunner,
                                             @NotNull final String name,
                                             @NotNull final CoverageFileProvider coverageDataFileProvider,
                                             @NotNull final CoverageEnabledConfiguration config) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    @Nullable
    public MyCoverageSuite createEmptyCoverageSuite(@NotNull final CoverageRunner coverageRunner) {
        if (!(coverageRunner instanceof MyCoverageRunner)) {
            return null;
        }
        return new MyCoverageSuite(this);
    }

    @Override
    public boolean coverageEditorHighlightingApplicableTo(@NotNull final PsiFile psiFile) {
        return psiFile instanceof PsiClassOwner;
    }

    @Override
    public boolean acceptedByFilters(@NotNull final PsiFile psiFile, @NotNull final CoverageSuitesBundle suite) {
        log.info("Executing acceptedByFilters...");
        final var entry = packageAndFileName();
        if (entry == null) {
            return false;
        }

        log.info("Looping through suites...");
        for (final var xmlSuite : suite.getSuites()) {
            if (!(xmlSuite instanceof MyCoverageSuite mySuite)) {
                continue;
            }
            log.info("Calling mySuite.getFileInfo()...");
            if (mySuite.getFileInfo(entry.getKey(), entry.getValue()) != null) {
                log.info("mySuite.getFileInfo() finished");
                return true;
            }
        }
        return false;
    }

    @Override
    public JavaCoverageViewExtension createCoverageViewExtension(final Project project, final CoverageSuitesBundle suiteBundle) {
        return new JavaCoverageViewExtension(getCoverageAnnotator(project), project, suiteBundle) {
            @Override
            protected boolean isBranchInfoAvailable(final CoverageRunner coverageRunner, final boolean branchCoverage) {
                log.info("Executing isBranchInfoAvailable()...");
                return true;
            }
        };
    }


    @Override
    @NlsActions.ActionText
    public String getPresentableText() {
        return "Test";
    }

    @Override
    public boolean isApplicableTo(@NotNull final RunConfigurationBase<?> conf) {
        return false;
    }

    @Override
    @NotNull
    public Set<String> getQualifiedNames(@NotNull final PsiFile sourceFile) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    @NotNull
    public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@NotNull final RunConfigurationBase<?> conf) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    @NotNull
    public MyCoverageAnnotator getCoverageAnnotator(final Project project) {
        log.info("Executing getCoverageAnnotator...");
        return MyCoverageAnnotator.getInstance(project);
    }

    private Map.Entry<String, String> packageAndFileName() {
        if (!(this instanceof PsiClassOwner s)) {
            return null;
        }
        final var packageName = s.getPackageName();
        return Map.entry(packageName, s.getName());
    }
}