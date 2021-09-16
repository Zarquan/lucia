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

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import org.springframework.security.oauth2.core.oidc.user.OidcUser;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@RestController
public class EPIController
    {
    protected static final String EPI_TASK_QUERY_PATH = "/check";
    protected static final String EPI_TASK_TYPE_PARAM = "tasktype";

    protected static final String QWERTY_TASK_TYPE    = "http://purl.example.org/qwerty-task";
    protected static final String QWERTY_SERVICE_TYPE = "http://purl.example.org/qwerty-service";

    protected static final String QWERTY_ENDPOINT_KEY = "qwerty.endpoint";
    protected static final String QWERTY_ENDPOINT_URL = "http://qwerty.example.org/service";

    protected static final String OIDC_FULLNAME_KEY = "oidc.fullname";

    public static enum EPIResponseEnum
        {
        YES,
        NO,
        MAYBE
        }

	@ResponseBody
	@RequestMapping(value = EPI_TASK_QUERY_PATH, method = { RequestMethod.GET, RequestMethod.POST })
	public EPIResponseBean check(
	    @RequestParam(name=EPI_TASK_TYPE_PARAM) String tasktype,
        //@AuthenticationPrincipal O-idcUser principal,
	    Model model
	    ) {
        if (QWERTY_TASK_TYPE.equals(tasktype))
            {
            /*
            String name = principal.map(
                OidcUser::getFullName
                );
             */
            return new EPIResponseBean(
                EPIResponseEnum.YES,
                QWERTY_SERVICE_TYPE
                )
                .addDetail(
                    QWERTY_ENDPOINT_KEY,
                    QWERTY_ENDPOINT_URL
                    );
                /*
                .addDetail(
                    OIDC_FULLNAME_KEY,
                    name
                    );
                */
            }
        else {
            return new EPIResponseBean(
                EPIResponseEnum.NO
                );
            }
	    }

    @JsonPropertyOrder({"reponsecode", "servicetype", "serviceinfo"})
    public static class EPIResponseBean
        {
        protected EPIResponseBean()
            {}

        public EPIResponseBean(final EPIResponseEnum code)
            {
            this(code, null);
            }

        public EPIResponseBean(final EPIResponseEnum code, final String type)
            {
            this.code = code ;
            this.type = type ;
            }

        private EPIResponseEnum code;

        @JsonProperty("reponsecode")
        public EPIResponseEnum getCode()
            {
            return this.code ;
            }

        private String type;

        @JsonProperty("servicetype")
        public String getType()
            {
            return this.type ;
            }

        protected Map<String, String> details = new HashMap<String, String>();

        @JsonProperty("serviceinfo")
        public Map<String, String> getDetails()
            {
            return this.details ;
            }

        public EPIResponseBean addDetail(final String key, final String value)
            {
            this.details.put(
                key,
                value
                );
            return this;
            }
        }
    }

