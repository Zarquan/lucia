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
 * Initial experiments, build from Spring Boot examples.
 * https://spring.io/guides/gs/spring-boot/
 * https://spring.io/guides/gs/actuator-service/
 *
 */
package net.ivoa.volt.uws.podman;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.core.DockerClientBuilder;

import lombok.extern.slf4j.Slf4j; 

//@SpringBootTest
//@AutoConfigureMockMvc
@Slf4j
public class DockerTest
    {
	static String ident ;
	
    @Test
    public void checkOne() throws Exception
        {
		GenericContainer<?> container = new GenericContainer<>(
			DockerImageName.parse("ghcr.io/linuxserver/webtop:ubuntu-mate")
			);
    	container.setStartupCheckStrategy(
			new MinimumDurationRunningStartupCheckStrategy(
				Duration.ofSeconds(1)
				)
			);
    	container.withExposedPorts(3000, 3000);
/*
 * 
    	container.setCommand(
				"sh",
				"-c",
				"sleep 120"
				);
 *     	
 */
		log.debug("starting");
		container.start();
		ident = container.getContainerId();
		log.debug("started");
        log.debug("port {}", container.getMappedPort(3000));

        
        
        }
    
    @Test
    public void checkTwo() throws Exception
        {
		
    	log.debug("stopping");
    	DockerClient client = DockerClientBuilder.getInstance().build();    	
		StopContainerCmd stop = client.stopContainerCmd(ident).withTimeout(10);
		stop.exec();
		log.debug("stopped");
        }
    }

/*
new IndefiniteWaitOneShotStartupCheckStrategy()
new OneShotStartupCheckStrategy().withTimeout(
    Duration.ofSeconds(3)
		)
*/		    			
