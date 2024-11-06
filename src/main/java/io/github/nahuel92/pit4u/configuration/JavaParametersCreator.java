package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class JavaParametersCreator {
    private static final Path PIT4U_LIB_PATH = PathManager.getPluginsDir()
            .resolve("pit4u")
            .resolve("lib");
    private static final Path JPL_PATH = PIT4U_LIB_PATH.resolve("junit-platform-launcher-1.9.2.jar");
    private static final List<String> PIT_LIBS = getPitLibs();

    public static JavaParameters create(final JavaRunConfigurationModule configurationModule,
                                        final Project project, final PIT4UEditorStatus pit4UEditorStatus) {
        setModule(configurationModule, project);
        final var javaParameters = new JavaParameters();
        addPitLibraries(javaParameters);
        configureModules(project, javaParameters);
        javaParameters.setWorkingDirectory(configurationModule.getProject().getBasePath());
        javaParameters.setMainClass("org.pitest.mutationtest.commandline.MutationCoverageReport");
        javaParameters.getProgramParametersList().add("--targetClasses", pit4UEditorStatus.getTargetClasses());
        javaParameters.getProgramParametersList().add("--targetTests", pit4UEditorStatus.getTargetTests());
        javaParameters.getProgramParametersList().add("--sourceDirs", pit4UEditorStatus.getSourceDir());
        javaParameters.getProgramParametersList().add("--reportDir", pit4UEditorStatus.getReportDir());
        Arrays.stream(pit4UEditorStatus.getOtherParams().split(StringUtils.SPACE))
                .forEach(otherParam -> javaParameters.getProgramParametersList().add(otherParam));
        return javaParameters;
    }

    private static List<String> getPitLibs() {
        try (final var path = Files.walk(PIT4U_LIB_PATH)) {
            return path.filter(e -> {
                        final var name = e.getFileName().toString();
                        return name.startsWith("pitest-") || name.startsWith("commons-");
                    })
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .toList();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setModule(final JavaRunConfigurationModule configurationModule, final Project project) {
        if (configurationModule.getModule() == null && project.getProjectFile() != null) {
            final var module = ModuleUtil.findModuleForFile(project.getProjectFile(), project);
            configurationModule.setModule(module);
        }
    }

    private static void addPitLibraries(final JavaParameters javaParameters) {
        PIT_LIBS.forEach(e -> javaParameters.getClassPath().addFirst(e));
        final var jplRequired = javaParameters.getClassPath()
                .getPathList()
                .stream()
                .noneMatch(e -> e.startsWith("junit-platform-launcher"));
        if (jplRequired) {
            javaParameters.getClassPath().addFirst(JPL_PATH.toString());
        }
    }

    private static void configureModules(final Project project, final JavaParameters javaParameters) {
        for (final var module : ModuleManager.getInstance(project).getModules()) {
            try {
                JavaParametersUtil.configureModule(module,
                        javaParameters,
                        JavaParameters.JDK_AND_CLASSES_AND_TESTS,
                        null
                );
            } catch (final CantRunException e) {
                throw new RuntimeException(e);
            }
        }
    }
}