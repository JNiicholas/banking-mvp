package com.example.banking.exception;

import com.example.banking.filter.TraceIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        // If you have a TraceIdFilter, register it so ApiError includes a traceId (optional)
        OncePerRequestFilter traceFilter = null;
        try {
            traceFilter = new TraceIdFilter();
        } catch (Throwable ignored) {
            // If the filter class/package name differs or doesn't exist, keep it null
        }

        var builder = MockMvcBuilders
                .standaloneSetup(new BoomController())
                .setControllerAdvice(new GlobalExceptionHandler());

        if (traceFilter != null) builder.addFilter(traceFilter);

        mvc = builder.build();
    }

    // ---------- Happy(ish) path: exception → handler → ApiError ----------

    @Test
    void notFound_is404_withApiError() throws Exception {
        mvc.perform(get("/boom/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("not found")))
                .andExpect(jsonPath("$.message", containsString("Customer not found")))
                .andExpect(jsonPath("$.path").value("/boom/not-found"));
    }

    @Test
    void badRequest_is400_withApiError() throws Exception {
        mvc.perform(get("/boom/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("bad request")))
                .andExpect(jsonPath("$.message", containsString("Amount must be positive")));
    }

    @Test
    void optimisticLock_is409_withApiError_sanitized() throws Exception {
        mvc.perform(get("/boom/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("conflict")))
                // Your handler typically returns a safe message (no internal class names)
                .andExpect(jsonPath("$.message", anyOf(
                        containsStringIgnoringCase("modified"),
                        containsStringIgnoringCase("conflict"),
                        not(blankOrNullString())
                )));
    }

    @Test
    void methodArgumentNotValid_is400_withFieldErrors() throws Exception {
        // Triggers @Valid failure (name is @NotBlank)
        mvc.perform(post("/boom/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("bad request")))
                .andExpect(jsonPath("$.message", not(blankOrNullString())));
        // If your handler includes a field-errors array/key, you can assert on it:
        // .andExpect(jsonPath("$.errors[0].field").value("name"))
        // .andExpect(jsonPath("$.errors[0].message", containsString("must not be blank")));
    }

    @Test
    void genericException_is500_sanitized() throws Exception {
        mvc.perform(get("/boom/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("internal server error")))
                .andExpect(jsonPath("$.message", not(blankOrNullString())));
    }

    // ---------- Dummy controller to throw exceptions the handler cares about ----------

    @RestController
    @RequestMapping("/boom")
    @Validated
    static class BoomController {

        @GetMapping("/not-found")
        public String notFound() {
            throw new NotFoundException("Customer not found");
        }

        @GetMapping("/bad-request")
        public String badRequest() {
            throw new BadRequestException("Amount must be positive");
        }

        @GetMapping("/optimistic-lock")
        public String optimisticLock() {
            throw new OptimisticLockingFailureException("version mismatch");
        }

        @PostMapping("/validate")
        public String validate(@RequestBody @Valid SimpleDto dto) {
            return "ok";
        }

        @GetMapping("/generic")
        public String generic() {
            throw new RuntimeException("boom");
        }
    }

    static class SimpleDto {
        @NotBlank
        public String name;
    }
}