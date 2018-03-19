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

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Basic ParDo with DoFn transformation
     */
    static class PrintElement extends DoFn<String, Void> {
        @ProcessElement
        public void processElement(ProcessContext c){
            LOG.info(c.element());
        }
    }

    // public static UppercaseElements extends PTransform<String, String> {
    //     @Override
    //     public String apply(String input){
    //         return input.toUppderCase();
    //     }
    // }

    public static void main(String[] args) {
        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
        Pipeline p = Pipeline.create(options);

        p.apply(Create.of("Hello", "World"))
                //.apply(TextIO.Read.from(inputFile)
                .apply(MapElements.via(new SimpleFunction<String, String>() {
                    @Override
                    public String apply(String input) {
                        return input.toUpperCase();
                    }
                }))
                .apply("Print Elements", ParDo.of(new DemoPipeline.PrintElement()));
        p.run();
    }
}
