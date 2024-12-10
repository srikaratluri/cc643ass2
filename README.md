### 1. SSH into 4 Instances
To SSH into your 4 instances, use the following command for each instance:

```bash
ssh -i /path/to/your/private-key.pem ubuntu@<instance-ip>
```
Repeat this step for all 4 instances.

---

### 2. Generate SSH Keys on All 4 Instances
On each of the instances, execute the following commands:

```bash
ssh-keygen -t rsa -N "" -f /home/ubuntu/.ssh/id_rsa
cd ~/.ssh
ls -lrt  # To see the generated keys
cat ~/.ssh/id_rsa.pub  # To display the public key
```

Make sure to note down the output of the `cat ~/.ssh/id_rsa.pub` command, as you will need to copy this public key to the other instances.

---

### 3. Add Public Keys to `authorized_keys` on All 4 Instances
On each instance, execute the following:

1. Open the `authorized_keys` using nano:

```bash
nano ~/.ssh/authorized_keys
```

2. Copy and paste the public keys from all 4 instances into this file. Each key should be on a new line.

---

### 4. Edit `/etc/hosts` on All 4 Instances
On each instance, add the following entries to `/etc/hosts` to define hostnames for each instance:

1. First, navigate to the home directory:

```bash
cd
```

2. Open the `/etc/hosts` file in `nano`:

```bash
sudo nano /etc/hosts
```

3. Add the following lines:

```
<ip-address> nn
<ip-address> dd1
<ip-address> dd2
<ip-address> dd3
```

Replace `<ip-address>` with the actual IP addresses of your instances.

---

### 5. Install Java, Maven, and Spark 3.4.1 on All 4 Instances
On each instance, install Java, Maven, and Spark as follows:

Install Java:
```bash
sudo apt update
sudo apt install openjdk-11-jdk -y
java -version  # To verify installation

Install Spark 3.4.1:

1. Download Spark from the official link (e.g., Spark 3.4.1):
```bash
wget https://archive.apache.org/dist/spark/spark-3.4.1/spark-3.4.1-bin-hadoop3.2.tgz
```

2. Extract Spark:
```bash
tar -xvzf spark-3.4.1-bin-hadoop3.2.tgz
```

3. Set Spark environment variables:
```bash
echo "export SPARK_HOME=/home/ubuntu/spark-3.4.1-bin-hadoop3.2" >> ~/.bashrc
echo "export PATH=\$SPARK_HOME/bin:\$PATH" >> ~/.bashrc
source ~/.bashrc
```

---

### 6. Copy `workers.template` to `workers`
Now, copy the `workers.template` file to `workers`:

```bash
cp $SPARK_HOME/conf/workers.template $SPARK_HOME/conf/workers
```

---

### 7. Edit `workers` File
Open the `workers` file in `nano` and add the following lines after `localhost`:

```bash
nano $SPARK_HOME/conf/workers
```

Add the following (replace `dd1/ip-address`, `dd2/ip-address`, `dd3/ip-address` with the actual IP addresses of your instances):

```
localhost
dd1/ip-address
dd2/ip-address
dd3/ip-address
```

---

### 8. Install Spark 3.4.3, Java.

---

### 9. Create Folders for Training and Eval and Place Java Codes
On each instance, create two folders named `Training` and `Eval`:

```bash
mkdir /opt/Project
```
use touch command to create a ModelTrainer.java and Predictor.java

Paste respective Java code files (for training and evaluation) inside these folders.

---


### 10. Run the Training Code in Parallel Using `spark-submit`
To run your training code using `spark-submit` on all instances in parallel, use the following command:

```bash
$SPARK_HOME/bin/spark-submit --class ModelTrainer --master spark://master-ip --deploy-mode client --executor-memory 2G --total-executor-cores 4 /opt/ModelTrainer.jar
```

Replace ip with the your IP address of the Spark master instance.

---
### 10.1 Run the Predictor Using `spark-submit`
To run your training code using `spark-submit` on all instances in parallel, use the following command:

```bash
$SPARK_HOME/bin/spark-submit --class Predictor --master spark://master-ip --deploy-mode client --executor-memory 2G --total-executor-cores 4 /home/ubuntu/Predictor.jar
```

Replace ip with the our IP address of the Spark master instance.

 you should get the output in this manner


 
 ![Screenshot 2024-12-09 153516](https://github.com/user-attachments/assets/63903a1d-78e8-467f-9ab5-2a8afa188b60)




```bash
$SPARK_HOME/bin/spark-submit --class Predictor --master spark://master-ip --deploy-mode client --executor-memory 2G --total-executor-cores 4 /home/ubuntu/Predictor.jar
```

### 11. Create a Docker Image
```
FROM openjdk:21-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y apt-utils wget unzip
RUN wget https://dlcdn.apache.org/spark/spark-3.5.3/spark-3.5.3-bin-hadoop3.tgz && \
    tar -xvzf spark-3.5.3-bin-hadoop3.tgz && \
    mv spark-3.5.3-bin-hadoop3 /opt/spark && \
    rm spark-3.5.3-bin-hadoop3.tgz
ENV SPARK_HOME=/opt/spark
ENV PATH="$SPARK_HOME/bin:$PATH"
COPY Predictor.jar /app/Predictor.jar
COPY TrainingDataset.csv /app/TrainingDataset.csv
COPY ValidationDataset.csv /app/ValidationDataset.csv
COPY app /app/app
COPY classes /app/classes
COPY wine_quality_model /app/wine_quality_model
CMD ["/opt/spark/bin/spark-submit", "--class", "Predictor", "--master", "local[*]", "/app/Predictor.jar"]

```





![Screenshot 2024-12-09 220031](https://github.com/user-attachments/assets/81a3eac9-c527-4535-a01a-9354eec361ba)








### 12. Run the Docker Container
Once the image is pulled, you can run it with:

```bash
sudo docker build -t wine-quality-predictor .
sudo docker run --rm -it -v /home/ubuntu/wine_quality_model:/app/wine_quality_model wine-quality-predictor
```
This will start the container and open a bash shell inside the container. You can then execute any further commands required.

---

---


### 13. Push the Docker Image
Once the image is built, you can push it to Docker Hub:
```bash
sudo docker login
```

```bash
sudo docker push srikaratluri3010/wine-quality-predictor:latest
```

Make sure you're logged in to Docker Hub using:



---
---




