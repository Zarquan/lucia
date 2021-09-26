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

import java.io.BufferedOutputStream;
import java.io.IOException;

import uws.UWSException;
import uws.job.ErrorType;
import uws.job.JobThread;
import uws.job.Result;
import uws.job.UWSJob;

/**
 * Simple Docker worker, lifetime of the Job matches the lifetime of the container. 
 *
 */
public class SimpleDockerWorker
extends JobThread
	{

	public SimpleDockerWorker(UWSJob job)
	throws UWSException {
		super(job);
		}

	@Override
	protected void jobWork()
	throws UWSException, InterruptedException
		{
		int count = 0;

		// 0. Retrieve the parameters:
		int executionTime = (int)getJob().getAdditionalParameterValue("time");

		// [OPTIONAL]
		//   Since the execution duration is known, we can easily set the quote property:
		job.setQuote(executionTime);

		// 1. EXECUTION TASK = to wait {time} seconds:
		while(!isInterrupted() && count < executionTime)
			{
			Thread.sleep(1000);
			count++;
			}

		// If the task has been canceled/interrupted, throw the corresponding exception:
		if (isInterrupted())
			{
			throw new InterruptedException();
			}

		// 2. WRITE THE RESULT FILE:
		try {
			// Create the result:
			Result result = createResult("Report");	// here, put the result name you want ; this name will be used to identify the result in the job description

			// [OPTIONAL]
			//   Set the MIME-type of the result file:
			result.setMimeType("text/plain");
			//   You could also set the length of the result (in bytes):
			// result.setSize(size);

			// Write the result:
			BufferedOutputStream output = new BufferedOutputStream(getResultOutput(result));
			output.write((executionTime + " seconds elapsed").getBytes());
			output.close();

			// Add it to the results list of this job:
			publishResult(result);

		 	}
		catch(IOException e)
			{
			// If there is an error, encapsulate it in an UWSException so that an error summary can be published:
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e, "Impossible to write the result file of the Job " + job.getJobId() + " !", ErrorType.TRANSIENT);
			}
		}
	}
