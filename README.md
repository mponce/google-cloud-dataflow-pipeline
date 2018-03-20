### Introduction

This is a demo on how to load data into Google BigQuery using Google Cloud DataFlow pipelines.

Google Cloud DataFlow is an Apache Beam Pipeline runner. A Pipeline is a sequence of data transformations applied to PCollections of Elements of different ElementTypes.

### Local SDK Setup


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

### Test Data

To run this demo you need to have your GCP account setup. This code can run from the developer machine on the GCP or from local system with the GCP SDK installed. 

You will also need to have some data in your Cloud Storage in CSV format to load to BigQuery. There is some sample data in the data folder of this project for reference.  

For example under the your project Storage create a directory structure for data, staging and templates.

**Storage Structure:**
```text
- <PROJECT-ID>/
     +---/data/
         +---/products.csv
             /customers.csv
     +---/staging/ (If you remove this folder running the Template will fail)
     +---/templates/
         +---/DemoPipeline (this will be created by mvn compile command line)
     +---/output/
```

### Compile and Run

```bash
mvn compile exec:java -Dexec.mainClass=com.example.DemoPipeline -Dexec.args="--project=<PROJECT_ID> \
    --jobName=<JOB_NAME> \
    --stagingLocation=gs://<PROJECT_ID>/staging \
    --output=<PROJECT_ID>:<BIG_QUERY_DATASET_NAME>.<TABLE_NAME> \
    --runner=DataflowRunner"  \
    --templateLocation=gs://<PROJECT_ID>/templates/DemoPipeline <-- optional: use only if later want to run from UI!
```

### Template Metadata

Upload the file ```DemoPipeline_metadata``` to your Cloud Storage template location ```<PROJECT_ID>/templates``` this will help the DataFlow UI automatically display the parameters required to run our DemoPipeline.

### Run from DataFlow UI

Login to GCP console under **DataFlow** service create a new job from *Custom Template* and select **DemoPipeline** the template from the ```<PROJECT_ID>/templates``` folder. The Dataflow UI will read the **DemoPipeline_metadata** file (previously uploaded) to render the input parameters. Fill in those parameters and Run the job. 

If all goes well you should be able to see the Job status (running, processed etc.) and options to see the Job *LOGS*.
