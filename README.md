# Introduction

This is a demo on how to load data into Google BigQuery using Google Cloud DataFlow pipelines.

Google Cloud DataFlow is an Apache Beam Pipeline runner. A Pipeline is a sequence of data transformations applied to PCollections of Elements of different ElementTypes.

# Local Setup

### Download and Install GCP SDK

Download and install the Google Cloud SDK to your computer by running:

```bash
sudo curl https://sdk.cloud.google.com | bash
```
After installing the SDK you will have ```gcloud and gsutil``` commands to interact with GCP services.

Make sure you login before compiling the project:
```bash
gcloud auth application-default login
```
This will open the browser and redirect you the GCP services to authenticate your computer.

Optionally set your default project:

```bash
gcloud config set project <YOUR CLOUD PLATFORM PROJECT ID>
```

# Test Data

To run this demo you need to have your GCP account setup. This code can run from the developer machine on the GCP or from local system with the GCP SDK installed. 

You will also need to have some sample data in your Storage in CSV format to load to BigQuery. There is some sample data in the data folder of this project for reference.  


# Compile and Run

```bash
mvn compile exec:java -Dexec.mainClass=com.example.DemoPipeline -Dexec.args="--project=<YOUR CLOUD PLATFORM PROJECT ID> \
    --stagingLocation=<YOUR CLOUD STORAGE LOCATION> \
    --output=<YOUR CLOUD STORAGE LOCATION> \
    --jobName=<ANY JOB NAME> \
    --templateLocation=<OPTIONALLY SAVE THIS JOB AS TEMPLATE AT STORAGE LOCATION>
    --runner=DataflowRunner"  
```
