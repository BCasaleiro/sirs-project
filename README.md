# SIRS Project

Grupo de SIRS 11 - Campus Alameda

Miguel Pinto, 79060, miguelpinto25@hotmail.com  
Bernardo Casaleiro, 87827, bernardocasaleiro@gmail.com  
João Godinho, 87830, joaogodinho_4@hotmail.com  

-------------------------------------------------------------------------------

## Instruções de execução

1) Executar o **postgres**!

2) Verificar se existe alguma tabela na base de dados emergenciesdb

```
psql emergenciesdb postgres
select * from ratings;
select * from requests;
```

2.1) Se existir fazer drop das tables

```
drop table requests, ratings;
```

Para correr o projeto é necessário ir até à diretoria onde está o ficheiro **pom.xml**.

3) Compilar e instalar a dependência **certificaterequest**

```
mvn clean install
```

4) Iniciar o **Certificate Authority** com o seguinte comando:

```
mvn -Djavax.net.ssl.keyStoreType=jks -Djavax.net.ssl.trustStoreType=jks -Djavax.net.ssl.keyStore=src/main/resources/cakeystore.jks -Djavax.net.ssl.trustStore=src/main/resources/cakeystore.jks -Djavax.net.debug=ssl -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStorePassword=changeit clean install exec:java
```

5) Iniciar o **Confirmation Central** com o comando:

```
mvn -Djavax.net.ssl.keyStoreType=jks -Djavax.net.ssl.trustStoreType=jks -Djavax.net.ssl.keyStore=src/main/resources/confirmationcentralkeystore.jks -Djavax.net.ssl.trustStore=src/main/resources/cakeystore.jks -Djavax.net.debug=ssl -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStorePassword=changeit clean install exec:java
```

6) Iniciar o **Dispatch Central** com o comando:

```
mvn -Djavax.net.ssl.keyStoreType=jks -Djavax.net.ssl.trustStoreType=jks -Djavax.net.ssl.keyStore=src/main/resources/dispatchcentralkeystore.jks -Djavax.net.ssl.trustStore=src/main/resources/cakeystore.jks -Djavax.net.debug=ssl -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStorePassword=changeit clean install exec:java
``` 

7) Iniciar o **Client** com o comando:

```
mvn -Djavax.net.ssl.trustStore=src/main/resources/cakeystore.jks -Djavax.net.ssl.trustStorePassword=changeit clean install exec:java
```
