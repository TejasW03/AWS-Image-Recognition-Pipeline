# AWS-Image-Recognition-Pipelin

This project demonstrates a distributed image recognition pipeline using Amazon Web Services (AWS). Two EC2 instances work in parallel to identify cars in images and extract any text present in those images using AWS Rekognition.

---

## Project Overview

This system uses the following AWS services:

- **EC2** for computation
- **S3** to host images
- **SQS** to manage communication between instances
- **Rekognition** for image and text detection

### Workflow:

1. **EC2 A** downloads images from an S3 bucket and uses Rekognition to detect cars.
2. If a car is found (with >90% confidence), the image index is sent to **SQS**.
3. **EC2 B** reads from the SQS queue and performs text recognition on those images.
4. EC2 B writes the results (image index and detected text) to a file.

---

## Cloud Environment Setup

### Launch EC2 Instances

- Launch **2 EC2 instances** with **Amazon Linux AMI**
- Use the **same key pair (.pem)** for both instances
- In the **Security Group**:
  - Set source IP to `MyIP`
  - Allow only ports **22 (SSH)**, **80 (HTTP)**, and **443 (HTTPS)**

---

### SSH Access

Use the following command (replace with your public DNS):

```bash
ssh -i "path/to/CS643.pem" ec2-user@<EC2_PUBLIC_DNS>
```

---

### Software Installation

Install Maven

```bash
sudo yum install -y maven
mvn -v
```

Install Java 8 (Amazon Corretto)

```bash
sudo rpm --import https://yum.corretto.aws/corretto.key
sudo curl -Lo /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo
sudo yum install -y java-1.8.0-amazon-corretto-devel
```

### Configure AWS Credentials

Create AWS credentials file:

```bash
mkdir ~/.aws
nano ~/.aws/credentials
```

Paste in:

```ini
[default]
aws_access_key_id=YOUR_ACCESS_KEY
aws_secret_access_key=YOUR_SECRET_KEY
aws_session_token=YOUR_SESSION_TOKEN
```

Note: Session tokens expire every 3 hours in AWS Educate. Refresh from [Vocareum > Account Details > "Access your credentials"].

## Maven Project Setup (Both EC2 A & B)

```bash
mkdir image-recognition
cd image-recognition

mvn archetype:generate \
  -DgroupId=com.aws.imagerecognition \
  -DartifactId=image-recognition \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd image-recognition
```

Replace App.java in src/main/java/com/aws/imagerecognition/ with:

- App_CarRcg.java on EC2 A

- App_TxtRcg.java on EC2 B

Also, edit and configure the pom.xml file with required AWS dependencies.

## Build and Run the Application

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.aws.imagerecognition.App"
```

## File Descriptions

| File              | Description                                                   |
| ----------------- | ------------------------------------------------------------- |
| `App_CarRcg.java` | Java program for detecting cars using AWS Rekognition (EC2 A) |
| `App_TxtRcg.java` | Java program for text recognition on car images (EC2 B)       |
| `README.md`       | Documentation                                                 |
