package io.github.nahuel92.pit4u.runner;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.JavaCoverageSuite;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.report.XMLProjectData;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MyCoverageSuite extends JavaCoverageSuite {
    private static final Logger log = Logger.getInstance(MyCoverageSuite.class);
    private XMLProjectData data;

    public MyCoverageSuite(@NotNull final MyCoverageEngine coverageEngine) {
        super(coverageEngine);
        log.info("In MyCoverageSuite first constructor...");
    }

    public MyCoverageSuite(@NotNull String name,
                           Project project,
                           @NotNull MyCoverageRunner runner,
                           @NotNull CoverageFileProvider fileProvider,
                           long lastCoverageTimeStamp,
                           MyCoverageEngine myCoverageEngine) {
        super(
                name,
                fileProvider,
                new String[]{},
                new String[]{},
                lastCoverageTimeStamp,
                false,
                true,
                false,
                runner,
                myCoverageEngine,
                project
        );
        log.info("In MyCoverageSuite second constructor...");
    }

    static String getPath(final String packageName, final String fileName) {
        if (packageName.isEmpty()) {
            return fileName;
        }
        return "${AnalysisUtils.fqnToInternalName(packageName)}/$fileName";
    }

    XMLProjectData getReportData() {
        log.info("Executing getReportData()...");
        if (data != null) {
            return data;
        }
        final var file = new File(getCoverageDataFileName());
        if (!file.exists()) {
            return null;
        }
        log.info("Calling loadCoverageData()...");
        data = ((MyCoverageRunner) getRunner()).loadCoverageData(file);
        log.info("loadCoverageData() finished");
        return data;
    }

    public XMLProjectData.FileInfo getFileInfo(final String packageName, final String fileName) {
        log.info("Executing getFileInfo()...");
        final var path = getPath(packageName, fileName);
        final var reportData = getReportData();
        return reportData == null ? null : reportData.getFile(path);
    }

    @Override
    public ProjectData getCoverageData(final CoverageDataManager coverageDataManager) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public void setCoverageData(final ProjectData projectData) {
        throw new UnsupportedOperationException("Should not be called");
    }
}
