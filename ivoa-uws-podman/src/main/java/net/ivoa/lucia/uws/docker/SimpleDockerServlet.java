/*
 *  Copyright (C) 2021 Royal Observatory, University of Edinburgh, UK
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of version 3 of the GNU General Public License
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package net.ivoa.lucia.uws.docker;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uws.UWSException;
import uws.job.JobList;
import uws.job.JobThread;
import uws.job.UWSJob;
import uws.job.parameters.InputParamController;
import uws.job.user.JobOwner;
import uws.service.UWSServlet;
import uws.service.UWSUrl;

public class SimpleDockerServlet
	extends UWSServlet
	{
	private static final long serialVersionUID = 1L;

	/**
	 * Initialize our UWS, creating the jobs list.
	 *
	 */
	@Override
	public void initUWS()
		throws UWSException
		{
		addJobList(
			new JobList("docker-workers")
			);

		addExpectedAdditionalParameter("image");
		setInputParamController(
			"image",
			new InputParamController()
				{
				@Override
				public Object getDefault()
					{
					return SimpleDockerWorker.DEFAULT_IMAGE_NAME.toString() ;
					}
	
				@Override
				public Object check(Object object)
				throws UWSException
					{
					return object ;
					}
	
				@Override
				public boolean allowModification()
					{
					return true;
					}
				}
			);

		addExpectedAdditionalParameter("command");
		setInputParamController(
			"command",
			new InputParamController()
				{
				@Override
				public Object getDefault()
					{
					return null;
					}
	
				@Override
				public Object check(Object object)
				throws UWSException
					{
					return object ;
					}
	
				@Override
				public boolean allowModification()
					{
					return true;
					}
				}
			);

		addExpectedAdditionalParameter("lifetime");
		setInputParamController(
			"lifetime",
			new InputParamController()
				{
				@Override
				public Object getDefault()
					{
					return "PT1M";
					}
	
				@Override
				public Object check(Object object)
				throws UWSException
					{
					return object ;
					}
	
				@Override
				public boolean allowModification()
					{
					return true;
					}
				}
			);
		
		}

	/**
	 * Create instances of jobs, but only the "work" part. The "work" and the description of the job (and all the provided parameters)
	 * are now separated and only kept in the UWSJob given in parameter. This one is created automatically by the API.
	 * You just have to provide the "work" part.
	 * 
	 */
	@Override
	public JobThread createJobThread(UWSJob job)
	throws UWSException
		{
		if (job.getJobList().getName().equals("docker-workers"))
			{
			return new SimpleDockerWorker(job);
			}
		else {
			throw new UWSException("Impossible to create a job inside the jobs list \"" + job.getJobList().getName() + "\" !");
			}
		}

	/**
	 * By overriding this function, you can customize the root of your UWS.
	 * If this function is not overridden an XML document which lists all registered jobs lists is returned.
	 *
	 */
	@Override
	protected void writeHomePage(UWSUrl requestUrl, HttpServletRequest req, HttpServletResponse resp, JobOwner user)
	throws UWSException, ServletException, IOException
		{
		PrintWriter out = resp.getWriter();

		out.println("<html><head><title>UWS4 example (using UWSServlet)</title></head><body>");
		out.println("<h1>UWS v4 Example (using UWSServlet)</h1");
		out.println("<p>Hello, this is an example of a use of the library UWS v4.4 !</p>");
		out.println("<p>Below is the list of all available jobs lists:</p>");

		out.println("<ul>");
		for(JobList jl : this)
			{
			out.println("<li>" + jl.getName() + " - " + jl.getNbJobs() + " jobs - <a href=\"" + requestUrl.listJobs(jl.getName()) + "\">" + requestUrl.listJobs(jl.getName()) + "</a></li>");
			}
		out.println("</ul>");
		}
	}
