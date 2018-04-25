/*
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.example;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import com.google.api.services.bigquery.model.TableRow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DataPipeline implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private static final String FIELD_SEPARATOR = ",";

    // FIXME: Replace with your GCP Options
    private static final String GCP_PROJECT_ID = "**REPLACE-ME**";
    private static final String GCP_BIG_QUERY_DATASET = "**REPLACE-ME**";
    private static final String GCP_BIG_QUERY_TABLE = "**REPLACE-ME**";

    private String jobId = "**REPLACE-ME**";

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public interface MyOptions extends DataflowPipelineOptions {
        /**
         * Specify the location of CSV files
         * For example: gs://my-project/input/**
         */
        @Description("Path of the CSV files to read from")
        String getInputFile();
        void setInputFile(String value);

        /**
         * Set this required option to specify where to write the output.
         */
        @Description("Path of the file to write to")
        @Validation.Required
        String getOutput();
        void setOutput(String value);
    }

    /**
     * Converts a String element to a TableRow element.
     * input: String
     * output: TableRow
     */
    static class ConvertTextToRow extends DoFn<String, TableRow> {
        @ProcessElement
        public void processElement(ProcessContext c){
            String[] columns = { "ID", "DESCRIPTION", "PRICE" };
            String[] data = c.element().split(FIELD_SEPARATOR);
            TableRow row = new TableRow();
            int i = 0;
            for (String elem : data){
                row.set(columns[i], elem);
                i++;
            }
            c.output(row);
        }
    }

    /**
     * A Transformation to prepare data for writing to BigQuery
     * input: PCollection<String>
     * output: PCollection<TableRow>
     */
    static class PrepareTableData extends PTransform<PCollection<String>, PCollection<TableRow>> {

        // BigQuery output table definition
        static TableSchema getSchema(){
            logger.info("Getting BigQuery TableSchema");
            List<TableFieldSchema> fields = new ArrayList<TableFieldSchema>();
            fields.add(new TableFieldSchema().setName("ID").setType("STRING"));
            fields.add(new TableFieldSchema().setName("DESCRIPTION").setType("STRING"));
            fields.add(new TableFieldSchema().setName("PRICE").setType("STRING"));
            return new TableSchema().setFields(fields);
        }

        // Apply a ParDo Transformation to convert each String row to TableRow
        @Override
        public PCollection<TableRow> expand(PCollection<String> stringPCollection) {
            return stringPCollection.apply(ParDo.of(new ConvertTextToRow()));
        }
    }

    public void run() {

        logger.info("Starting DataPipeline");

        // Google Cloud DataFlow Options
        MyOptions options = PipelineOptionsFactory.as(MyOptions.class);
        options.setJobName(this.jobId);
        options.setProject(GCP_PROJECT_ID);
        options.setTempLocation("gs://" + GCP_PROJECT_ID + "/staging");
        options.setStagingLocation("gs://" + GCP_PROJECT_ID + "/staging");
        options.setInputFile("gs://" + GCP_PROJECT_ID + "/csv/products.csv");
        options.setOutput(GCP_PROJECT_ID + ":" + GCP_BIG_QUERY_DATASET + "." + GCP_BIG_QUERY_TABLE);
        options.setRunner(DataflowRunner.class);

        // Create the Pipeline with the specified options.
        Pipeline p = Pipeline.create(options);

        logger.info("Applying Transformations");

        // Apply Transformations
        p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
                .apply("PrepareToWrite",new PrepareTableData())
                .apply("WriteData",BigQueryIO.writeTableRows()
                        .withSchema(PrepareTableData.getSchema())
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
                        .to(options.getOutput()));

        // Run the Pipeline
        logger.info("Running Pipeline");
        p.run();
    }
}
