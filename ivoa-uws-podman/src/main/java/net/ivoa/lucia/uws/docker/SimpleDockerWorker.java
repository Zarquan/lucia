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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.command.InspectContainerResponse;

import lombok.extern.slf4j.Slf4j;
import uws.UWSException;
import uws.job.ErrorType;
import uws.job.JobThread;
import uws.job.Result;
import uws.job.UWSJob;

/**
 * Simple Docker worker, lifetime of the Job matches the lifetime of the container. 
 *
 */
@Slf4j
public class SimpleDockerWorker
extends JobThread
	{

	public SimpleDockerWorker(UWSJob job)
	throws UWSException {
		super(job);
		}

	public static final Duration DEFAULT_LIFETIME = Duration.ofSeconds(60);
	public static final DockerImageName FEDORA_IMAGE_NAME = DockerImageName.parse("docker.io/library/fedora:34");
	public static final DockerImageName WEBTOP_IMAGE_NAME = DockerImageName.parse("ghcr.io/linuxserver/webtop:ubuntu-mate");
	public static final DockerImageName DEFAULT_IMAGE_NAME = WEBTOP_IMAGE_NAME;
	
	@Override
	protected void jobWork()
	throws UWSException, InterruptedException
		{
		Instant starttime = Instant.now();

		Duration lifetime = DEFAULT_LIFETIME ;
		String lifestr = ((String)getJob().getAdditionalParameterValue("lifetime")).trim();
    	if ((lifestr != null) ? lifestr.length() > 0 : false)
			{
			try {
		        lifetime = Duration.parse(
	        		lifestr
		    		);
	        	}
	        catch (Exception ouch)
	        	{
	        	log.error("Exception parsing lifetime [{}]", lifestr);
				throw new UWSException(
					    UWSException.BAD_REQUEST,
					    ouch,
					    "Exception parsing lifetime [" + lifestr + "]",
					    ErrorType.FATAL
					    );
	        	}
			}
        
        Instant maxtime = starttime.plus(lifetime);

		String imagestr = ((String)getJob().getAdditionalParameterValue("image")).trim();
		log.debug("image [{}]", imagestr);

		DockerImageName imagename = DEFAULT_IMAGE_NAME; 
    	if ((imagestr != null) ? imagestr.length() > 0 : false)
    		{
    		imagename = DockerImageName.parse(imagestr);
    		try {
    			imagename.assertValid();
    			}
	        catch (Exception ouch)
	        	{
	        	log.error("Exception parsing image name [{}]", imagestr);
				throw new UWSException(
				    UWSException.BAD_REQUEST,
				    ouch,
				    "Exception parsing image name [" + imagestr+ "]",
				    ErrorType.FATAL
				    );
	        	}
    		}
		
		GenericContainer<?> container = new GenericContainer<>(
			imagename 
			);
    	container.setStartupCheckStrategy(
			new MinimumDurationRunningStartupCheckStrategy(
				Duration.ofSeconds(1)
				)
			);
    	container.withExposedPorts(3000);

/*
 * 
			new MinimumDurationRunningStartupCheckStrategy(
				Duration.ofSeconds(1)
				)
			new OneShotStartupCheckStrategy()
    	
    	String commandstr = ((String)getJob().getAdditionalParameterValue("command")).trim();
    	if ((commandstr != null) ? commandstr.length() > 0 : false)
    		{
    		container.withCommand(commandstr);
    		}
 * 
 *     	
 */
    	
		log.debug("starting");
		container.start();
		String ident = container.getContainerId();
		log.debug("started");
		log.debug("ident [{}]", ident);

		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"ports\": [");
		int count = 0;
		for(Integer internal : container.getExposedPorts())
			{
			if (count > 0)
				{
				buffer.append(",");
				}
			buffer.append("{");
			Integer external = container.getMappedPort(internal);
			buffer.append("\"internal\":");
			buffer.append(internal);
			buffer.append(",");
			buffer.append("\"external\":");
			buffer.append(external);
			buffer.append("}");
			count++;
			}
		buffer.append("]");
		buffer.append("}");
		log.debug("ports [{}]", buffer.toString());

	    InspectContainerResponse inspect = container.getContainerInfo();
	    log.debug("state [{}]", inspect.getState().toString());

		stringResult("state", inspect.getState().toString());
		stringResult("ports", buffer.toString());
	    
		// Poll the container state.
		while(!isInterrupted() && (Instant.now().isBefore(maxtime)) && inspect.getState().getRunning())
		    {
    	    log.debug("waiting");
			Thread.sleep(1000);
    	    log.debug("checking");
    	    inspect = container.getContainerInfo();
	        log.debug("running [{}]", inspect.getState().getRunning());
			}

		// Stop the container.
        try {
    	    log.debug("stopping");
            container.stop();
            }
        catch (Exception ouch)
            {
    	    log.debug("Exception during stop [{}][{}]", ouch.getClass().getName(), ouch.getMessage());
            }

		// If the task has been cancelled/interrupted, throw the corresponding exception:
		if (isInterrupted())
			{
			log.debug("interrupted");
			throw new InterruptedException();
			}

		try {
			// Create the UWS result.
			Result stdout = createResult("stdout");
			stdout.setMimeType("text/plain");
			// Write the container log to the result.
			// Inefficient - converts byte[] to String to byte[] 
			Writer writer = new OutputStreamWriter(
			    getResultOutput(
			        stdout
			        )
		        );
			writer.write(
			    container.getLogs()
			    );
			writer.close();
			// Add the UWS result to the results list.
			publishResult(stdout);
			
		 	}
		catch(IOException ouch)
			{
			throw new UWSException(
			    UWSException.INTERNAL_SERVER_ERROR,
			    ouch,
			    "Exception writing stdout for job [" + job.getJobId() + "]",
			    ErrorType.TRANSIENT
			    );
			}

		// Close the container connection.
		// Needs to be in a finally block. 
        try {
    	    log.debug("closing");
            container.close();
            }
        catch (Exception ouch)
            {
    	    log.debug("Exception during close [{}][{}]", ouch.getClass().getName(), ouch.getMessage());
            }
		}

	private Result stringResult(final String name, final String value)
	throws UWSException
		{
		Result result = createResult(name);
		result.setMimeType("text/plain");
		try {
			Writer writer = new OutputStreamWriter(
			    getResultOutput(
			        result
			        )
		        );
			writer.write(
			    value
			    );
			writer.close();
			}
		catch (IOException ouch)
			{
    	    log.debug("Exception writing result [{}][{}][{}]", name, ouch.getClass().getName(), ouch.getMessage());
			}
		publishResult(result);
		return result ;
		}
	}

