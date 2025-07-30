package com.example.rest;

import com.example.dto.CarFilter;
import com.example.dto.FilterOptionsDto;
import com.example.dto.PagedResult;
import com.example.entity.Car;
import com.example.repository.CarRepository;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cars")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarResource {

    private final CarRepository carRepository;

    public CarResource(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @GET
    @Path("/filter-options")
    public FilterOptionsDto getFilterOptions() {
        return carRepository.getFilterOptions();
    }

    @POST
    @Path("/search")
    public Response searchCars(
            CarFilter filter,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("10") int pageSize) {
        PagedResult<Car> result = carRepository.search(filter, pageIndex, pageSize);
        return Response.ok(result.list())
                .header("X-Total-Count", result.totalCount())
                .build();
    }
}