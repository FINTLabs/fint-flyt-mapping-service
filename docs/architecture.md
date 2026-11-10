# Architecture

C4 level 1 to 3 for fint-flyt-mapping-service. Blue is this service, turquoise is external.

## C4 level 1: System context

A gateway registers an instance, this service maps it, and archive-gateway sends it to the archive.

```mermaid
C4Context
    title System context for fint-flyt-mapping-service

    System_Ext(history, "fint-flyt-history-service", "Registers instances and records every event in the flow.")
    System_Ext(gateway, "FLYT gateways", "Receive instances from source applications.")
    System(mapping, "fint-flyt-mapping-service", "Maps an instance to the format of the destination.")
    System_Ext(intgrConf, "fint-flyt-intgr-conf-service", "Integrations, configurations and value convertings.")
    System_Ext(archive, "fint-flyt-archive-gateway", "Sends mapped instances to the archive.")
    System_Ext(idp, "NAM", "Issues OAuth2 tokens.")

    Rel(gateway, history, "Registers")
    BiRel(history, mapping, "Instance events", "Kafka")
    Rel(mapping, archive, "instance-mapped", "Kafka")
    Rel(mapping, idp, "Fetches token", "client_credentials")
    Rel(mapping, intgrConf, "Configuration", "HTTP")

    UpdateRelStyle(gateway, history, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(history, mapping, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(mapping, archive, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(mapping, idp, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(mapping, intgrConf, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateElementStyle(mapping, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(gateway, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(history, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(archive, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(idp, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(intgrConf, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

`instance-registered` is still also produced by fint-flyt-instance-service, which is being merged
into history-service.

## C4 level 2: Containers

One Spring Boot application with no database. Everything it needs per instance is fetched from
intgr-conf-service.

```mermaid
C4Container
    title Containers

    ContainerQueue_Ext(kafkaIn, "Kafka", "Topic", "instance-registered, instance-requested-for-retry")
    System_Ext(prometheus, "Prometheus", "Metrics in the cluster.")
    Container(app, "fint-flyt-mapping-service", "Kotlin, Spring Boot 3, Java 25", "Consumes instances, maps them and publishes the result. No database.")
    Container_Ext(intgrConf, "fint-flyt-intgr-conf-service", "Spring Boot", "/api/intern-klient")
    ContainerQueue_Ext(kafkaOut, "Kafka", "Topic", "instance-mapped, instance-error")
    System_Ext(idp, "NAM", "fint-idp token endpoint.")

    Rel(kafkaIn, app, "Consumes")
    Rel(prometheus, app, "Scrapes", "HTTP")
    Rel(app, kafkaOut, "Publishes")
    Rel(app, idp, "Fetches token", "client_credentials")
    Rel(app, intgrConf, "Configuration", "HTTP")

    UpdateRelStyle(kafkaIn, app, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(prometheus, app, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(app, kafkaOut, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(app, idp, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(app, intgrConf, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateElementStyle(app, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(kafkaIn, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(kafkaOut, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(prometheus, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(idp, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(intgrConf, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

## C4 level 3: Components

```mermaid
C4Component
    title Components in fint-flyt-mapping-service

    Component(consumer, "Kafka consumers", "flyt-kafka", " Retries with exponential backoff, then GENERAL_SYSTEM_ERROR.")
    ContainerQueue_Ext(kafka, "Kafka", "Topic", "instance-registered, instance-requested-for-retry, instance-mapped, instance-error")
    Component(processing, "InstanceProcessingService", "Service", "Fetches configuration, maps, publishes the result or an error event.")
    Component(producers, "Kafka producers", "flyt-kafka", "Publish instance-mapped and instance-error.")
    Component(client, "IntgrConfClient", "RestClient", "OAuth2 token from NAM. 404 gives null.")
    Component(mapping, "InstanceMappingService", "Service", "Walks the ObjectMapping. ValueMappingService and the reference services resolve $if, $icf and $vc.")
    Container_Ext(intgrConf, "fint-flyt-intgr-conf-service", "Spring Boot", "/api/intern-klient")
    Component(valueConverting, "ValueConvertingService", "Service", "Looks up the instance value in a value converting.")

    Rel(kafka, consumer, "Consumes")
    Rel(consumer, processing, "process()")
    Rel(processing, producers, "Publishes")
    Rel(producers, kafka, "Sends")
    Rel(processing, client, "Fetches")
    Rel(processing, mapping, "Maps")
    Rel(mapping, valueConverting, "VALUE_CONVERTING")
    Rel(valueConverting, client, "Value converting")
    Rel(client, intgrConf, "GET", "HTTP")

    UpdateRelStyle(kafka, consumer, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(consumer, processing, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(processing, producers, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(producers, kafka, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(processing, client, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(processing, mapping, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(mapping, valueConverting, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(valueConverting, client, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(client, intgrConf, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateElementStyle(consumer, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(processing, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(producers, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(client, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(mapping, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(valueConverting, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(kafka, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(intgrConf, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```
