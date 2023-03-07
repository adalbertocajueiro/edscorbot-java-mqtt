# Getting Started

This repository contains a simple Java/Spring Boot implementation for the arm controller of the ED Scorbot project.  

### Tools Information
This project has been developed using the following tools:
* Java version 18.0.1.1
* Apache Maven version 3.8.5
* Spring Boot version 3.0.2
* Visual Studio Code version 1.74.3 with extensions: Extension Pack for Java and Maven for Java. 
* AsyncAPI CLI for generating the API documentation.  
* [Mosquitto Broker](https://mosquitto.org/)
* Mosquitto tools (`mosquitto_pub` and `mosquitto_sub`) for testing purposes

### Install instructions
* You can try to use your own versions of Java and Maven. If it does not work we advice to install the above versions
* Download de project and unzip it
* Open the project in Visual Studio Code (vscode). It might be possible vscode offers other extensions to be installed. Just accept it.
* Folder `docs` contains the generated HTML documentation of EDScorbot Controller (AsyncAPI)
* (Step 1 - optional) If you want to install the AsyncAPI generator globally run `sudo npm install -g @asyncapi/cli`  `OR` if you want to install only in this project run `npm install @asyncapi/generator`
* (Step 2 - optional) The documentation generator requires a template to format its output. To install the most common template (HTML) run `npm install @asyncapi/html-template`
* Steps 1 and 2 install the necessary dependencies to re-generate the documentation and need to run only at once. As the project has already a package.json file containing the names of all dependencies to generate documentation, you can also just run `npm install` and the above dependencies will be installed.
* (Step 3 - optional) The file `edscorbot-async-api.yaml` contains a valid specification of its AsyncAPI. If you want to generate its documentation in `docs` folder run `asyncapi generate fromTemplate edscorbot-async-api.yaml @asyncapi/html-template -o docs`  
* For more information about customizing doc generation please refer to (https://www.asyncapi.com/docs/tools/generator/usage)
* Configure Mosquitto to start a tcp listener on port 1883 (to accept messages from the controller) and a web sockets listener on port 8080 (to accept messages from angular): edit your conf file (normally mosquitto.conf) and add these lines:
  - `listener 1883`
  - `listener 8080`
  - `protocol websockets`
  - `allow_anonymous true`
* Start you Mosquitto Broker in localhost and port 1883. Open a terminal and start mosquitto. Normally this is achieved by running the command `mosquitto`. The terminal remains watching the broker execution. 
* Run the class EdscorbotMqttApplication.java and the server should start. This step is more user friendly if executed from the vscode editor, as it presents options for Run and Debug above the `main` method.
* Open a new terminal to simulate subscribers (arm's clients) receiving messages from the controller. Run the command `mosquitto_sub -h localhost -p 1883 -t "EDScorbot/metainfo"  -t "EDScorbot/commands" -t "EDScorbot/moved" -q 0` to launch a consumer subscribed in all channels. The terminal remains watching the consumer and shows the messages delivered to it. 
* Open a new terminal to simulate clients trying to interact with the controller. Run the commands:
  - `mosquitto_pub -h localhost -p 1883 -t "EDScorbot/metainfo" -m "{ \"signal\": 1}" -q 0` to publish a message of a client requesting the meta info of all arms (see it in the consumer terminal)
  - `mosquitto_pub -h localhost -p 1883 -t "EDScorbot/commands" -m "{ \"signal\": 3}" -q 0` to publish a message of a client requesting the status of the arm (see it in the consumer terminal)
  - `mosquitto_pub -h localhost -p 1883 -t "EDScorbot/commands" -m "{ \"signal\": 5, \"client\"={ \"id\": \"adalberto.cajueiro@gmail.com\"}}" -q 0` to publish a message of a client who wants to connect with the arm. The broker prints a message in its console and sends a message to the consumers containing the new owner of the arm (see it in the consumer terminal)
  - `mosquitto_pub -h localhost -p 1883 -t "EDScorbot/commands" -m "{ \"signal\": 8, \"client\"={ \"id\": \"adalberto.cajueiro@gmail.com\"}, \"content\": { \"coordinates\": [10.0,10.0,10.0,10.0]}}" -q 0` to publish a message requesting to move the arm to a specific point (see the last point in the consumer terminal)
  - `mosquitto_pub -h localhost -p 1883 -t "EDScorbot/commands" -m "{ \"signal\": 9,\"client\"={ \"id\": \"adalberto.cajueiro@gmail.com\"}, \"content\": { \"points\": [ { \"coordinates\": [10.0,10.0,10.0,10.0]},{ \"coordinates\": [20.0,20.0,20.0,20.0]},{ \"coordinates\": [30.0,30.0,30.0,30.0]},{ \"coordinates\": [40.0,40.0,40.0,40.0]},{ \"coordinates\": [50.0,50.0,50.0,50.0]},{ \"coordinates\": [60.0,60.0,60.0,60.0]},{ \"coordinates\": [70.0,70.0,70.0,70.0]},{ \"coordinates\": [80.0,80.0,80.0,80.0]} ] }}" -q 0` to publish a message requesting to apply a trajectory. The broker prints a message in its console and sends messages to the consumers (one message for each point of the trajectory) informing that the arm was moved accordingly (see the points in the consumer terminal)
  - `mosquitto_pub -h localhost -p 1883 -t "EDScorbot/commands" -m "{ \"signal\": 10, \"client\"={ \"id\": \"adalberto.cajueiro@gmail.com\"}}" -q 0` to publish a message requesting to cancel a trajectory execution. The controller publishes the last point executed (see it in the consumer terminal)
  - `mosquitto_pub -h localhost -p 1883 -t "EDScorbot/commands" -m "{ \"signal\": 7, \"client\"={ \"id\": \"adalberto.cajueiro@gmail.com\"}}" -q 0` to publish a message requesting to disconnect from the arm. The broker prints a message in its console and sends a message to the consumers containning the current status of the arm (see it in the consumer terminal)

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.2/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.0.2/maven-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.0.2/reference/htmlsingle/#using.devtools)

