// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.ragcdev.learn.msal4j.resourceserver;

import java.util.Date;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/api/date/appRole")
    @ResponseBody
    @PreAuthorize("hasAuthority('APPROLE_Allow.Access.WebApi.AppRole') or hasAuthority('SCOPE_Allow.Access.WebApi.AppRole')")
    public String dateProtectedAppRole() {
        return new DateResponse().toString();
    }

    @GetMapping("/api/date")
    @ResponseBody
    public String date() {        
        return new DateResponse().toString();
    }
    private class DateResponse {
        private String humanReadable;
        private String timeStamp;
        private String principal;
        public DateResponse() {
            Date now = new Date();
            this.humanReadable = now.toString();
            this.timeStamp = Long.toString(now.getTime());
        }
        public String toString() {
            return String.format("{\"humanReadable\": \"%s\", \"timeStamp\": \"%s\"}", principal, humanReadable, timeStamp);
        }
    }
}
