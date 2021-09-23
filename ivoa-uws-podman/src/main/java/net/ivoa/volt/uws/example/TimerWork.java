/**
 * Copy of example servlet provided with uwslib.
 *
 */
package net.ivoa.volt.uws.example;

import java.io.BufferedOutputStream;
import java.io.IOException;

import uws.UWSException;
import uws.job.ErrorType;
import uws.job.JobThread;
import uws.job.Result;
import uws.job.UWSJob;

public class TimerWork extends JobThread {

	public TimerWork(UWSJob j) throws UWSException{
		super(j);
	}

	@Override
	protected void jobWork() throws UWSException, InterruptedException{
		int count = 0;

		// 0. Retrieve the parameters:
		int executionTime = (int)getJob().getAdditionalParameterValue("time");

		// [OPTIONAL]
		//   Since the execution duration is known, we can easily set the quote property:
		job.setQuote(executionTime);

		// 1. EXECUTION TASK = to wait {time} seconds:
		while(!isInterrupted() && count < executionTime){
			Thread.sleep(1000);
			count++;
		}

		// If the task has been canceled/interrupted, throw the corresponding exception:
		if (isInterrupted())
			throw new InterruptedException();

		// 2. WRITE THE RESULT FILE:
		try{
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

		}catch(IOException e){
			// If there is an error, encapsulate it in an UWSException so that an error summary can be published:
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e, "Impossible to write the result file of the Job " + job.getJobId() + " !", ErrorType.TRANSIENT);
		}
	}

}
