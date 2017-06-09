package uk.gov.justice.digital.pdf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import uk.gov.justice.digital.pdf.data.PdfRequest;
import uk.gov.justice.digital.pdf.service.PdfGenerator;

import static spark.Spark.*;
import static uk.gov.justice.digital.pdf.helpers.JsonRoute.*;

@Slf4j
public class Server {

    public static void main(String[] args) {

        log.info("Started PDF Generator Service ...");

        run(new Configuration());
    }

    public static void run(Configuration configuration)  {

        val injector = Guice.createInjector(configuration);

        port(injector.getInstance(Key.get(Integer.class, Names.named("port"))));

        getJson("/configuration", configuration::allSettings);
        postJson("/generate", PdfRequest.class, injector.getInstance(PdfGenerator.class)::process);


        if (injector.getInstance(Key.get(Boolean.class, Names.named("debugLog")))) { // Set DEBUG log and debug template endpoint

            ((Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);

            get("/debug/:template", (request, response) -> {

                response.type("application/pdf");

                return ArrayUtils.toPrimitive(
                        injector.getInstance(PdfGenerator.class).process(new PdfRequest(
                                        request.params(":template"),
                                        request.queryMap().toMap().entrySet().stream().collect(
                                                Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]))
                                )
                        )
                );
            });
        }
    }
}

//@TODO: Tests required !!
// unit test on process for leng, integration on whole for config AND postJons returning text starting [0, 50, 5 etc
// Write a test for [0, 45, 25 ...]

// CAN THEN OFFER TWO DIFFERENT REPORTS FROM THE FRONT SCREEN IN PLAY
// RETURN A JSON ARRAY OF BYTES eg [1, 60, 24, 44] etc


// @todo: dockerise - ./gradle build && docker build -t pdfgenerator - same fat jat run Docker with environments