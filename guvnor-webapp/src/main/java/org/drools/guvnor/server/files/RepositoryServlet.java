/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.server.files;

import org.drools.guvnor.server.util.BeanManagerUtils;
import org.drools.guvnor.server.util.TestEnvironmentSessionHelper;
import org.drools.repository.RulesRepository;
import org.drools.util.codec.Base64;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.solder.beanManager.BeanManagerLocator;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * This is a base servlet that all repo servlets inherit behaviour from.
 */
public class RepositoryServlet extends HttpServlet {

    private static final long serialVersionUID = 510l;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    protected RulesRepository rulesRepository;

    @Inject
    protected AuthorizationHeaderChecker authorizationHeaderChecker;

    @Deprecated
    public static FileManagerService getFileManager() { // TODO seam3upgrade
        BeanManagerLocator beanManagerLocator = new BeanManagerLocator();
        if (beanManagerLocator.isBeanManagerAvailable()) {
            return (FileManagerService) BeanManagerUtils.getInstance("fileManager");
        } else {
            //MN: NOTE THIS IS MY HACKERY TO GET IT WORKING IN GWT HOSTED MODE.
            //THIS IS ALL THAT IS NEEDED FOR THE SERVLETS.
//            log.debug("WARNING: RUNNING IN NON SEAM MODE SINGLE USER MODE - ONLY FOR TESTING AND DEBUGGING !!!!!");
            FileManagerService manager = new FileManagerService();
            try {
                manager.setRepository(new RulesRepository(TestEnvironmentSessionHelper.getSession(false)));
                return manager;
            } catch (Exception e) {
                throw new IllegalStateException();
            }

        }
    }

    /**
     * Here we perform the action in the appropriate security context.
     */
    void doAuthorizedAction(HttpServletRequest req,
                            HttpServletResponse res,
                            Command action) throws IOException {
        String auth = req.getHeader("Authorization");

        if (!authorizationHeaderChecker.allowUser(auth)) {
            res.setHeader("WWW-Authenticate",
                    "BASIC realm=\"users\"");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                action.execute();
            } catch (RuntimeException e) {
                log.error(e.getMessage(),
                        e);
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(),
                        e);
                throw new RuntimeException(e);
            }
        }
    }

    static interface Command {
        public void execute() throws Exception;
    }

}
