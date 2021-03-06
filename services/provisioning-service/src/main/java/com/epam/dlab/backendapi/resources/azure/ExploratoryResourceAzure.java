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

package com.epam.dlab.backendapi.resources.azure;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.resources.base.ExploratoryService;
import com.epam.dlab.dto.azure.exploratory.ExploratoryActionStopAzure;
import com.epam.dlab.dto.azure.exploratory.ExploratoryCreateAzure;
import com.epam.dlab.dto.exploratory.ExploratoryGitCredsUpdateDTO;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/exploratory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExploratoryResourceAzure {
    @Inject
    private ExploratoryService exploratoryService;

    @Path("/create")
    @POST
    public String create(@Auth UserInfo ui, ExploratoryCreateAzure dto) throws IOException, InterruptedException {
        return exploratoryService.action(ui.getName(), dto, DockerAction.CREATE);
    }

    @Path("/start")
    @POST
    public String start(@Auth UserInfo ui, ExploratoryGitCredsUpdateDTO dto) throws IOException, InterruptedException {
        return exploratoryService.action(ui.getName(), dto, DockerAction.START);
    }

    @Path("/terminate")
    @POST
    public String terminate(@Auth UserInfo ui, ExploratoryActionStopAzure dto) throws IOException, InterruptedException {
        return exploratoryService.action(ui.getName(), dto, DockerAction.TERMINATE);
    }

    @Path("/stop")
    @POST
    public String stop(@Auth UserInfo ui, ExploratoryActionStopAzure dto) throws IOException, InterruptedException {
        return exploratoryService.action(ui.getName(), dto, DockerAction.STOP);
    }
}
