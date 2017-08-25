# guacamole-auth-pam

Guacamole provides access to remote desktops from your web browser.  See
https://guacamole.incubator.apache.org/ for information on Guacamole.  The
guacamole-auth-pam extension allows you to log into Guacamole with PAM.

## INSTALLATION

Build the extension with Maven and copy the built JAR file to
GUACAMOLE_HOME/extensions. Example:

```
mvn package
cp target/guacamole-auth-pam-*.jar /etc/guacamole/extensions
```

Create the file /etc/pam.d/guacamole. Example for Debian/Ubuntu:

```
@include common-auth
@include common-account
```

Create the file GUACAMOLE_HOME/unix-user-mapping.xml. Add connection
configurations to the file. See the Guacamole manual for valid parameters.
Reference the configurations from user and group elements.  Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<unix-user-mapping serviceName="guacamole">
    <config name="RDP Connection" protocol="rdp">
        <param name="hostname" value="localhost" />
        <param name="username" value="${GUAC_USERNAME}" />
        <param name="password" value="${GUAC_PASSWORD}" />
    </config>

    <config name="VNC Connection" protocol="vnc">
        <param name="hostname" value="localhost" />
        <param name="port" value="5901" />
        <param name="password" value="VNCPASS" />
    </config>

    <user name="USERNAME">
        <config-ref name="RDP Connection" />
        <config-ref name="VNC Connection" />
    </user>

    <group name="GROUPNAME">
        <config-ref name="RDP Connection" />
    </group>
</unix-user-mapping>
```

## DEPENDENCIES

This extension requires Java, libpam4j and Apache Guacamole.

Building the extension requires Apache Maven.

## LICENSE AND COPYRIGHT

Copyright 2017 Andreas Vögele

This extension is free software; you can redistribute it and/or modify it
under the terms of the Apache 2.0 license.

See https://www.apache.org/licenses/LICENSE-2.0 for more information.
