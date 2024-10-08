package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.cphbusiness.utils.Utils;
import lombok.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();
        try {
            List<DTOs.FlightDTO> flightList = flightReader.getFlightsFromFile("flights.json");

            /*List<DTOs.FlightInfo> flightInfoList = flightReader.getFlightInfoDetails(flightList);
//            flightInfoList.forEach(f->{
//                System.out.println("\n"+f);
            });*/

            List<DTOs.FlightInfo> specificFlightInfoList = getSpecificAirportDepartures(flightList);
            specificFlightInfoList.forEach(flightInfo -> {
                String formattedOutput = String.format(
                        "\nFlight Information:" +
                                "\n-------------------" +
                                "\nFlight Number : %s" +
                                "\nIATA Code     : %s" +
                                "\nAirline       : %s" +
                                "\nDuration      : %s minutes" +
                                "\nDeparture     : %s" +
                                "\nArrival       : %s" +
                                "\nOrigin        : %s" +
                                "\nDestination   : %s",
                        flightInfo.getName(),
                        flightInfo.getIata(),
                        flightInfo.getAirline(),
                        flightInfo.getDuration().toMinutes(),
                        flightInfo.getDeparture(),
                        flightInfo.getArrival(),
                        flightInfo.getOrigin(),
                        flightInfo.getDestination()
                );

                System.out.println(formattedOutput);
            });

            Duration avgFlightTime = flightReader.getAvgFlightTimeFromIATA("AQJ", flightInfoList);
            System.out.printf("%n%n");
            System.out.println(" >>> AVG Flight Time for AQJ: " +  avgFlightTime.toString());
            System.out.printf("%n%n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
//        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
//        return flights;
//    }

    public static List<DTOs.FlightInfo> getSpecificAirportDepartures(List<DTOs.FlightDTO> flightList) {

        // Using Scanner for Getting Input from User
        try (Scanner in = new Scanner(System.in)) {
            System.out.println("Please enter your desired airport:");
            String airportInput = in.nextLine();
            System.out.println("" + airportInput);

            // Filtering and mapping flights in a single stream operation
            return flightList.stream()
                    .filter(flight -> {
                        String airport = flight.getDeparture().getAirport();
                        return airport != null && airport.equals(airportInput);
                    })
                    .map(flight -> {
                        Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
                        return DTOs.FlightInfo.builder()
                                .name(flight.getFlight().getNumber())
                                .iata(flight.getFlight().getIata())
                                .airline(flight.getAirline().getName())
                                .duration(duration)
                                .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                                .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                                .origin(flight.getDeparture().getAirport())
                                .destination(flight.getArrival().getAirport())
                                .build();
                    })
                    .collect(Collectors.toList());
        }
    }

    public List<DTOs.FlightInfo> getFlightInfoDetails(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightInfo> flightInfoList = flightList.stream().map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            DTOs.FlightInfo flightInfo = DTOs.FlightInfo.builder()
                    .name(flight.getFlight().getNumber())
                    .iata(flight.getFlight().getIata())
                    .airline(flight.getAirline().getName())
                    .duration(duration)
                    .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                    .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                    .origin(flight.getDeparture().getAirport())
                    .destination(flight.getArrival().getAirport())
                    .build();

            return flightInfo;
        }).toList();
        return flightInfoList;
    }

    public List<DTOs.FlightDTO> getFlightsFromFile(String filename) throws IOException {
        DTOs.FlightDTO[] flights = new Utils().getObjectMapper().readValue(Paths.get(filename).toFile(), DTOs.FlightDTO[].class);

        List<DTOs.FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;
    }

    public Duration getAvgFlightTimeFromIATA(String iata, List<DTOs.FlightInfo> flightInfoList) {
        long total = 0;
        for (DTOs.FlightInfo flightInfo : flightInfoList) {
            total += flightInfo.getDuration().getSeconds();
        }
        return Duration.ofSeconds(total/flightInfoList.size());
    }

}




















