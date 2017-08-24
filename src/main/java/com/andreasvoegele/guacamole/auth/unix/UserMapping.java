/*
 * Mapping of user and group names to Guacamole configurations
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.guacamole.protocol.GuacamoleConfiguration;

/**
 * Mapping of user and group names to Guacamole configurations.
 */
public class UserMapping {

    /**
     * A service name for the authentication service.
     */
    private String serviceName = "guacamole";

    /**
     * The Guacamole configurations, indexed by name.
     */
    private Map<String, GuacamoleConfiguration> configNameToConfig = new LinkedHashMap<String, GuacamoleConfiguration>();

    /**
     * Mapping of user names to configuration names.
     */
    private Map<String, Set<String>> userNameToConfigNames = new LinkedHashMap<String, Set<String>>();

    /**
     * Mapping of group names to configuration names.
     */
    private Map<String, Set<String>> groupNameToConfigNames = new LinkedHashMap<String, Set<String>>();

    /**
     * Returns the service name for the authentication service, e.g.
     * "guacamole".
     *
     * In the case of PAM, the service name is the name of the PAM configuration
     * file in /etc/pam.d.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName
     *            the service name
     */
    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Adds a Guacamole configuration.
     *
     * @param configName
     *            the configuration's name
     * @param config
     *            the configuration to store
     */
    void addConfig(String configName, GuacamoleConfiguration config) {
        configNameToConfig.put(configName, config);
    }

    /**
     * Adds a user and the names of associated configurations.
     *
     * @param userName
     *            the user's name
     * @param configNames
     *            a set of associated configuration names
     */
    void addUser(String userName, Set<String> configNames) {
        userNameToConfigNames.put(userName, configNames);
    }

    /**
     * Adds a group and the names of associated configurations.
     *
     * @param groupName
     *            the group's name
     * @param configNames
     *            a set of associated configuration names
     */
    void addGroup(String groupName, Set<String> configNames) {
        groupNameToConfigNames.put(groupName, configNames);
    }

    /**
     * Returns the specified user's Guacamole configurations.
     *
     * @param userName
     *            the user's name
     * @param groupNames
     *            the user's groups
     * @return the user's configurations
     */
    public Map<String, GuacamoleConfiguration> getConfigurations(String userName, Collection<String> groupNames) {

        Set<String> configNames = new HashSet<String>();

        if (userNameToConfigNames.containsKey(userName)) {
            configNames.addAll(userNameToConfigNames.get(userName));
        }

        for (String groupName : groupNames) {
            if (groupNameToConfigNames.containsKey(groupName)) {
                configNames.addAll(groupNameToConfigNames.get(groupName));
            }
        }

        Map<String, GuacamoleConfiguration> configs = new HashMap<String, GuacamoleConfiguration>();

        for (String configName : configNames) {
            if (configNameToConfig.containsKey(configName)) {
                // The configurations are copied as they may contain place
                // holders like ${GUAC_USERNAME} that will be replaced later.
                configs.put(configName, new GuacamoleConfiguration(configNameToConfig.get(configName)));
            }
        }

        return configs;

    }

}
