/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import com.google.api.services.bigquery.model.TableFieldSchema;
//import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
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

public class DemoPipeline {
    private static final Logger LOG = LoggerFactory.getLogger(DemoPipeline.class);

    public interface MyOptions extends PipelineOptions {
        /**
         * Set this required option to specify where to write the output.
         */
        @Description("Path of the file to write to")
        @Validation.Required
        String getOutput();
        void setOutput(String value);
    }
    
    /**
     * Converts a string to a tableRow
     * input: String, output: TableRow
     */
    static class ConvertTextToRow extends DoFn<String, TableRow> {
        @ProcessElement
        public void processElement(ProcessContext c){
            TableRow row = new TableRow().set("column1", c.element());
            c.output(row);
        }
    }

    /**
     * Prepares data for writing
     */
    static class PrepareTableData extends PTransform<PCollection<String>, PCollection<TableRow>> {

        // table definition
        static TableSchema getSchema(){
            List<TableFieldSchema> fields = new ArrayList<>();
            fields.add(new TableFieldSchema().setName("column1").setType("STRING"));
            return new TableSchema().setFields(fields);
        }

        // table data
        @Override
        public PCollection<TableRow> expand(PCollection<String> stringPCollection) {
            return stringPCollection.apply(ParDo.of(new ConvertTextToRow()));
        }
    }

    public static void main(String[] args) {
        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
        Pipeline p = Pipeline.create(options);

        p.apply(Create.of("Hello", "World"))
            .apply(new PrepareTableData())
            .apply(BigQueryIO.writeTableRows()
                    .withSchema(PrepareTableData.getSchema())
                    .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                    .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
                    .to(options.getOutput()));

        p.run().waitUntilFinish();
    }
}
