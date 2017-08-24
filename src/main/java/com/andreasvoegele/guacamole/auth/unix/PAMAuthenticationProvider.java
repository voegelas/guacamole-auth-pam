/*
 * PAM authentication provider for Guacamole
 *
 * Copyright 2017 Andreas Voegele
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andreasvoegele.guacamole.auth.unix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.environment.LocalEnvironment;
import org.apache.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.apache.guacamole.net.auth.Credentials;
import org.apache.guacamole.protocol.GuacamoleConfiguration;

import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * PAM authentication provider for Guacamole.
 *
 * The provider reads the configuration from
 * GUACAMOLE_HOME/unix-user-mapping.xml.
 */
public class PAMAuthenticationProvider extends SimpleAuthenticationProvider {

    /**
     * Logger for this class.
     */
    private final Logger logger = LoggerFactory.getLogger(PAMAuthenticationProvider.class);

    /**
     * The configuration filename.
     */
    public static final String USER_MAPPING_FILENAME = "unix-user-mapping.xml";

    /**
     * The time the configuration file was last modified.
     */
    private long lastModified;

    /**
     * The parsed configuration.
     */
    private UserMapping cachedUserMapping;

    /**
     * Guacamole server environment.
     */
    private final Environment environment;

    /**
     * Creates a new PAMAuthenticationProvider.
     *
     * @throws GuacamoleException
     *             If a required property is missing, or an error occurs while
     *             parsing a property.
     */
    public PAMAuthenticationProvider() throws GuacamoleException {
        environment = new LocalEnvironment();
    }

    @Override
    public String getIdentifier() {
        return "pam";
    }

    /**
     * XXX
     *
     * @return
     * @throws GuacamoleException
     */
    private UserMapping getUserMapping() {

        File userMappingFile = new File(environment.getGuacamoleHome(), USER_MAPPING_FILENAME);
        if (!userMappingFile.exists()) {
            logger.debug("User mapping file \"{}\" does not exist and will not be read.", userMappingFile);
            return null;
        }

        if (lastModified < userMappingFile.lastModified()) {

            synchronized (this) {

                logger.debug("Reading user mapping file: \"{}\"", userMappingFile);

                try {

                    UserMappingContentHandler contentHandler = new UserMappingContentHandler();

                    XMLReader parser = XMLReaderFactory.createXMLReader();
                    parser.setContentHandler(contentHandler);
                    Reader reader = new BufferedReader(new FileReader(userMappingFile));
                    parser.parse(new InputSource(reader));
                    reader.close();

                    // XXX Warn if unknown config names.
                    lastModified = userMappingFile.lastModified();
                    cachedUserMapping = contentHandler.asUserMapping();

                } catch (IOException e) {
                    logger.warn("Unable to read user mapping file \"{}\": {}", userMappingFile, e.getMessage());
                    return null;
                } catch (SAXException e) {
                    logger.warn("User mapping file \"{}\" is not valid: {}", userMappingFile, e.getMessage());
                    return null;
                }

            }
        }

        return cachedUserMapping;

    }

    @Override
    public Map<String, GuacamoleConfiguration> getAuthorizedConfigurations(Credentials cred) throws GuacamoleException {

        // Abort authorization if no configuration exists
        UserMapping userMapping = getUserMapping();
        if (userMapping == null)
            throw new GuacamoleServerException("Configuration could not be read.");

        // Validate user and return the associated connections
        try {
            String serviceName = userMapping.getServiceName();
            String userName = cred.getUsername();
            UnixUser user = new PAM(serviceName).authenticate(userName, cred.getPassword());
            if (user != null)
                return userMapping.getConfigurations(userName, user.getGroups());
        } catch (PAMException e) {
            // Fall through
        }

        // Unauthorized
        return null;

    }

}