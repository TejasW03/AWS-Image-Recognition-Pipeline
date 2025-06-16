
How to set-up the cloud environment and run the application.

- Setup AWS EC2 and SQS

- Login using SSH

ssh -i "C:\Users\Tejas\Documents\aws\key\CS643.pem" ec2-user@ec2-52-71-252-119.compute-1.amazonaws.com

ssh -i "C:\Users\Tejas\Documents\aws\key\CS643.pem" ec2-user@ec2-52-90-224-217.compute-1.amazonaws.com

- Install Maven and verify version
sudo yum install -y maven
mvn -v

- Install Java 
sudo rpm --import https://yum.corretto.aws/corretto.key
sudo curl -Lo /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo
sudo yum install -y java-1.8.0-amazon-corretto-devel

-Setup AWS Credentials

mkdir ~/.aws

nano ~/.aws/credentials

[default]
aws_access_key_id=ASIA5GZ6AGWYIGHA75QJ
aws_secret_access_key=MRm0dlCH98Ns/nGxuCjj+DZpybmpoVbRWfhIpqVess
7aws_session_token=IQoJb3JpZ2luX2VjEBUaCXVzLXdlc3QtMiJIMEYCIQCfh+BMpQ194iD061ddmXONe+gSFM+gwkB40dUnULYUSwIhAPcOwcVbVPkK/gE23O+y8AXm9JGg/i1c8ScJKWBz1pnQKqgCCH4QAhoMOTA3OTc4NzQ5MzYwIgyxHNlEoOBg4Gd4iEYqhQK+UV37KQ8DwSoe02DSSW98lKb4E/FNC1T02QDRSZ3tvX8w8+fujXZW8yDLc1orescqpFcDV5wDnhFwqE1H4LwpSrMtiW5bYgfHPf3oup6bzfC+Ec/vRTOCqZesswobFajQ+p38K3RjnINvp7WYZDBZ7I8F63ARRLfinKE9zn9YNlD0KdyZprbkSjNCUZhv8k4rMFmq2jWd0DlZsu9zo3dtc1cxSwHTh7Cya1W4jgNbMuZayfpDCHhss7N27Ej4EJgROsKoapBeC5hbgN2AYSyVI6nXPcPHTnh1lYNjHR2hKqU6ib7U47/TpPyPnHXBNwqsuJO7WgAYTyoELhNj472rROFuSk8w/NnVuAY6nAGaoS5fQzmDP40/j+mSY1OXL+mrtgIjSTXpsD4uosBu9wc3HOpJebS1rkkiTZ94tAT3bXa48Cqv1w95nxFBp5Qx5RmmocgCVNkFyz6j2j7MpZyOVH7qL5oX9hMtf1ATLM2pxGvVB4s0Tc+pXt8NwXQwmPO/dBJMOouyt4fB1TPYwP5C7i1ixfipAXGMmzHDHLInLDgn7hwOou1/2DM=

- Car Recognition & Text Recognition Maven Setup 

mkdir image-recognition
cd image-recognition

Create Projecet: mvn archetype:generate -DgroupId=com.aws.imagerecognition -DartifactId=image-recognition -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

nano src/main/java/com/aws/imagerecognition/App.java

Setup pom.xml file: nano pom.xml

build project: mvn clean install

Run: mvn exec:java -Dexec.mainClass="com.aws.imagerecognition.App"


