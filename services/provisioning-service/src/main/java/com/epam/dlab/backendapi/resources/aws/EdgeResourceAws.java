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

package com.epam.dlab.backendapi.resources.aws;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.core.response.handlers.EdgeCallbackHandler;
import com.epam.dlab.backendapi.resources.EdgeService;
import com.epam.dlab.dto.ResourceSysBaseDTO;
import com.epam.dlab.dto.aws.edge.EdgeInfoAws;
import com.epam.dlab.dto.aws.keyload.UploadFileAws;
import com.epam.dlab.dto.aws.keyload.UploadFileResultAws;
import com.epam.dlab.rest.contracts.EdgeAPI;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static com.epam.dlab.rest.contracts.ApiCallbacks.*;

/**
 * Provides API to manage Edge node on AWS
 */
@Path(EdgeAPI.EDGE)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class EdgeResourceAws extends EdgeService {
    public EdgeResourceAws() {
        log.info("{} is initialized", getClass().getSimpleName());
    }

    @POST
    @Path("/create")
    public String create(@Auth UserInfo ui, UploadFileAws dto) throws IOException, InterruptedException {
        saveKeyToFile(dto.getEdge().getEdgeUserName(), dto.getContent());
        return action(ui.getName(), dto.getEdge(), dto.getEdge().getAwsIamUser(), KEY_LOADER, DockerAction.CREATE);
    }

    @POST
    @Path("/start")
    public String start(@Auth UserInfo ui, ResourceSysBaseDTO<?> dto) throws IOException, InterruptedException {
        return action(ui.getName(), dto, dto.getAwsIamUser(), EDGE + STATUS_URI, DockerAction.START);
    }

    @POST
    @Path("/stop")
    public String stop(@Auth UserInfo ui, ResourceSysBaseDTO<?> dto) throws IOException, InterruptedException {
        return action(ui.getName(), dto, dto.getAwsIamUser(), EDGE + STATUS_URI, DockerAction.STOP);
    }

    @SuppressWarnings("unchecked")
    protected FileHandlerCallback getFileHandlerCallback(DockerAction action, String uuid, String user, String callbackURI) {
        return new EdgeCallbackHandler(selfService, action, uuid, user, callbackURI, EdgeInfoAws.class, UploadFileResultAws.class);
    }
}