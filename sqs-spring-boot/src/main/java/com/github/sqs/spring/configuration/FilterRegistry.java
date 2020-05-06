package com.github.sqs.spring.configuration;

import com.github.sqs.spring.core.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * filter registry
 *
 * @author 无羡 2020-04-30
 */
public class FilterRegistry {
    private List<Filter> filters = new ArrayList<>();

    public void registerFilter(Filter filter) {
        filters.add(filter);
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }
}
