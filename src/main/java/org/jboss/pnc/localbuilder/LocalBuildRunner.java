package org.jboss.pnc.localbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.localbuilder.factories.BuildConfigurationSetFactory;
import org.jboss.pnc.localbuilder.model.JSONBuildConfiguration;
import org.jboss.pnc.localbuilder.parser.PncLocalCliOptions;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;


import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Singleton
public class LocalBuildRunner {

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    PncLocalCliOptions options;


    BuildConfigurationSet buildConfigurationSet;

    public void main(@Observes ContainerInitialized event, @Parameters List<String> parameters) {
        final CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(parameters);
            File jsonSource = options.getBuildJSON();
            ObjectMapper mapper = new ObjectMapper();
            List<JSONBuildConfiguration> jsonConfig = mapper.readValue(jsonSource, new TypeReference<List<JSONBuildConfiguration>>() {});
            executeBuild(jsonConfig);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CmdLineException e) {
            parser.printUsage(System.err);
        }
    }

    private void executeBuild(List<JSONBuildConfiguration> configList){
        if (configList.stream().allMatch(config -> config.isValid())) {
            buildConfigurationSet = BuildConfigurationSetFactory.generateBuildConfigurationSet(configList, "Local Build");
            try {
                buildCoordinator.build(buildConfigurationSet, null);
            } catch (CoreException e) {
                e.printStackTrace();
            }
//            catch (DatastoreException e) {
//                e.printStackTrace();
//            }
        }
        else {
            System.err.println("Error: Configuration file is invalid");
        }
    }

    public void collectBuildSetStatusChangedEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        if (buildSetStatusChangedEvent.getNewStatus().isCompleted()){
            buildCoordinator.shutdownCoordinator();
        }
    }

    public void collectBuildStatusChangedEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        Optional<BuildConfiguration> optional = buildConfigurationSet.getBuildConfigurations().stream()
                .filter(bc -> bc.getId() == buildStatusChangedEvent.getBuildConfigurationId()).findFirst();
        BuildConfiguration buildConfiguration = optional.get();
        if (buildConfiguration != null) {
            System.out.println(String.format("Build %s is now %s",buildConfiguration.getName(), buildStatusChangedEvent.getNewStatus().name()));
        }
    }


}
