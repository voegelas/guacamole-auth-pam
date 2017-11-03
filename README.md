# guacamole-auth-pam

[Guacamole](https://guacamole.incubator.apache.org/) provides access to remote
desktops from your web browser.  This extension allows you to log into
Guacamole with PAM.

## INSTALLATION

Build the extension with Maven and copy the built JAR file to
GUACAMOLE_HOME/extensions. Example:

```
mvn package
cp target/guacamole-auth-pam-*.jar /etc/guacamole/extensions
```

Create the file /etc/pam.d/guacamole. Example for Ubuntu:

```
#%PAM-1.0
@include common-auth
@include common-account
```

Add the Tomcat user to the "shadow" group if you would like to authenticate
Guacamole users with the pam_unix(8) module:

```
usermod -a -G shadow tomcat8
```

Create the file GUACAMOLE_HOME/unix-user-mapping.xml. Add connection
configurations to the file. See the Guacamole manual for valid parameters.
Reference the configurations from user and group elements.  Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<unix-user-mapping serviceName="guacamole">
    <config name="RDP Connection" protocol="rdp">
        <param name="hostname" value="client.example.com" />
        <param name="username" value="${GUAC_USERNAME}" />
        <param name="password" value="${GUAC_PASSWORD}" />
        <param name="domain" value="EXAMPLE" />
        <param name="security" value="nla" />
        <param name="server-layout" value="en-us-qwerty" />
    </config>

    <config name="VNC Connection" protocol="vnc">
        <param name="hostname" value="localhost" />
        <param name="port" value="5901" />
        <param name="password" value="secret" />
    </config>

    <user name="andreas">
        <config-ref name="RDP Connection" />
        <config-ref name="VNC Connection" />
    </user>

    <group name="users">
        <config-ref name="RDP Connection" />
    </group>
</unix-user-mapping>
```

## DEPENDENCIES

This extension requires Java, libpam4j and Apache Guacamole.

Building the extension requires Apache Maven.

## LICENSE AND COPYRIGHT

Copyright 2017 Andreas Vögele

This extension is free software; you can redistribute and modify it under the
terms of the Apache 2.0 license.

See https://www.apache.org/licenses/LICENSE-2.0 for more information.
