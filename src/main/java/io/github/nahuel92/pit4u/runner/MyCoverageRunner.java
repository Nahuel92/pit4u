package io.github.nahuel92.pit4u.runner;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.openapi.diagnostic.Logger;
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

    public XMLProjectData loadCoverageData(final File xmlFile) {
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


        for (final var mutatedClass : mutationsByClass.entrySet()) {
            var coveredLines = 0;
            var uncoveredLines = 0;
            var sourceFile = "";

            final var linesInfo = new ArrayList<XMLProjectData.LineInfo>();
            for (final var mutation : mutatedClass.getValue()) {
                sourceFile = mutation.sourceFile();
                if (mutation.status() == Mutation.Status.KILLED) {
                    coveredLines++;
                }
                if (mutation.status() == Mutation.Status.NO_COVERAGE) {
                    uncoveredLines++;
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
            final var classInfo = new XMLProjectData.ClassInfo(
                    mutatedClass.getKey(),
                    sourceFile,
                    uncoveredLines,
                    coveredLines,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
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