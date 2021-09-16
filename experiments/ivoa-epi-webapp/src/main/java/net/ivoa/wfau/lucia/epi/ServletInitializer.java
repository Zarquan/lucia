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
package net.ivoa.lucia.epi;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer
    {
	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder application)
	    {
		return application.sources(
		    EPIWebappApplication.class
		    );
	    }
    }
