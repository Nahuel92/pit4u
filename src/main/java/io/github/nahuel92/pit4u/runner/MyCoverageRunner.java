package io.github.nahuel92.pit4u.runner;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.report.XMLProjectData;
import io.github.nahuel92.pit4u.highlighter.Mutations;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

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
            throw new RuntimeException(e);
        }

        final var projectData = new XMLProjectData();

        results.mutations()
                .stream()
                .filter(e -> StringUtils.isNotBlank(e.mutatedClass()))
                .map(e ->
                        new XMLProjectData.ClassInfo(
                                e.mutatedClass(),
                                e.mutatedClass(),
                                0,
                                10,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0
                        )
                )
                .forEach(projectData::addClass);

        XMLProjectData.FileInfo info = new XMLProjectData.FileInfo("io.github.nahuel92.MyEntity");
        info.lines.add(
                new XMLProjectData.LineInfo(
                        1, 0, 1, 0, 1
                )
        );
        projectData.addFile(
                info
        );

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