/*
 * Copyright (c) 2017, EPAM SYSTEMS INC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.backendapi.resources.dto.SparkStandaloneConfiguration;
import com.epam.dlab.backendapi.resources.dto.aws.AwsEmrConfiguration;
import com.epam.dlab.backendapi.roles.RoleType;
import com.epam.dlab.backendapi.roles.UserRoles;
import com.epam.dlab.cloud.CloudProvider;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.dto.base.DataEngineType;
import com.epam.dlab.dto.imagemetadata.ComputationalMetadataDTO;
import com.epam.dlab.dto.imagemetadata.ExploratoryMetadataDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.DockerAPI;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Provides the REST API to retrieve exploratory/computational templates.
 */
@Path("/infrastructure_templates")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class InfrastructureTemplatesResource implements DockerAPI {

    @Inject
    private SelfServiceApplicationConfiguration configuration;

    @Inject
    private SettingsDAO settingsDAO;


    @Inject
    @Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTService provisioningService;

    /**
     * Returns the list of the computational resources templates for user.
     *
     * @param userInfo user info.
     */
    @GET
    @Path("/computational_templates")
    public Iterable<FullComputationalTemplate> getComputationalTemplates(@Auth UserInfo userInfo) {
        log.debug("Loading list of computational templates for user {}", userInfo.getName());
        try {
            ComputationalMetadataDTO[] array = provisioningService.get(DOCKER_COMPUTATIONAL, userInfo.getAccessToken(), ComputationalMetadataDTO[].class);

            return Arrays.stream(array).map(e -> {
                e.setImage(getSimpleImageName(e.getImage()));
                return e;
            }).filter(e -> UserRoles.checkAccess(userInfo, RoleType.COMPUTATIONAL, e.getImage()))
                    .map(this::fullComputationalTemplate).collect(Collectors.toList());

        } catch (DlabException e) {
            log.error("Could not load list of computational templates for user: {}", userInfo.getName(), e);
            throw e;
        }
    }

    /**
     * Returns the list of the exploratory environment templates for user.
     *
     * @param userInfo user info.
     */
    @GET
    @Path("/exploratory_templates")
    public Iterable<ExploratoryMetadataDTO> getExploratoryTemplates(@Auth UserInfo userInfo) {
        log.debug("Loading list of exploratory templates for user {}", userInfo.getName());
        try {
            ExploratoryMetadataDTO[] array = provisioningService.get(DOCKER_EXPLORATORY, userInfo.getAccessToken(), ExploratoryMetadataDTO[].class);

            return Arrays.stream(array).map(e -> {
                e.setImage(getSimpleImageName(e.getImage()));
                return e;
            }).filter(e -> exploratoryGpuIssuesAzureFilter(e) && UserRoles.checkAccess(userInfo, RoleType.EXPLORATORY, e.getImage())).collect(Collectors.toList());


        } catch (DlabException e) {
            log.error("Could not load list of exploratory templates for user: {}", userInfo.getName(), e);
            throw e;
        }
    }

    /**
     * Temporary filter for creation of exploratory env due to Azure issues
     */
    private boolean exploratoryGpuIssuesAzureFilter(ExploratoryMetadataDTO e) {
        return (settingsDAO.getConfOsFamily().equals("redhat") && configuration.getCloudProvider() == CloudProvider.AZURE) ?
                !(e.getImage().endsWith("deeplearning") || e.getImage().endsWith("tensor"))
                : true;
    }

    /**
     * Return the image name without suffix version.
     *
     * @param imageName the name of image.
     */
    private String getSimpleImageName(String imageName) {
        int separatorIndex = imageName.indexOf(':');
        return (separatorIndex > 0 ? imageName.substring(0, separatorIndex) : imageName);
    }


    /**
     * Wraps metadata with limits
     *
     * @param metadataDTO metadata
     * @return wrapped object
     */

    private FullComputationalTemplate fullComputationalTemplate(ComputationalMetadataDTO metadataDTO) {

        DataEngineType dataEngineType = DataEngineType.fromDockerImageName(metadataDTO.getImage());

        if (dataEngineType == DataEngineType.CLOUD_SERVICE) {
            return new AwsFullComputationalTemplate(metadataDTO,
                    AwsEmrConfiguration.builder()
                            .minEmrInstanceCount(configuration.getMinEmrInstanceCount())
                            .maxEmrInstanceCount(configuration.getMaxEmrInstanceCount())
                            .maxEmrSpotInstanceBidPct(configuration.getMaxEmrSpotInstanceBidPct())
                            .minEmrSpotInstanceBidPct(configuration.getMinEmrSpotInstanceBidPct())
                            .build());
        } else if (dataEngineType == DataEngineType.SPARK_STANDALONE) {
            return new SparkFullComputationalTemplate(metadataDTO,
                    SparkStandaloneConfiguration.builder()
                            .maxSparkInstanceCount(configuration.getMaxSparkInstanceCount())
                            .minSparkInstanceCount(configuration.getMinSparkInstanceCount())
                            .build());
        } else {
            throw new IllegalArgumentException("Unknown data engine " + dataEngineType);
        }
    }

    private class FullComputationalTemplate {
        @JsonUnwrapped
        private ComputationalMetadataDTO computationalMetadataDTO;


        private FullComputationalTemplate(ComputationalMetadataDTO metadataDTO) {
            this.computationalMetadataDTO = metadataDTO;
        }
    }

    private class AwsFullComputationalTemplate extends FullComputationalTemplate {
        @JsonProperty("limits")
        private AwsEmrConfiguration awsEmrConfiguration;

        public AwsFullComputationalTemplate(ComputationalMetadataDTO metadataDTO,
                                            AwsEmrConfiguration awsEmrConfiguration) {
            super(metadataDTO);
            this.awsEmrConfiguration = awsEmrConfiguration;
        }
    }

    private class SparkFullComputationalTemplate extends FullComputationalTemplate {
        @JsonProperty("limits")
        private SparkStandaloneConfiguration sparkStandaloneConfiguration;

        public SparkFullComputationalTemplate(ComputationalMetadataDTO metadataDTO,
                                              SparkStandaloneConfiguration sparkStandaloneConfiguration) {
            super(metadataDTO);
            this.sparkStandaloneConfiguration = sparkStandaloneConfiguration;
        }
    }
}

