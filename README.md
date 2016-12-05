# SIRS Project

Grupo de SIRS 11 - Campus Alameda

Miguel Pinto, 79060, miguelpinto25@hotmail.com  
Bernardo Casaleiro, 87827, bernardocasaleiro@gmail.com  
João Godinho, 87830, joaogodinho_4@hotmail.com  

-------------------------------------------------------------------------------

## Instruções de execução

Os comandos mvn executam-se sempre onde está o ficheiro **pom.xml**.

Compilar e instalar a dependência do **CertificateRequest**:

```
mvn clean install
```

Primeiro iniciar o **Certificate Authority** com o seguinte comando:

```
mvn -Djavax.net.ssl.keyStoreType=jks -Djavax.net.ssl.trustStoreType=jks -Djavax.net.ssl.keyStore=src/main/resources/cakeystore.jks -Djavax.net.ssl.trustStore=src/main/resources/cakeystore.jks -Djavax.net.debug=ssl -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStorePassword=changeit clean install exec:java
```

De seguida executar o **Dispatch Central** com o comando:

```
mvn -Djavax.net.ssl.keyStoreType=jks -Djavax.net.ssl.trustStoreType=jks -Djavax.net.ssl.keyStore=src/main/resources/dispatchcentralkeystore.jks -Djavax.net.ssl.trustStore=src/main/resources/cakeystore.jks -Djavax.net.debug=ssl -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStorePassword=changeit clean install exec:java
``` 

Por último executar o **Client** com o comando:

```
mvn -Djavax.net.ssl.trustStore=src/main/resources/cakeystore.jks -Djavax.net.ssl.trustStorePassword=changeit clean install exec:java
```
