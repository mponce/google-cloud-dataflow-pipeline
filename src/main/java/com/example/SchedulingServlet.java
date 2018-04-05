package com.example;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class SchedulingServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UUID uuid = UUID.randomUUID();
        String jobId = "scheduled-job-" + uuid.toString();
        DataPipeline pipeline = new DataPipeline();
        pipeline.setJobId(jobId);
        Thread t = new Thread(pipeline);
        t.start();
        resp.setContentType("text/plain");
        resp.getWriter().println(jobId + " scheduled!");
    }
}
