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

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.json.JSONObject;

@SpringBootTest
@AutoConfigureMockMvc
public class EPIControllerTest
    {

    @Autowired
    private MockMvc mvc;

    @Test
    public void checkNot() throws Exception
        {
        mvc.perform(
            MockMvcRequestBuilders.get(
                EPIController.EPI_TASK_QUERY_PATH
                )
            .accept(
                MediaType.APPLICATION_JSON
                )
            .param(
                EPIController.EPI_TASK_TYPE_PARAM,
                "nope"
                )
            )
            .andExpect(
                status().isOk()
                )
            .andExpect(
                content().string(
                    equalTo("NO")
                    )
                );
        }
    }

