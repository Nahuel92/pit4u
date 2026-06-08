package io.github.nahuel92.pit4u.configuration;

import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.execution.ParametersListUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

final class JavaParametersCreator {
    private static final Logger LOG = Logger.getInstance(JavaParametersCreator.class);
    private static final String PLUGIN_ID = "io.github.nahuel92.pit4u";
    private static Collection<String> PIT_LIBS;

    public static JavaParameters create(@NotNull final JavaRunConfigurationModule configurationModule,
                                        @NotNull final Project project,
                                        @NotNull final PIT4UEditorStatus pit4UEditorStatus,
                                        @NotNull final String alignedLauncherPath) {
        final var javaParameters = new JavaParameters();
        setModule(configurationModule, project);
        final var module = configurationModule.getModule();
        configureModules(module, project, javaParameters);
        addPitLibraries(javaParameters, alignedLauncherPath);

        javaParameters.setWorkingDirectory(configurationModule.getProject().getBasePath());
        javaParameters.setMainClass("org.pitest.mutationtest.commandline.MutationCoverageReport");

        javaParameters.getProgramParametersList().add("--targetClasses", pit4UEditorStatus.getTargetClasses());
        javaParameters.getProgramParametersList().add("--targetTests", pit4UEditorStatus.getTargetTests());
        javaParameters.getProgramParametersList().add("--sourceDirs", pit4UEditorStatus.getSourceDir());
        javaParameters.getProgramParametersList().add("--reportDir", pit4UEditorStatus.getReportDir());

        if (StringUtils.isNotBlank(pit4UEditorStatus.getOtherParams())) {
            javaParameters.getProgramParametersList().addAll(ParametersListUtil.parse(pit4UEditorStatus.getOtherParams()));
        }
        return javaParameters;
    }

    private static Collection<String> getPitLibs() {
        final var pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));
        if (pluginDescriptor == null) {
            LOG.error("Could not find plugin descriptor for " + PLUGIN_ID);
            return Set.of();
        }

        final var libPath = pluginDescriptor.getPluginPath().resolve("lib");
        if (!Files.exists(libPath)) {
            return Set.of();
        }

        try (final var walk = Files.walk(libPath)) {
            return walk.filter(path -> {
                        final var name = path.getFileName().toString();
                        return name.startsWith("pitest") || name.startsWith("commons");
                    })
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .toList();
        } catch (final IOException e) {
            LOG.error("Failure when walking PIT4U library path: " + libPath, e);
            return Set.of();
        }
    }

    private static void setModule(final JavaRunConfigurationModule configurationModule, final Project project) {
        if (configurationModule.getModule() == null && project.getProjectFile() != null) {
            final var module = ModuleUtil.findModuleForFile(project.getProjectFile(), project);
            configurationModule.setModule(module);
        }
    }

    private static void addPitLibraries(final JavaParameters javaParameters, final String alignedLauncherPath) {
        if (PIT_LIBS == null || PIT_LIBS.isEmpty()) {
            PIT_LIBS = getPitLibs();
        }
        PIT_LIBS.forEach(e -> javaParameters.getClassPath().add(e));

        final var hasLauncher = javaParameters.getClassPath()
                .getPathList()
                .stream()
                .anyMatch(e -> e.contains("junit-platform-launcher"));
        if (!hasLauncher && alignedLauncherPath != null) {
            javaParameters.getClassPath().addTail(alignedLauncherPath);
        }
    }

    private static void configureModules(final Module module,
                                         final Project project, final JavaParameters javaParameters) {
        if (module != null) {
            configureModule(module, javaParameters);
            return;
        }
        LOG.warn("Target module could not be determined. Proceeding with fallback project-level classpath configuration.");
        final var projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            javaParameters.setJdk(projectSdk);
        }
    }

    private static void configureModule(final Module module, final JavaParameters javaParameters) {
        try {
            JavaParametersUtil.configureModule(
                    module,
                    javaParameters,
                    JavaParameters.JDK_AND_CLASSES_AND_TESTS,
                    null
            );
        } catch (final CantRunException e) {
            LOG.error("Failure when configuring module", e);
        }
    }
}