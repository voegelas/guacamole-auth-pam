/*
 * XML parser for unix-user-mapping.xml
 *
 * Copyright 2017-2019 Andreas Voegele
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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.guacamole.protocol.GuacamoleConfiguration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML parser for the configuration file used by the PAM authentication
 * provider.
 */
public class UserMappingContentHandler extends DefaultHandler {

    /**
     * The name of the document root.
     */
    private String rootTag = "unix-user-mapping";

    /**
     * The UserMapping which will contain all parsed data.
     */
    private UserMapping userMapping = new UserMapping();

    /**
     * The parser's states.
     */
    private enum State {
        DOCUMENT, TOP_LEVEL, CONFIG, PARAM, USER, GROUP, CONFIG_REF
    }

    /**
     * The parser's state is tracked on a stack.
     */
    private Stack<State> state = new Stack<State>();

    /**
     * The name of the current configuration, if any.
     */
    private String configName = null;

    /**
     * The current configuration being parsed, if any.
     */
    private GuacamoleConfiguration config = null;

    /**
     * The current user or group name, if any.
     */
    private String userOrGroupName = null;

    /**
     * The names of the configurations that are associated with the current user
     * or group, if any.
     */
    private Set<String> configNames = null;

    @Override
    public void startDocument() {
        state.push(State.DOCUMENT);
    }

    @Override
    public void endDocument() {
        state.pop();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equals(rootTag)) {

            if (state.peek() != State.DOCUMENT)
                throw new SAXException(rootTag + " cannot be nested.");

            String serviceName = attributes.getValue("serviceName");
            if (serviceName != null)
                userMapping.setServiceName(serviceName);

            state.push(State.TOP_LEVEL);

        }

        else if (qName.equals("config")) {

            if (state.peek() != State.TOP_LEVEL)
                throw new SAXException("config cannot be nested.");

            configName = attributes.getValue("name");
            if (configName == null)
                throw new SAXException("Each config must have a name.");

            String protocol = attributes.getValue("protocol");
            if (protocol == null)
                throw new SAXException("Each config must have a protocol.");

            config = new GuacamoleConfiguration();
            config.setProtocol(protocol);

            state.push(State.CONFIG);

        }

        else if (qName.equals("param")) {

            if (state.peek() != State.CONFIG)
                throw new SAXException("param without config");

            String name = attributes.getValue("name");
            if (name == null)
                throw new SAXException("Each param must have a name.");

            String value = attributes.getValue("value");
            if (value == null)
                throw new SAXException("Each param must have a value.");

            config.setParameter(name, value);

            state.push(State.PARAM);
        }

        else if (qName.equals("user")) {

            if (state.peek() != State.TOP_LEVEL)
                throw new SAXException("user cannot be nested.");

            userOrGroupName = attributes.getValue("name");
            if (userOrGroupName == null)
                throw new SAXException("Each user must have a name.");

            configNames = new LinkedHashSet<String>();

            state.push(State.USER);
        }

        else if (qName.equals("group")) {

            if (state.peek() != State.TOP_LEVEL)
                throw new SAXException("group cannot be nested.");

            userOrGroupName = attributes.getValue("name");
            if (userOrGroupName == null)
                throw new SAXException("Each group must have a name.");

            configNames = new LinkedHashSet<String>();

            state.push(State.GROUP);
        }

        else if (qName.equals("config-ref")) {

            if (state.peek() != State.USER && state.peek() != State.GROUP)
                throw new SAXException("config-ref without user or group");

            String name = attributes.getValue("name");
            if (name == null)
                throw new SAXException("Each config-ref must have a name.");

            configNames.add(name);

            state.push(State.CONFIG_REF);
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equals(rootTag))
            state.pop();

        else if (qName.equals("config")) {

            userMapping.addConfig(configName, config);

            configName = null;
            config = null;

            state.pop();

        }

        else if (qName.equals("param"))
            state.pop();

        else if (qName.equals("user")) {

            userMapping.addUser(userOrGroupName, configNames);

            userOrGroupName = null;
            configNames = null;

            state.pop();

        }

        else if (qName.equals("group")) {

            userMapping.addGroup(userOrGroupName, configNames);

            userOrGroupName = null;
            configNames = null;

            state.pop();

        }

        else if (qName.equals("config-ref"))
            state.pop();

    }

    /**
     * Returns the parsed configuration.
     *
     * @return a mapping from users and groups to Guacamole configurations
     */
    public UserMapping asUserMapping() {
        return userMapping;
    }

}
