package com.example.repository;

import java.util.Collections;
import java.util.List;

import com.example.dto.CarFilter;
import com.example.dto.FilterOptionsDto;
import com.example.dto.PagedResult;
import com.example.entity.Brand;
import com.example.entity.Car;
import com.example.entity.Dealership;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class CarRepository implements PanacheRepository<Car> {

    private final EntityManager entityManager;

    public CarRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public FilterOptionsDto getFilterOptions() {
        List<Brand> brands = Brand.listAll(Sort.by("name"));
        List<Dealership> dealerships = Dealership.listAll(Sort.by("name", "city"));
        List<String> colors = entityManager
                .createQuery("SELECT DISTINCT c.color FROM Car c ORDER BY c.color", String.class)
                .getResultList();
        List<String> features = entityManager
                .createQuery("SELECT DISTINCT f FROM Car c JOIN c.features f ORDER BY f", String.class)
                .getResultList();
        return new FilterOptionsDto(brands, dealerships, colors, features);
    }

    public PagedResult<Car> search(CarFilter filter, int pageIndex, int pageSize) {
        StringBuilder queryBuilder = new StringBuilder();
        Parameters params = new Parameters();

        addCondition(filter.brandIds, "brand.id IN :brandIds", "brandIds", queryBuilder, params);
        addCondition(filter.dealershipIds, "dealership.id IN :dealershipIds", "dealershipIds", queryBuilder, params);
        addCondition(filter.colors, "color IN :colors", "colors", queryBuilder, params);

        if (filter.features != null && !filter.features.isEmpty()) {
            queryBuilder.append("AND id IN (SELECT c.id FROM Car c JOIN c.features f WHERE f IN :features) ");
            params.and("features", filter.features);
        }

        addRangeCondition(filter.minYear, "productionYear >= :minYear", "minYear", queryBuilder, params);
        addRangeCondition(filter.maxYear, "productionYear <= :maxYear", "maxYear", queryBuilder, params);
        addRangeCondition(filter.minPrice, "price >= :minPrice", "minPrice", queryBuilder, params);
        addRangeCondition(filter.maxPrice, "price <= :maxPrice", "maxPrice", queryBuilder, params);

        String conditions = queryBuilder.length() > 0 ? queryBuilder.substring(4) : "1=1";

        long totalCount = find(conditions, params).count();
        List<Car> pagedCars = find(conditions, params)
                .page(Page.of(pageIndex, pageSize))
                .list();

        if (pagedCars.isEmpty()) {
            return new PagedResult<>(Collections.emptyList(), 0);
        }

        List<Long> carIds = pagedCars.stream()
                .map(car -> car.id)
                .toList();

        List<Car> cars = find("id IN ?1", carIds)
                .withHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Car.withBrandAndDealership"))
                .list();
        return new PagedResult<>(cars, totalCount);
    }

    private void addCondition(Object value, String clause, String paramName, StringBuilder qb, Parameters params) {
        if (value instanceof java.util.Collection && !((java.util.Collection<?>) value).isEmpty()) {
            qb.append("AND ").append(clause).append(" ");
            params.and(paramName, value);
        }
    }

    private void addRangeCondition(Object value, String clause, String paramName, StringBuilder qb, Parameters params) {
        if (value != null) {
            qb.append("AND ").append(clause).append(" ");
            params.and(paramName, value);
        }
    }
}