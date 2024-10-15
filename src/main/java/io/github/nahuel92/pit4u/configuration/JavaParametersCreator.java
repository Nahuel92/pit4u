package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import io.github.nahuel92.pit4u.gui.Pit4USettingsEditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class JavaParametersCreator {
    private static final List<String> PIT_LIBS = getPitLibs();

    public static JavaParameters create(final JavaRunConfigurationModule configurationModule,
                                        final Project project,
                                        final Pit4USettingsEditor pit4USettingsEditor) {
        setModule(configurationModule, project);
        final var javaParameters = new JavaParameters();
        addPitLibraries(javaParameters);
        configureModules(project, javaParameters);
        javaParameters.setWorkingDirectory(configurationModule.getProject().getBasePath());
        javaParameters.setMainClass("org.pitest.mutationtest.commandline.MutationCoverageReport");
        javaParameters.getProgramParametersList().add("--targetClasses", pit4USettingsEditor.getTargetClasses());
        javaParameters.getProgramParametersList().add("--targetTests", pit4USettingsEditor.getTargetTests());
        javaParameters.getProgramParametersList().add("--sourceDirs", pit4USettingsEditor.getSourceDirs());
        javaParameters.getProgramParametersList().add("--reportDir", pit4USettingsEditor.getReportDir());
        return javaParameters;
    }

    private static List<String> getPitLibs() {
        final var pluginsPath = PathManager.getPluginsPath();
        final var pit4UPath = Path.of(pluginsPath).resolve("pit4u").resolve("lib");
        try (final var path = Files.walk(pit4UPath)) {
            return path.filter(e -> {
                        final var name = e.getFileName().toString();
                        return name.startsWith("pitest-");
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
        if (!javaParameters.getClassPath().getPathList().contains("unit-platform-launcher")) {
            javaParameters.getClassPath().addFirst(
                    PIT_LIBS.getFirst().substring(0, PIT_LIBS.getFirst().indexOf("/lib")) +
                            "/lib/junit-platform-launcher-1.9.2.jar"
            );
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