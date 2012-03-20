@echo on

set path_cp=%HOME%/.m2/repository
set app_cp=./
set app_cp=%app_cp%;%path_cp%/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar

set app_cp=%app_cp%;../target/fragile_apserver-0.1.0-SNAPSHOT.jar

echo http://localhost:8081/

java -cp %app_cp% jp.gr.java_conf.fragile.service.net.example.Main
