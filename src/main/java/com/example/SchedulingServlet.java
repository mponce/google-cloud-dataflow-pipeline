package com.example;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SchedulingServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        DataPipeline pipeline = new DataPipeline();
	Thread t = new Thread(pipeline);
	t.start();
        resp.setContentType("text/plain");
        resp.getWriter().println("job scheduled");
    }
}
