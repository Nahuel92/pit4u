package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class JavaParametersCreator {
    private static final Logger LOGGER = Logger.getInstance(JavaParametersCreator.class);
    private static final Map<Boolean, List<String>> PIT_LIBS = getPitLibs();

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

    private static Map<Boolean, List<String>> getPitLibs() {
        try (final var path = Files.walk(PathManager.getPluginsDir()
                .resolve("pit4u")
                .resolve("lib"))) {
            return path.filter(e -> {
                        final var name = e.getFileName().toString();
                        return name.startsWith("pitest") || name.startsWith("commons") || name.startsWith("junit-platform");
                    })
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.partitioningBy(e -> e.startsWith("junit-platform")));
        } catch (final IOException e) {
            LOGGER.error("Failure when walking PIT4U library path", e);
            return Map.of();
        }
    }

    private static void setModule(final JavaRunConfigurationModule configurationModule, final Project project) {
        if (configurationModule.getModule() == null && project.getProjectFile() != null) {
            final var module = ModuleUtil.findModuleForFile(project.getProjectFile(), project);
            configurationModule.setModule(module);
        }
    }

    private static void addPitLibraries(final JavaParameters javaParameters) {
        PIT_LIBS.get(false).forEach(e -> javaParameters.getClassPath().add(e));
        final var jplRequired = javaParameters.getClassPath()
                .getPathList()
                .stream()
                .noneMatch(e -> e.startsWith("junit-platform-launcher"));
        if (jplRequired) {
            PIT_LIBS.get(true).forEach(e -> javaParameters.getClassPath().add(e));
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
                LOGGER.error("Failure when configuring module", e);
            }
        }
    }
}