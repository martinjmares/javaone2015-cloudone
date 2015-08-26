# JavaOne 2015: Cloud One

Sample project for JavaOne 2015 presentation - "Rest Conversation". Main goal is to elaborate around REST based integration patterns in the cloud solution.

## Introduction

This project represents simulation of the simplified cloud solution. The cloud architecture is oriented for JAX-RS based micro-services. 

Idea is, that each micro-service is executed as a new JVM instance (or 'cluster' of the new JVM instances). Each micro-service must be based on the same cloud framework. It means that defines extension of the C1Application class. (BTW: It is just extension of the JAX-RS Application class.) The java Main (class with main(String[]) is defined by the cloud library. 

Cloud management is represented by one application which provides several cloud services including configuration, monitoring and naming. 

We defined this simplified cloud solution to present one specific idea: New ReST based integration patterns between application – especially in one cloud or on one environment. 

## Project Structure

We named our cloud: CloudOne. You know, it is presentation for JavaOne, so …
CloudOne libraries and executable jars are named based on cloud types.

- **Cirrus** is basic shared library for all CloudOne applications
- **Cumulonimbus** is central cloud service. In this demo it does not support clustering. It must be executed ones per cloud and it must be local for all other executed applications. You know, simple.
- **Stratus** is pom which should be used as a parent for all CloudOne micro-services.
- **Nimbostratus** is C1Application and Main class for all micro-services. Provides bootstrap functionality and administration interface for the CloudOne instance. Communication interface for Cumulonimbus.

## Build

This is pure Maven2 based project. So, use ```mvn clean install```
