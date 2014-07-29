/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package controllers;

import java.io.StringWriter;

import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class MyController extends Controller {

    protected static ObjectMapper mapper = JsonUtil.mapper();

    /**
     * @param obj
     *            a string representation of this object will be retourned
     * @return a json response for the passed object
     */
    public static Result JsonResponse(Object obj) {
	return JsonResponse(obj, 200);
    }

    /**
     * @param obj
     *            a string representation of this object will be retourned
     * @param code
     *            http code
     * @return JSON Standard Response
     */
    public static Result JsonResponse(Object obj, int code) {
	response().setHeader("Access-Control-Allow-Methods",
		"POST, GET, OPTIONS, PUT, DELETE");
	response().setHeader("Access-Control-Max-Age", "3600");
	response()
		.setHeader("Access-Control-Allow-Headers",
			"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token");
	response().setHeader("Access-Control-Allow-Credentials", "true");
	response().setHeader("Content-Type", "application/json; charset=utf-8");

	StringWriter w = new StringWriter();
	try {
	    mapper.writeValue(w, obj);
	} catch (Exception e) {
	    return internalServerError("Not able to create response!");
	}
	return status(code, w.toString());
    }
}