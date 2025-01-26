package io.github.nahuel92.pit4u.runner;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.report.XMLProjectData;
import io.github.nahuel92.pit4u.highlighter.Mutation;
import io.github.nahuel92.pit4u.highlighter.Mutations;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MyCoverageRunner extends CoverageRunner {
    private static final Logger log = Logger.getInstance(MyCoverageRunner.class);
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    @Override
    @Nullable
    public ProjectData loadCoverageData(@NotNull final File sessionDataFile,
                                        @Nullable final CoverageSuite baseCoverageSuite) {
        throw new UnsupportedOperationException("Should not be called");
    }

    public XMLProjectData loadCoverageData(final File xmlFile, final Project project) {
        log.info("Executing loadCoverageData(xmlFile)...");

        Mutations results;
        try {
            results = XML_MAPPER.readValue(xmlFile, Mutations.class);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        final var mutationsByClass = results.mutations()
                .stream()
                .collect(Collectors.groupingBy(Mutation::mutatedClass));

        final var classesInfo = new ArrayList<XMLProjectData.ClassInfo>();
        final var filesInfo = new ArrayList<XMLProjectData.FileInfo>();


        final var psiFacade = JavaPsiFacade.getInstance(project);


        for (final var mutatedClass : mutationsByClass.entrySet()) {
            final var psiClass = psiFacade.findClass(
                    mutatedClass.getKey(),
                    GlobalSearchScope.allScope(project)
            );

            int missedLines = 0;
            int missedMethods = 0;
            if (psiClass != null) {
                final var document = FileDocumentManager.getInstance()
                        .getDocument(psiClass.getContainingFile().getVirtualFile());
                if (document != null) {
                    missedLines = document.getLineCount();
                    missedMethods = psiClass.getMethods().length;
                }
            }

            var coveredLines = 0;
            var sourceFile = "";

            final var linesInfo = new ArrayList<XMLProjectData.LineInfo>();
            final var methods = new HashSet<String>();
            for (final var mutation : mutatedClass.getValue()) {
                sourceFile = mutation.sourceFile();
                if (mutation.status() == Mutation.Status.KILLED) {
                    coveredLines++;
                    missedLines--;
                    methods.add(mutation.mutatedMethod());
                }

                //
                final var lineInfo = new XMLProjectData.LineInfo(
                        mutation.lineNumber(),
                        0,
                        mutation.status() == Mutation.Status.NO_COVERAGE ? 0 : 1,
                        0,
                        0
                );
                linesInfo.add(lineInfo);
            }

            missedMethods -= methods.size();


            final var classInfo = new XMLProjectData.ClassInfo(
                    mutatedClass.getKey(),
                    sourceFile,
                    missedLines,
                    coveredLines,
                    0,
                    0,
                    0,
                    0,
                    missedMethods,
                    methods.size()
            );
            classesInfo.add(classInfo);

            //
            final var fileInfo = new XMLProjectData.FileInfo(mutatedClass.getKey());
            fileInfo.lines.addAll(linesInfo);
            filesInfo.add(fileInfo);
        }

        final var projectData = new XMLProjectData();
        classesInfo.forEach(projectData::addClass);
        filesInfo.forEach(projectData::addFile);
        return projectData;
    }

    @Override
    @NotNull
    @NonNls
    public String getPresentableName() {
        return "Presentable name test"; // I'll give it a proper name later
    }

    @Override
    @NotNull
    @NonNls
    public String getId() {
        return "io.github.nahuel92.MyCoverageEngine";
    }

    @Override
    @NotNull
    @NonNls
    public String getDataFileExtension() {
        return "xml";
    }

    @Override
    public boolean acceptsCoverageEngine(@NotNull final CoverageEngine coverageEngine) {
        return coverageEngine instanceof MyCoverageEngine;
    }
}