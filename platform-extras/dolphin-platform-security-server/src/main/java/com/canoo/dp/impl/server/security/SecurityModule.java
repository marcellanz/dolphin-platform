/*
 * Copyright 2015-2018 Canoo Engineering AG.
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
package com.canoo.dp.impl.server.security;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.server.spi.AbstractBaseModule;
import com.canoo.platform.server.spi.ModuleDefinition;
import com.canoo.platform.server.spi.ModuleInitializationException;
import com.canoo.platform.server.spi.ServerCoreComponents;
import org.apiguardian.api.API;

import javax.servlet.ServletContext;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.19.0", status = INTERNAL)
@ModuleDefinition
public class SecurityModule extends AbstractBaseModule {

    public final static String MODULE_NAME = "SecurityModule";

    private final static String MODULE_ACTIVE_PROPERTY= "security.active";

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void initialize(final ServerCoreComponents coreComponents) throws ModuleInitializationException {
        Assert.requireNonNull(coreComponents, "coreComponents");
        final KeycloakConfiguration configuration = new KeycloakConfiguration(coreComponents.getConfiguration());
        final DolphinSecurityBootstrap bootstrap = DolphinSecurityBootstrap.getInstance();
        bootstrap.init(coreComponents.getInstance(ServletContext.class), coreComponents.getConfiguration());

        if(configuration.isLoginEndpointActive()) {
            final ServletContext servletContext = coreComponents.getInstance(ServletContext.class);
            servletContext.addServlet("security-login", new KeycloakTokenServlet(configuration)).addMapping(configuration.getLoginEndpoint());
            servletContext.addServlet("security-logout", new KeycloakLogoutServlet(configuration)).addMapping(configuration.getLogoutEndpoint());


        }
    }

    @Override
    protected String getActivePropertyName() {
        return MODULE_ACTIVE_PROPERTY;
    }
}
