package io.github.nahuel92.pit4u.runner;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.analysis.AnalysisUtils;
import com.intellij.coverage.analysis.CoverageInfoCollector;
import com.intellij.coverage.analysis.JavaCoverageAnnotator;
import com.intellij.coverage.analysis.JavaCoverageClassesAnnotator;
import com.intellij.coverage.analysis.PackageAnnotator;
import com.intellij.coverage.view.CoverageClassStructure;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.rt.coverage.report.XMLProjectData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class MyCoverageAnnotator extends JavaCoverageAnnotator {
    private static final Logger log = Logger.getInstance(MyCoverageAnnotator.class);
    private final Project project;

    MyCoverageAnnotator(final Project project) {
        super(project);
        this.project = project;
    }

    public static MyCoverageAnnotator getInstance(final Project project) {
        return project.getService(MyCoverageAnnotator.class);
    }

    @Override
    public Runnable createRenewRequest(@NotNull final CoverageSuitesBundle suite,
                                       @NotNull final CoverageDataManager dataManager) {
        log.info("Executing createRenewRequest()...");
        return () -> {
            annotate(suite, dataManager, new JavaCoverageInfoCollector(this));
            myStructure = new CoverageClassStructure(project, this, suite);
            Disposer.register(this, myStructure);
            dataManager.triggerPresentationUpdate();
        };
    }

    private void annotate(final CoverageSuitesBundle suite, final CoverageDataManager dataManager, final CoverageInfoCollector collector) {
        final var classCoverage = new HashMap<String, PackageAnnotator.ClassCoverageInfo>();
        final var flattenPackageCoverage = new HashMap<String, PackageAnnotator.PackageCoverageInfo>();
        final var flattenDirectoryCoverage = new HashMap<VirtualFile, PackageAnnotator.PackageCoverageInfo>();
        final Module[] sourceRoots0 = dataManager.doInReadActionIfProjectOpen(
                () -> ModuleManager.getInstance(suite.getProject()).getModules()
        );
        final var sourceRoots1 = sourceRoots0 == null ? List.<Module>of() : Arrays.asList(sourceRoots0);
        final var sourceRoots = sourceRoots1.stream()
                .map(JavaCoverageClassesAnnotator::getSourceRoots)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        for (final var xmlSuite : suite.getSuites()) {
            if (!(xmlSuite instanceof MyCoverageSuite s)) {
                continue;
            }

            final var xmlReport = s.getReportData();
            if (xmlReport == null) {
                continue;
            }
            for (final var classInfo : xmlReport.getClasses()) {
                final var currentCoverage = classCoverage.putIfAbsent(
                        classInfo.name,
                        new PackageAnnotator.ClassCoverageInfo()
                );
                final var thisSuiteCoverage = getCoverageForClass(classInfo);

                // apply delta
                //final var coverage = thisSuiteCoverage - currentCoverage;
                //currentCoverage.append(coverage);

                final var packageName = StringUtil.getPackageName(classInfo.name);
                final var virtualFile = findFile(packageName, classInfo.fileName, sourceRoots);

                final var a = flattenPackageCoverage.putIfAbsent(packageName, new PackageAnnotator.PackageCoverageInfo());
                //a.append(coverage);
                if (virtualFile != null) {
                    final var b = flattenDirectoryCoverage.putIfAbsent(virtualFile, new PackageAnnotator.PackageCoverageInfo());
                    //b.append(coverage);
                }
            }
        }

        // Include anonymous and internal classes to the containing class
        classCoverage
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(e -> AnalysisUtils.getSourceToplevelFQName(e.getKey())))
                .forEach((key, value) -> {
                    final var coverage = new PackageAnnotator.ClassCoverageInfo();
                    value.forEach(r -> coverage.append(r.getValue()));
                    collector.addClass(key, coverage);
                });

        JavaCoverageClassesAnnotator.annotatePackages(flattenPackageCoverage, collector);
        JavaCoverageClassesAnnotator.annotateDirectories(flattenDirectoryCoverage, collector, sourceRoots);
    }

    private VirtualFile findFile(String packageName, String fileName, Collection<VirtualFile> sourceRoots) {
        if (fileName == null) return null;
        final var path = MyCoverageSuite.getPath(packageName, fileName);
        for (final var root : sourceRoots) {
            final var file = root.findFileByRelativePath(path);
            if (file == null) {
                continue;
            }
            return file.getParent();
        }
        return null;
    }

    private PackageAnnotator.ClassCoverageInfo getCoverageForClass(final XMLProjectData.ClassInfo classInfo) {
        final var coverage = new PackageAnnotator.ClassCoverageInfo();
        coverage.totalBranchCount = classInfo.coveredBranches + classInfo.missedBranches;
        coverage.coveredBranchCount = classInfo.coveredBranches;
        coverage.totalMethodCount = classInfo.coveredMethods + classInfo.missedMethods;
        coverage.coveredMethodCount = classInfo.coveredMethods;
        if (coverage.totalMethodCount > 0) {
            coverage.totalClassCount = 1;
        } else {
            coverage.totalClassCount = 0;
        }
        if (classInfo.coveredMethods > 0) {
            coverage.coveredClassCount = 1;
        } else {
            coverage.coveredClassCount = 0;
        }
        coverage.totalLineCount = classInfo.coveredLines + classInfo.missedLines;
        coverage.fullyCoveredLineCount = classInfo.coveredLines;
        return coverage;
    }

}
