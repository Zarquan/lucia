/**
 * Copy of example servlet provided with uwslib.
 *
 */
package net.ivoa.volt.uws.example;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uws.UWSException;
import uws.job.ErrorType;
import uws.job.JobList;
import uws.job.JobThread;
import uws.job.UWSJob;
import uws.job.parameters.InputParamController;
import uws.job.user.JobOwner;
import uws.service.UWSServlet;
import uws.service.UWSUrl;

public class MyUWSServlet extends UWSServlet {
	private static final long serialVersionUID = 1L;

	/* REQUIRED
	 * Initialize your UWS. At least, you should create one jobs list. */
	@Override
	public void initUWS() throws UWSException{
		addJobList(new JobList("timers"));

		addExpectedAdditionalParameter("time");
		setInputParamController("time", new InputParamController() {
			@Override
			public Object getDefault(){
				return 10;
			}

			@Override
			public Object check(Object val) throws UWSException{
				int time;
				if (val instanceof Integer)
					time = ((Integer)val).intValue();
				else if (val instanceof String){
					try{
						time = Integer.parseInt((String)val);
					}catch(NumberFormatException nfe){
						throw new UWSException(UWSException.BAD_REQUEST, nfe, "Wrong \"time\" syntax: a positive integer is expected!", ErrorType.FATAL);
					}
				}else
					throw new UWSException(UWSException.BAD_REQUEST, "Wrong \"time\" type: a positive integer is expected!");

				if (time <= 0)
					throw new UWSException("Wrong \"time\" value: \"" + time + "\"! A positive integer is expected.");
				else
					return time;
			}

			@Override
			public boolean allowModification(){
				return true;
			}
		});
	}

	/*
	 * REQUIRED
	 * Create instances of jobs, but only the "work" part. The "work" and the description of the job (and all the provided parameters)
	 * are now separated and only kept in the UWSJob given in parameter. This one is created automatically by the API.
	 * You just have to provide the "work" part.
	 */
	@Override
	public JobThread createJobThread(UWSJob job) throws UWSException{
		if (job.getJobList().getName().equals("timers"))
			return new TimerWork(job);
		else
			throw new UWSException("Impossible to create a job inside the jobs list \"" + job.getJobList().getName() + "\" !");
	}

	/* OPTIONAL
	 * By overriding this function, you can customize the root of your UWS.
	 * If this function is not overridden an XML document which lists all registered jobs lists is returned. */
	@Override
	protected void writeHomePage(UWSUrl requestUrl, HttpServletRequest req, HttpServletResponse resp, JobOwner user) throws UWSException, ServletException, IOException{
		PrintWriter out = resp.getWriter();

		out.println("<html><head><title>UWS4 example (using UWSServlet)</title></head><body>");
		out.println("<h1>UWS v4 Example (using UWSServlet)</h1");
		out.println("<p>Hello, this is an example of a use of the library UWS v4.4 !</p>");
		out.println("<p>Below is the list of all available jobs lists:</p>");

		out.println("<ul>");
		for(JobList jl : this){
			out.println("<li>" + jl.getName() + " - " + jl.getNbJobs() + " jobs - <a href=\"" + requestUrl.listJobs(jl.getName()) + "\">" + requestUrl.listJobs(jl.getName()) + "</a></li>");
		}
		out.println("</ul>");
	}

}
